package com.broker.controller;

import com.broker.data.Trade;
import com.broker.external.BrokerTradeSide;
import com.broker.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(TradeSubmitController.BASE_API_URL)
public class TradeSubmitController {
    protected static final String BASE_API_URL = "/api";
    private static final String TRADE_SELL_URL = "/sell";
    private static final String TRADE_BUY_URL = "/buy";

    private final TradeService tradeService;

    public TradeSubmitController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(TRADE_SELL_URL)
    public ResponseEntity<Void> sell(@RequestBody @Valid CreateTradeParam createTradeParam, UriComponentsBuilder uriBuilder) {
        return submitTrade(createTradeParam, uriBuilder, BrokerTradeSide.SELL);
    }

    @PostMapping(TRADE_BUY_URL)
    public ResponseEntity<Void> buy(@RequestBody @Valid CreateTradeParam createTradeParam, UriComponentsBuilder uriBuilder) {
        return submitTrade(createTradeParam, uriBuilder, BrokerTradeSide.BUY);
    }

    private ResponseEntity<Void> submitTrade(
            CreateTradeParam createTradeParam,
            UriComponentsBuilder uriBuilder,
            BrokerTradeSide tradeSide) {
        Trade trade = tradeService.submitTrade(createTradeParam, tradeSide);

        UriComponents uriComponents = uriBuilder
                .path(BASE_API_URL)
                .path(TradeViewController.TRADE_BY_TRADE_ID_URL)
                .buildAndExpand(trade.getId());

        return ResponseEntity.created(uriComponents.toUri()).build();
    }
}