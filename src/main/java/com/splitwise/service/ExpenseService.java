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
import java.util.Optional;
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
        SplitType splitType = expense.getSplitType();

        Map<Long, BigDecimal> userToOwedAmount = new HashMap<>();
        List<ExpenseShare> shares = expense.getUserShares();
        switch (splitType) {
            case EQUAL:
                BigDecimal equalShare = totalAmount.divide(BigDecimal.valueOf(shares.size()), RoundingMode.HALF_UP);
                for (ExpenseShare share : shares) {
                    userToOwedAmount.put(share.getUser().getId(), equalShare);
                }
                break;

            case UNEQUAL:
                for (ExpenseShare share : shares) {
                    userToOwedAmount.put(share.getUser().getId(), share.getAmount());
                }
                break;

            case PERCENTAGE:
                for (ExpenseShare share : shares) {
                    BigDecimal percentage = share.getAmount(); // here see it this way, 25 means 25%
                    BigDecimal calculatedAmount = totalAmount.multiply(percentage).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                    userToOwedAmount.put(share.getUser().getId(), calculatedAmount);
                }
                break;

            default:
                throw new RuntimeException("Unsupported split type: " + splitType);
        }

        for (Map.Entry<Long, BigDecimal> entry : userToOwedAmount.entrySet()) {
            Long userId = entry.getKey();
            BigDecimal amountOwed = entry.getValue();

            if (userId.equals(paidById)) {
                continue;
            }
            Long debtorId = userId;
            Long creditorId = paidById;
            BigDecimal amount = amountOwed;

            Optional<UserBalance> forwardBalanceOpt = userBalanceRepository.findByUserIdAndOtherUserId(debtorId, creditorId);
            Optional<UserBalance> reverseBalanceOpt = userBalanceRepository.findByUserIdAndOtherUserId(creditorId, debtorId);

            if (forwardBalanceOpt.isPresent()) {
                UserBalance balance = forwardBalanceOpt.get();
                balance.setBalanceAmount(balance.getBalanceAmount().add(amount));
                userBalanceRepository.save(balance);
            } else if (reverseBalanceOpt.isPresent()) {
                UserBalance balance = reverseBalanceOpt.get();
                BigDecimal newAmount = balance.getBalanceAmount().subtract(amount);

                if (newAmount.compareTo(BigDecimal.ZERO) > 0) {
                    balance.setBalanceAmount(newAmount);
                    userBalanceRepository.save(balance);
                } else if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
                    balance.setUserId(debtorId);
                    balance.setOtherUserId(creditorId);
                    balance.setBalanceAmount(newAmount.abs());
                    userBalanceRepository.save(balance);
                } else {
                    userBalanceRepository.delete(balance);
                }
            } else {
                UserBalance newBalance = new UserBalance();
                newBalance.setUserId(debtorId);
                newBalance.setOtherUserId(creditorId);
                newBalance.setBalanceAmount(amount);
                userBalanceRepository.save(newBalance);
            }
        }
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
    public Map<String, Object> getUserBalance(Long userId) {
        List<UserBalance> balances = userBalanceRepository.findByUserId(userId);

        List<Map<String, Object>> owedList = balances.stream()
                .filter(balance -> balance.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(balance -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("owesTo", balance.getOtherUserId());
                    entry.put("amount", balance.getBalanceAmount());
                    return entry;
                })
                .collect(Collectors.toList());

        BigDecimal totalOwed = owedList.stream()
                .map(entry -> (BigDecimal) entry.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("totalOwed", totalOwed);
        response.put("breakdown", owedList);

        return response;
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

    public String settlePayment(Long senderId, Long receiverId, BigDecimal amount) {
        UserBalance senderBalance = userBalanceRepository.findByUserIdAndOtherUserId(senderId, receiverId)
                .orElseGet(() -> {
                    UserBalance newBalance = new UserBalance();
                    newBalance.setUserId(senderId);
                    newBalance.setOtherUserId(receiverId);
                    newBalance.setBalanceAmount(BigDecimal.ZERO);
                    return newBalance;
                });

        BigDecimal updatedBalance = senderBalance.getBalanceAmount().subtract(amount);
        if (updatedBalance.compareTo(BigDecimal.ZERO) > 0) {
            senderBalance.setBalanceAmount(updatedBalance);
            userBalanceRepository.save(senderBalance);
        } else if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal reversedAmount = updatedBalance.abs();
            if (senderBalance.getId() != null) {
                userBalanceRepository.delete(senderBalance);
            }
            UserBalance reverseBalance = userBalanceRepository.findByUserIdAndOtherUserId(receiverId, senderId)
                    .orElseGet(() -> {
                        UserBalance newBalance = new UserBalance();
                        newBalance.setUserId(receiverId);
                        newBalance.setOtherUserId(senderId);
                        newBalance.setBalanceAmount(BigDecimal.ZERO);
                        return newBalance;
                    });

            reverseBalance.setBalanceAmount(reversedAmount);
            userBalanceRepository.save(reverseBalance);
        } else {
            if (senderBalance.getId() != null) {
                userBalanceRepository.delete(senderBalance);
            }
        }

        Settlement settlement = new Settlement();
        settlement.setSenderId(senderId);
        settlement.setReceiverId(receiverId);
        settlement.setAmount(amount);
        settlementRepository.save(settlement);

        String message = String.format("User %d has successfully settled payment of ₹%.2f to User %d", senderId, amount, receiverId);
        System.out.println(message);
        return message;
    }



    public Map<Long, String> getAllUserBalances() {
        List<UserBalance> allBalances = userBalanceRepository.findAll();
        Map<Long, Map<Long, BigDecimal>> userToOwedMap = new HashMap<>();
        for (UserBalance balance : allBalances) {
            Long userId = balance.getUserId();
            Long otherUserId = balance.getOtherUserId();
            BigDecimal amount = balance.getBalanceAmount();

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                userToOwedMap
                        .computeIfAbsent(userId, k -> new HashMap<>())
                        .merge(otherUserId, amount, BigDecimal::add);
            }
        }
        Map<Long, String> response = new HashMap<>();
        List<Long> allUserIds = userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        for (Long userId : allUserIds) {
            Map<Long, BigDecimal> owesMap = userToOwedMap.getOrDefault(userId, new HashMap<>());

            if (!owesMap.isEmpty()) {
                BigDecimal totalOwed = owesMap.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String toUsers = owesMap.keySet().stream()
                        .map(id -> "User " + id)
                        .collect(Collectors.joining(", "));

                response.put(userId, "User " + userId + " owes a total of ₹" + totalOwed + " to {" + toUsers + "}");
            } else {
                response.put(userId, "User " + userId + " owes nothing");
            }
        }
        return response;
    }
}