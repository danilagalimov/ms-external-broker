package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.external.BrokerResponseCallback;
import com.broker.repository.TradeRepository;
import com.broker.service.locker.Locker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Future;

@Service
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
        if (tryAcquireLock(tradeId)) {
            tradeRepository.updateTradeStatus(ExecutionStatus.EXECUTED, tradeId);
        }
    }

    @Override
    @Transactional
    public void unsuccessful(UUID tradeId, String reason) {
        if (tryAcquireLock(tradeId)) {
            tradeRepository.updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, reason, tradeId);
        }
    }

    @Transactional
    public void timeout(UUID tradeId) {
        tradeRepository.updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, tradeTimeoutReason, tradeId);
    }

    private boolean tryAcquireLock(UUID tradeId) {
        Future<?> cancellationFuture = locker.getSinglePermit(tradeId);

        if (null == cancellationFuture) {
            return false;
        }

        cancellationFuture.cancel(false);

        return true;
    }
}
