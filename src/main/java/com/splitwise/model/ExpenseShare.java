package com.splitwise.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expense_shares") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseShare {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "expense_id")
        @JsonBackReference 
        private Expense expense;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private BigDecimal amount;
    }
