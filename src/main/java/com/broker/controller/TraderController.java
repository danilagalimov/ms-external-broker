package com.broker.controller;

import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.external.BrokerTradeSide;
import com.broker.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(TraderController.BASE_API_URL)
public class TraderController {
    protected static final String BASE_API_URL = "/api";
    private static final String ALL_TRADES_URL = "/trades";
    private static final String TRADE_BY_TRADE_ID_URL = "/trades/{tradeId}";
    private static final String TRADE_STATUS_BY_TRADE_ID_URL = "/trades/{tradeId}/status";
    private static final String TRADE_BUY_SELL_URL = "/{tradeType:^buy$|^sell$}";

    private final TradeService tradeService;

    public TraderController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(TRADE_BUY_SELL_URL)
    public ResponseEntity<Void> doBuySell(@RequestBody @Valid CreateTradeParam createTradeParam, @PathVariable String tradeType, UriComponentsBuilder uriBuilder) {
        Trade trade = tradeService.submitTrade(createTradeParam, BrokerTradeSide.valueOf(tradeType.toUpperCase()));
        tradeService.addBrokerRequest(trade);

        UriComponents uriComponents = uriBuilder
                .path(BASE_API_URL)
                .path(TRADE_BY_TRADE_ID_URL)
                .buildAndExpand(trade.getId());

        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @GetMapping(ALL_TRADES_URL)
    public Iterable<Trade> findAll() {
        return tradeService.findAll();
    }

    @GetMapping(TRADE_BY_TRADE_ID_URL)
    public Trade findTradeById(@PathVariable String tradeId) {
        return tradeService.findById(tradeId);
    }

    @GetMapping(TRADE_STATUS_BY_TRADE_ID_URL)
    public TradeStatusOnly findStatusById(@PathVariable String tradeId) {
        return tradeService.findStatusById(tradeId);
    }

}