package com.splitwise.dto;

import com.splitwise.model.SplitType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Long paidById;
    private SplitType splitType;
    private Map<Long, BigDecimal> userShares; // Map of User ID to Share Amount
}