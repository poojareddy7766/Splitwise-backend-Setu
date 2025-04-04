package com.splitwise.service;

import com.splitwise.dto.ExpenseDTO;
import com.splitwise.dto.ExpenseRequest;
import com.splitwise.model.*;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.SettlementRepository;
import com.splitwise.repository.UserBalanceRepository;
import com.splitwise.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
   private SettlementRepository settlementRepository;
    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository,UserBalanceRepository userBalanceRepository, SettlementRepository settlementRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.userBalanceRepository = userBalanceRepository;
        this.settlementRepository = settlementRepository;
    }
    public Page<Expense> getExpensesByUserIdPaginated(Long userId, Pageable pageable) {
        return expenseRepository.findByUserShares_User_Id(userId, pageable);
    }

   @Transactional
   public Expense createExpense(ExpenseRequest request) {
       // Fetch the user who paid the expense
       User paidBy = userRepository.findById(request.getPaidById())
               .orElseThrow(() -> new RuntimeException("User not found"));
       Expense expense = new Expense();
       expense.setDescription(request.getDescription());
       expense.setAmount(request.getAmount());
       expense.setPaidBy(paidBy);
       expense.setSplitType(request.getSplitType());
       expense.setUserShares(request.getUserShares().entrySet().stream()
               .map(entry -> {
                   ExpenseShare share = new ExpenseShare();
                   share.setUser(userRepository.findById(entry.getKey())
                           .orElseThrow(() -> new RuntimeException("User not found")));
                   share.setAmount(entry.getValue());
                   share.setExpense(expense);
                   return share;
               })
               .collect(Collectors.toList()));
       Expense savedExpense = expenseRepository.save(expense);
       updateUserBalances(savedExpense);

       return savedExpense;
   }
    private void updateUserBalances(Expense expense) {
        Long paidById = expense.getPaidBy().getId();
        BigDecimal totalAmount = expense.getAmount();
        int numberOfUsers = expense.getUserShares().size();
        BigDecimal equalShare = totalAmount.divide(BigDecimal.valueOf(numberOfUsers), RoundingMode.HALF_UP);
        for (ExpenseShare share : expense.getUserShares()) {
            Long userId = share.getUser().getId();
            BigDecimal shareAmount = share.getAmount();
            BigDecimal balanceAmount = shareAmount.subtract(equalShare);
            if (userId.equals(paidById)) {
                continue;
            }
            UserBalance userBalance = userBalanceRepository.findByUserIdAndOtherUserId(userId, paidById)
                    .orElseGet(() -> {
                        UserBalance newBalance = new UserBalance();
                        newBalance.setUserId(userId);
                        newBalance.setOtherUserId(paidById);
                        newBalance.setBalanceAmount(BigDecimal.ZERO);
                        return newBalance;
                    });

            userBalance.setBalanceAmount(userBalance.getBalanceAmount().subtract(balanceAmount));
            userBalanceRepository.save(userBalance);
            UserBalance payerBalance = userBalanceRepository.findByUserIdAndOtherUserId(paidById, userId)
                    .orElseGet(() -> {
                        UserBalance newBalance = new UserBalance();
                        newBalance.setUserId(paidById);
                        newBalance.setOtherUserId(userId);
                        newBalance.setBalanceAmount(BigDecimal.ZERO);
                        return newBalance;
                    });

            payerBalance.setBalanceAmount(payerBalance.getBalanceAmount().add(balanceAmount));
            userBalanceRepository.save(payerBalance);
        }
    }

    public Map<String, String> getUserBalance(Long userId) {
        List<UserBalance> balancesOwed = userBalanceRepository.findByUserId(userId);
        List<UserBalance> balancesReceivable = userBalanceRepository.findByOtherUserId(userId);
        BigDecimal totalOwed = balancesOwed.stream()
                .map(UserBalance::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceivable = balancesReceivable.stream()
                .map(UserBalance::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netBalance = totalReceivable.subtract(totalOwed);
        Map<String, String> response = new HashMap<>();
        if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
            response.put("message", "User " + userId + " is owed ₹" + netBalance);
        } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
            response.put("message", "User " + userId + " owes ₹" + netBalance.abs());
        } else {
            response.put("message", "User " + userId + " owes nothing");
        }

        return response;
    }



    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public List<Expense> getExpensesByUserId(Long userId) {
    return expenseRepository.findAll()
            .stream()
            .filter(expense -> expense.getUserShares().stream()
                    .anyMatch(share -> share.getUser().getId().equals(userId)))
            .collect(Collectors.toList());
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
public void settlePayment(Long senderId, Long receiverId, BigDecimal amount) {
    UserBalance senderBalance = userBalanceRepository.findByUserIdAndOtherUserId(senderId, receiverId)
            .orElseGet(() -> {
                UserBalance newBalance = new UserBalance();
                newBalance.setUserId(senderId);
                newBalance.setOtherUserId(receiverId);
                newBalance.setBalanceAmount(BigDecimal.ZERO);
                return newBalance;
            });
    senderBalance.setBalanceAmount(senderBalance.getBalanceAmount().subtract(amount));
    if (senderBalance.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
        userBalanceRepository.delete(senderBalance);
    } else {
        userBalanceRepository.save(senderBalance);
    }
    UserBalance receiverBalance = userBalanceRepository.findByUserIdAndOtherUserId(receiverId, senderId)
            .orElseGet(() -> {
                UserBalance newBalance = new UserBalance();
                newBalance.setUserId(receiverId);
                newBalance.setOtherUserId(senderId);
                newBalance.setBalanceAmount(BigDecimal.ZERO);
                return newBalance;
            });
    receiverBalance.setBalanceAmount(receiverBalance.getBalanceAmount().add(amount));

    if (receiverBalance.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
        userBalanceRepository.delete(receiverBalance);
    } else {
        userBalanceRepository.save(receiverBalance);
    }
    Settlement settlement = new Settlement();
    settlement.setSenderId(senderId);
    settlement.setReceiverId(receiverId);
    settlement.setAmount(amount);
    settlementRepository.save(settlement);
    System.out.println("Updated sender balance: " + senderBalance);
    System.out.println("Updated receiver balance: " + receiverBalance);
    System.out.println("Recorded settlement: " + settlement);
}

public Map<Long, String> getAllUserBalances() {

    List<UserBalance> allBalances = userBalanceRepository.findAll();

    Map<Long, BigDecimal> userNetBalances = new HashMap<>();

    for (UserBalance balance : allBalances) {
        Long userId = balance.getUserId();
        Long otherUserId = balance.getOtherUserId();
        BigDecimal balanceAmount = balance.getBalanceAmount();

        userNetBalances.put(userId, userNetBalances.getOrDefault(userId, BigDecimal.ZERO).subtract(balanceAmount));

        userNetBalances.put(otherUserId, userNetBalances.getOrDefault(otherUserId, BigDecimal.ZERO).add(balanceAmount));
    }


    Map<Long, String> response = new HashMap<>();
    for (Map.Entry<Long, BigDecimal> entry : userNetBalances.entrySet()) {
        Long userId = entry.getKey();
        BigDecimal netBalance = entry.getValue();

        if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
            response.put(userId, "User " + userId + " is with a profit of ₹" + netBalance);
        } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
            response.put(userId, "User " + userId + " owes ₹" + netBalance.abs());
        } else {
            response.put(userId, "User " + userId + " owes nothing");
        }
    }
    List<Long> allUserIds = userRepository.findAll().stream().map(User::getId).collect(Collectors.toList());
    for (Long userId : allUserIds) {
        if (!response.containsKey(userId)) {
            response.put(userId, "User " + userId + " owes nothing");
        }
    }

    return response;
}
}