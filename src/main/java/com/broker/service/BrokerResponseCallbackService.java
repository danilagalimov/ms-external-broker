package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.external.BrokerResponseCallback;
import com.broker.repository.TradeRepository;
import com.broker.service.locker.Locker;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Future;

@Service
@Log4j2
public class BrokerResponseCallbackService implements BrokerResponseCallback {
    private final Locker locker;
    private final TradeRepository tradeRepository;

    private final String tradeTimeoutReason;

    public BrokerResponseCallbackService(Locker locker, TradeRepository tradeRepository, @Value("${broker.message.trade.expired}") String tradeTimeoutReason) {
        this.locker = locker;
        this.tradeRepository = tradeRepository;
        this.tradeTimeoutReason = tradeTimeoutReason;
    }

    @Override
    @Transactional
    public void successful(UUID tradeId) {
        log.debug("Successfully processed trade {}", tradeId);
        if (tryAcquireLock(tradeId)) {
            tradeRepository.updateTradeStatus(ExecutionStatus.EXECUTED, tradeId);
        }
    }

    @Override
    @Transactional
    public void unsuccessful(UUID tradeId, String reason) {
        log.debug("Unsuccessfully processed trade {}", tradeId);
        if (tryAcquireLock(tradeId)) {
            tradeRepository.updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, reason, tradeId);
        }
    }

    @Transactional
    public void timeout(UUID tradeId) {
        log.debug("Trade {} timed out", tradeId);
        tradeRepository.updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, tradeTimeoutReason, tradeId);
    }

    private boolean tryAcquireLock(UUID tradeId) {
        Future<?> cancellationFuture = locker.getSinglePermit(tradeId);

        if (null == cancellationFuture) {
            log.debug("Failed to get lock for trade {}", tradeId);
            return false;
        }

        log.debug("Successfully got lock for trade {}", tradeId);
        cancellationFuture.cancel(false);

        return true;
    }
}
