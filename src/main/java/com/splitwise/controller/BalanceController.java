package com.splitwise.controller;

import com.splitwise.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController

@RequestMapping("/balances")
@Tag(name = "Balances", description = "APIs for managing user balances")
public class BalanceController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/all")
    @Operation(summary = "Get all user balances", description = "Fetches the balances of all users")
    public Map<Long, String> getAllUserBalances() {
        return expenseService.getAllUserBalances();
    }
}