package com.training.orderservice.client.impl;

import com.training.orderservice.client.ProductServiceClient;
import com.training.orderservice.client.dto.ProductSnapshot;
import com.training.orderservice.exception.InsufficientStockException;
import com.training.orderservice.exception.ProductNotFoundException;
import com.training.orderservice.exception.ProductServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Talks to the real Product Service over HTTP (SDD Section 29.1). The endpoint
 * shapes below follow the Product Service's actual contract, not the SDD's
 * originally-assumed one — only this implementation needed to change, per the
 * SDD's own note that the Order Service's domain logic stays untouched either way.
 */
@Component
public class ProductServiceRestClient implements ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceRestClient.class);

    private final RestClient restClient;

    public ProductServiceRestClient(@Qualifier("productServiceHttpClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public ProductSnapshot getProduct(UUID productId) {
        try {
            ProductResponse response = restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class);
            return new ProductSnapshot(response.id(), response.productName(), response.price(), response.stockQuantity());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException("Product " + productId + " was not found in the Product Service");
        } catch (RestClientException ex) {
            log.warn("Product Service call failed while fetching product {}", productId, ex);
            throw new ProductServiceUnavailableException("Product Service is unavailable while fetching product " + productId, ex);
        }
    }

    @Override
    public void reduceStock(UUID productId, int quantity, Long orderId) {
        try {
            restClient.patch()
                    .uri("/api/v1/products/{id}/reduce-stock", productId)
                    .body(new StockReductionRequest(quantity, String.valueOf(orderId)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException("Product " + productId + " was not found in the Product Service");
        } catch (HttpClientErrorException.Conflict ex) {
            // Product Service's own atomic reduce-stock enforces stock non-negativity as a
            // race-condition safety net beyond the Order Service's own availability check.
            throw new InsufficientStockException(
                    "Product Service rejected the stock reduction for product " + productId + ": insufficient stock");
        } catch (RestClientException ex) {
            log.warn("Product Service call failed while reducing stock for product {}", productId, ex);
            throw new ProductServiceUnavailableException("Product Service is unavailable while reducing stock for product " + productId, ex);
        }
    }

    @Override
    public void restoreStock(UUID productId, int quantity, Long orderId) {
        try {
            restClient.patch()
                    .uri("/api/v1/products/{id}/stock", productId)
                    .body(new StockAdjustmentRequest(quantity, "INCREASE"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException("Product " + productId + " was not found in the Product Service");
        } catch (RestClientException ex) {
            log.warn("Product Service call failed while restoring stock for product {}", productId, ex);
            throw new ProductServiceUnavailableException("Product Service is unavailable while restoring stock for product " + productId, ex);
        }
    }

    private record ProductResponse(UUID id, String productName, BigDecimal price, Integer stockQuantity) {
    }

    private record StockReductionRequest(Integer quantity, String orderReference) {
    }

    private record StockAdjustmentRequest(Integer quantity, String operation) {
    }
}
