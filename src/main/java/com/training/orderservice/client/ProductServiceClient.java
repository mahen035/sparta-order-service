package com.training.orderservice.client;

import com.training.orderservice.client.dto.ProductSnapshot;

public interface ProductServiceClient {

    ProductSnapshot getProduct(Long productId);

    void reduceStock(Long productId, int quantity, Long orderId);

    void restoreStock(Long productId, int quantity, Long orderId);
}
