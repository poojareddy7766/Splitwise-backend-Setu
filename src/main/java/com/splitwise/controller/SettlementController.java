package com.splitwise.controller;

import com.splitwise.model.Settlement;
import com.splitwise.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    @Autowired
    private SettlementRepository settlementRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER')") // Only users with the 'USER' role can access this endpoint
    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }
}