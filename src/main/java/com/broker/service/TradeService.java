package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.exception.TradeNotFoundException;
import com.broker.external.BrokerTrade;
import com.broker.external.BrokerTradeSide;
import com.broker.external.ExternalBroker;
import com.broker.controller.CreateTradeParam;
import com.broker.repository.TradeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Locker locker;
    private final ExternalBroker externalBroker;
    private final BrokerResponseCallbackService brokerResponseCallbackService;
    private final Duration timeout;

    public TradeService(TradeRepository tradeRepository, ScheduledExecutorService scheduledExecutorService, Locker locker,
                        ExternalBroker externalBroker, BrokerResponseCallbackService brokerResponseCallbackService,
                        @Value("${broker.service.timeout}") Duration timeout) {
        this.tradeRepository = tradeRepository;
        this.scheduledExecutorService = scheduledExecutorService;
        this.locker = locker;
        this.externalBroker = externalBroker;
        this.brokerResponseCallbackService = brokerResponseCallbackService;
        this.timeout = timeout;
    }

    @Transactional
    public Trade submitTrade(CreateTradeParam createTradeParam, BrokerTradeSide tradeSide) {
        Trade trade = new Trade();

        trade.setSide(tradeSide);

        trade.setSymbol(createTradeParam.getSymbol());
        trade.setPrice(createTradeParam.getPrice());
        trade.setQuantity(createTradeParam.getQuantity());

        trade.setStatus(ExecutionStatus.PENDING_EXECUTION);

        trade = tradeRepository.save(trade);

        return trade;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Iterable<Trade> findAll() {
        return tradeRepository.findAll();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Trade findById(String tradeId) {
        return tradeRepository.findById(UUID.fromString(tradeId), Trade.class).orElseThrow(TradeNotFoundException::new);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public TradeStatusOnly findStatusById(String tradeId) {
        return tradeRepository.findById(UUID.fromString(tradeId), TradeStatusOnly.class).orElseThrow(TradeNotFoundException::new);
    }

    public void addBrokerRequest(Trade trade) {
        UUID tradeId = trade.getId();

        Future<?> taskTimeoutFuture = scheduledExecutorService.schedule(() -> {
                if (null != locker.getSinglePermit(tradeId)) {
                    brokerResponseCallbackService.timeout(tradeId);
                }
            },
            timeout.toNanos(), TimeUnit.NANOSECONDS);

        locker.addTrade(tradeId, taskTimeoutFuture);

        scheduledExecutorService.submit(() -> externalBroker.execute(new BrokerTrade(tradeId, trade.getSymbol(), trade.getQuantity(), trade.getSide(), trade.getPrice())));
    }

}
