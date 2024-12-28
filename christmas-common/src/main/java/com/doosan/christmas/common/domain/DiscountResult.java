package com.doosan.christmas.common.domain;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DiscountResult {
    private int totalAmount;
    private int discountAmount;
    private int finalAmount;
    private List<String> appliedEvents;
} 