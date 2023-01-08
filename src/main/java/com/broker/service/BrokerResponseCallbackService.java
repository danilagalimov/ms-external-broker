package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.external.BrokerResponseCallback;
import com.broker.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
public class BrokerResponseCallbackService implements BrokerResponseCallback {
    private final TradeRepository tradeRepository;

    private final TradeTimeoutService tradeTimeoutService;

    public BrokerResponseCallbackService( TradeRepository tradeRepository, TradeTimeoutService tradeTimeoutService) {
        this.tradeRepository = tradeRepository;
        this.tradeTimeoutService = tradeTimeoutService;
    }

    @Override
    @Transactional
    public void successful(UUID tradeId) {
        log.debug("Received successful response for trade {}", tradeId);

        int updatedCount = tradeRepository.updateTradeStatus(ExecutionStatus.EXECUTED, tradeId, ExecutionStatus.PENDING_EXECUTION, tradeTimeoutService.getLastAllowedTime());

        log.debug("Updated {} trades", updatedCount);
    }

    @Override
    @Transactional
    public void unsuccessful(UUID tradeId, String reason) {
        log.debug("Unsuccessfully processed trade {}", tradeId);

        int updatedCount = tradeRepository.updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, reason, tradeId, ExecutionStatus.PENDING_EXECUTION, tradeTimeoutService.getLastAllowedTime());

        log.debug("Updated {} trades", updatedCount);
    }
}
