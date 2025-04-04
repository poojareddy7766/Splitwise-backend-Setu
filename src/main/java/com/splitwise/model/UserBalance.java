package com.splitwise.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "user_balance")
@Data
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "other_user_id", nullable = false)
    private Long otherUserId;

    @Column(name = "balance_amount", nullable = false)
    private BigDecimal balanceAmount;
}