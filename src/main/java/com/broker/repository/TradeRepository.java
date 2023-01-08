package com.broker.repository;


import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    @Modifying
    @Query("update Trade t set t.status = ?1 where t.id = ?2 and t.status = ?3 and t.timestamp >= ?4")
    int updateTradeStatus(ExecutionStatus newStatus, UUID tradeId, ExecutionStatus expectedStatus, LocalDateTime lastAllowedTime);

    @Modifying
    @Query("update Trade t set t.status = ?1, t.reason = ?2 where t.id = ?3 and t.status = ?4 and t.timestamp >= ?5")
    int updateTradeStatusAndReason(ExecutionStatus newStatus, String reason, UUID tradeId, ExecutionStatus expectedStatus, LocalDateTime lastAllowedTime);

}
