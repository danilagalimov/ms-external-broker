package com.broker.repository;


import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    <T> Optional<T> findById(UUID id, Class<T> resultClass);

    @Modifying
    @Query("update Trade t set t.status = ?1 where t.id = ?2")
    void updateTradeStatus(ExecutionStatus newStatus, UUID tradeId);

    @Modifying
    @Query("update Trade t set t.status = ?1, t.reason = ?2 where t.id = ?3")
    void updateTradeStatusAndReason(ExecutionStatus newStatus, String reason, UUID tradeId);

}
