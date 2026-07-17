package com.training.orderservice.service.impl;

import com.training.orderservice.entity.Order;
import com.training.orderservice.exception.OrderNotFoundException;
import com.training.orderservice.repository.OrderRepository;
import com.training.orderservice.security.CallerContext;
import com.training.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, CallerContext caller) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!caller.isAdmin() && !order.getCustomerId().equals(caller.customerId())) {
            // BR-10: a cross-customer access attempt must not be distinguishable from a
            // missing order, so this reuses OrderNotFoundException rather than a 403.
            throw new OrderNotFoundException(orderId);
        }

        return order;
    }
}
