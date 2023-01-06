package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import com.broker.external.BrokerTrade;
import com.broker.external.BrokerTradeSide;
import com.broker.external.ExternalBroker;
import com.broker.repository.TradeRepository;
import com.broker.service.locker.Locker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TradeBrokerService {
    private final TradeRepository tradeRepository;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Locker locker;
    private final ExternalBroker externalBroker;
    private final BrokerResponseCallbackService brokerResponseCallbackService;
    private final Duration timeout;

    public TradeBrokerService(TradeRepository tradeRepository, ScheduledExecutorService scheduledExecutorService,
                              Locker locker, ExternalBroker externalBroker, BrokerResponseCallbackService brokerResponseCallbackService,
                              @Value("${broker.service.timeout}") Duration timeout) {
        this.tradeRepository = tradeRepository;
        this.scheduledExecutorService = scheduledExecutorService;
        this.locker = locker;
        this.externalBroker = externalBroker;
        this.brokerResponseCallbackService = brokerResponseCallbackService;
        this.timeout = timeout;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Trade createTrade(CreateTradeParam createTradeParam, BrokerTradeSide tradeSide) {
        Trade trade = new Trade();

        trade.setSide(tradeSide);

        trade.setSymbol(createTradeParam.getSymbol());
        trade.setPrice(createTradeParam.getPrice());
        trade.setQuantity(createTradeParam.getQuantity());

        trade.setStatus(ExecutionStatus.PENDING_EXECUTION);

        trade = tradeRepository.save(trade);

        return trade;
    }

    @Async
    public void addBrokerRequest(Trade trade) {
        UUID tradeId = trade.getId();

        Future<?> taskTimeoutFuture = scheduledExecutorService.schedule(() -> {
                    if (null != locker.getSinglePermit(tradeId)) {
                        brokerResponseCallbackService.timeout(tradeId);
                    }
                },
                timeout.toNanos(), TimeUnit.NANOSECONDS);

        locker.addTrade(tradeId, taskTimeoutFuture);

        externalBroker.execute(new BrokerTrade(tradeId, trade.getSymbol(), trade.getQuantity(), trade.getSide(), trade.getPrice()));
    }

}
