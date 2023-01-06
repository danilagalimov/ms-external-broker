package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.exception.TradeNotFoundException;
import com.broker.external.BrokerTradeSide;
import com.broker.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
public class TradeService {
    private final TradeRepository tradeRepository;
    private final TradeBrokerService tradeBrokerService;

    public TradeService(TradeRepository tradeRepository, TradeBrokerService tradeBrokerService) {
        this.tradeRepository = tradeRepository;
        this.tradeBrokerService = tradeBrokerService;
    }

    public Trade submitTrade(CreateTradeParam createTradeParam, BrokerTradeSide tradeSide) {
        Trade newTrade = tradeBrokerService.createTrade(createTradeParam, tradeSide);
        log.debug("Create a new trade {}", newTrade);

        tradeBrokerService.addBrokerRequest(newTrade);
        return newTrade;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Iterable<Trade> findAll() {
        return tradeRepository.findAll();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Trade findById(String tradeId) throws TradeNotFoundException {
        log.debug("Trying to find trade with id {}", tradeId);
        return tradeRepository.findById(UUID.fromString(tradeId), Trade.class)
                        .orElseThrow(TradeNotFoundException::new);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public TradeStatusOnly findStatusById(String tradeId) throws TradeNotFoundException {
        log.debug("Trying to find status for trade with id {}", tradeId);
        return tradeRepository.findById(UUID.fromString(tradeId), TradeStatusOnly.class)
                        .orElseThrow(TradeNotFoundException::new);
    }


}
