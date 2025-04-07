package com.splitwise.controller;

import com.splitwise.dto.ExpenseDTO;
import com.splitwise.dto.ExpenseRequest;

import com.splitwise.model.Expense;
import com.splitwise.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.splitwise.model.ExpenseShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    @Autowired
    private final ExpenseService expenseService;

    @PostMapping("/add")
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseRequest request) {
        Expense expense = expenseService.createExpense(request);
        ExpenseDTO expenseDTO = mapToDTO(expense);
        return ResponseEntity.ok(expenseDTO);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }

   private ExpenseDTO mapToDTO(Expense expense) {
    ExpenseDTO dto = new ExpenseDTO();
    dto.setId(expense.getId());
    dto.setDescription(expense.getDescription());
    dto.setAmount(expense.getAmount());
    dto.setPaidById(expense.getPaidBy().getId());
    dto.setSplitType(expense.getSplitType());
    dto.setUserShares(expense.getUserShares().stream()
            .collect(Collectors.toMap(
                    share -> share.getUser().getId(),
                    ExpenseShare::getAmount
            )));
    return dto;
}
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        Expense expense = expenseService.getExpenseById(id);
        ExpenseDTO expenseDTO = mapToDTO(expense);
        return ResponseEntity.ok(expenseDTO);
    }
    @GetMapping("/user/{userId}")
public ResponseEntity<List<ExpenseDTO>> getExpensesByUserId(@PathVariable Long userId) {
    List<ExpenseDTO> expenses = expenseService.getExpensesByUserId(userId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(expenses);
}
@GetMapping("/user/{userId}/fetchAll")
    public Page<Expense> getExpensesByUserIdPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return expenseService.getExpensesByUserIdPaginated(userId, pageable);
    }

    @GetMapping("/getBalance/{id}")
    public ResponseEntity<Map<String, Object>> getUserBalance(@PathVariable Long id) {
        Map<String, Object> balance = expenseService.getUserBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/settle")
    public ResponseEntity<String> settlePayment(@RequestParam Long senderId,@RequestParam Long receiverId, @RequestParam BigDecimal amount) {
        String message = expenseService.settlePayment(senderId, receiverId, amount);
        return ResponseEntity.ok(message);
    }
   
}