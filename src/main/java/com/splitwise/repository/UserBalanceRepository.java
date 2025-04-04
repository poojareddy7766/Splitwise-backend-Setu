package com.splitwise.repository;

import com.splitwise.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    List<UserBalance> findByUserId(Long userId);
    List<UserBalance> findByOtherUserId(Long otherUserId);

    Optional<UserBalance> findByUserIdAndOtherUserId(Long userId, Long otherUserId);
}