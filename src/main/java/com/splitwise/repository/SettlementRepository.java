package com.splitwise.repository;

import com.splitwise.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

}