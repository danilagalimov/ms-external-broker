package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import com.broker.external.BrokerTrade;
import com.broker.external.BrokerTradeSide;
import com.broker.external.ExternalBroker;
import com.broker.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Log4j2
public class TradeBrokerService {
    private final TradeRepository tradeRepository;
    private final ExternalBroker externalBroker;

    public TradeBrokerService(TradeRepository tradeRepository, ExternalBroker externalBroker) {
        this.tradeRepository = tradeRepository;

        this.externalBroker = externalBroker;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Trade createTrade(CreateTradeParam createTradeParam, BrokerTradeSide tradeSide) {
        Trade trade = new Trade();

        trade.setSide(tradeSide);

        trade.setSymbol(createTradeParam.getSymbol());
        trade.setPrice(createTradeParam.getPrice());
        trade.setQuantity(createTradeParam.getQuantity());

        trade.setStatus(ExecutionStatus.PENDING_EXECUTION);
        trade.setTimestamp(LocalDateTime.now());

        trade = tradeRepository.save(trade);

        return trade;
    }

    @Async
    public void addBrokerRequest(Trade trade) {
        UUID tradeId = trade.getId();

        log.debug("Sending trade {} to external broker", tradeId);
        externalBroker.execute(new BrokerTrade(tradeId, trade.getSymbol(), trade.getQuantity(), trade.getSide(), trade.getPrice()));
    }

}
