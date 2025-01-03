package com.doosan.christmas.order.client;

import com.doosan.christmas.order.client.dto.ProductResponse;
import lombok.Data;

@Data
public class ProductResponseWrapper {
    private boolean success;
    private ProductResponse data;
    private String error;
    private String message;
}

