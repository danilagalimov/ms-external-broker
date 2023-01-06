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
@RequestMapping("/api")
public class TradeSubmitController {
    private final TradeService tradeService;

    public TradeSubmitController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sell(@RequestBody @Valid CreateTradeParam createTradeParam, UriComponentsBuilder uriBuilder) {
        return submitTrade(createTradeParam, uriBuilder, BrokerTradeSide.SELL);
    }

    @PostMapping("/buy")
    public ResponseEntity<Void> buy(@RequestBody @Valid CreateTradeParam createTradeParam, UriComponentsBuilder uriBuilder) {
        return submitTrade(createTradeParam, uriBuilder, BrokerTradeSide.BUY);
    }

    private ResponseEntity<Void> submitTrade(
            CreateTradeParam createTradeParam,
            UriComponentsBuilder uriBuilder,
            BrokerTradeSide tradeSide) {
        Trade trade = tradeService.submitTrade(createTradeParam, tradeSide);

        UriComponents uriComponents = uriBuilder
                .path(TradeViewController.BASE_API_URL)
                .path(TradeViewController.TRADE_BY_TRADE_ID_URL)
                .buildAndExpand(trade.getId());

        return ResponseEntity.created(uriComponents.toUri()).build();
    }
}