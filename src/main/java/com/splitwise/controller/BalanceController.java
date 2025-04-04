package com.splitwise.controller;

import com.splitwise.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/balances")
public class BalanceController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/all")
    public Map<Long, String> getAllUserBalances() {
        return expenseService.getAllUserBalances();
    }
}