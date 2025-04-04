package com.splitwise.dto;

import com.splitwise.model.SplitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    private String description;
    private BigDecimal amount;
    private Long paidById;
    private SplitType splitType;
    private Map<Long, BigDecimal> userShares;
}
