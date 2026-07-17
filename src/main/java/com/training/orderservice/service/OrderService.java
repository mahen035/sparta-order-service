package com.training.orderservice.service;

import com.training.orderservice.entity.Order;
import com.training.orderservice.security.CallerContext;

public interface OrderService {

    Order getOrderById(Long orderId, CallerContext caller);
}
