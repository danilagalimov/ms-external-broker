package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.exception.TradeNotFoundException;
import com.broker.external.BrokerTradeSide;
import com.broker.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Log4j2
public class TradeService {
    private final TradeRepository tradeRepository;
    private final TradeBrokerService tradeBrokerService;
    private final TradeTimeoutService tradeTimeoutService;

    public TradeService(TradeRepository tradeRepository, TradeBrokerService tradeBrokerService, TradeTimeoutService tradeTimeoutService) {
        this.tradeRepository = tradeRepository;
        this.tradeBrokerService = tradeBrokerService;
        this.tradeTimeoutService = tradeTimeoutService;
    }

    public Trade submitTrade(CreateTradeParam createTradeParam, BrokerTradeSide tradeSide) {
        Trade newTrade = tradeBrokerService.createTrade(createTradeParam, tradeSide);
        log.debug("Create a new trade {}", newTrade);

        tradeBrokerService.addBrokerRequest(newTrade);
        return newTrade;
    }

    @Transactional()
    public Iterable<Trade> findAll() {
        List<Trade> allTrades = tradeRepository.findAll();

        allTrades.forEach(createProcessor());

        log.debug("Found {} trades total", allTrades.size());
        return allTrades;
    }

    @Transactional()
    public Trade findById(String tradeId) throws TradeNotFoundException {
        log.debug("Trying to find trade with id {}", tradeId);
        Optional<Trade> trade = tradeRepository.findById(UUID.fromString(tradeId));

        trade.ifPresent(createProcessor());

        return trade.orElseThrow(TradeNotFoundException::new);
    }

    @Transactional()
    public TradeStatusOnly findStatusById(String tradeId) throws TradeNotFoundException {
        log.debug("Trying to find status for trade with id {}", tradeId);
        return new TradeStatusOnly(findById(tradeId).getStatus());
    }

    /**
     * Creates a function which checks if the trade is outdated, and if is, updates trade attributes to indicate this.
     * Method was created to use the single 'lastAllowedTime' value for all trades in the single request.
     */
    private Consumer<Trade> createProcessor() {
        LocalDateTime lastAllowedTime = tradeTimeoutService.getLastAllowedTime();

        return trade -> {
            if (trade.getTimestamp().isBefore(lastAllowedTime) && trade.getStatus() == ExecutionStatus.PENDING_EXECUTION) {
                log.debug("Marking trade {} as timed out", trade.getId());
                trade.setStatus(ExecutionStatus.NOT_EXECUTED);
                trade.setReason(tradeTimeoutService.getTradeTimeoutReason());
            }
        };
    }

}
