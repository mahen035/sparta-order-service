        package com.training.orderservice.controller;

        import com.training.orderservice.dto.response.OrderResponse;
        import com.training.orderservice.entity.Order;
        import com.training.orderservice.mapper.OrderMapper;
        import com.training.orderservice.security.CallerContext;
        import com.training.orderservice.service.OrderService;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.RequestHeader;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("/api/v1/orders")
        public class OrderController {

            private final OrderService orderService;
            private final OrderMapper orderMapper;

            public OrderController(OrderService orderService, OrderMapper orderMapper) {
                this.orderService = orderService;
                this.orderMapper = orderMapper;
            }

            @GetMapping("/{orderId}")
            public OrderResponse getOrder(
                    @PathVariable Long orderId,
                    @RequestHeader("X-Customer-Id") Long customerId,
                    @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String role) {
                CallerContext caller = new CallerContext(customerId, role);
                Order order = orderService.getOrderById(orderId, caller);
                return orderMapper.toResponse(order);
            }
        }
