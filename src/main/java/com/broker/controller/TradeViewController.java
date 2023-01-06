package com.broker.controller;

import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TradeViewController.BASE_API_URL)
public class TradeViewController {
    protected static final String BASE_API_URL = "/api";
    protected static final String TRADE_BY_TRADE_ID_URL = "/trades/{tradeId}";

    private final TradeService tradeService;

    public TradeViewController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/trades")
    public Iterable<Trade> findAll() {
        return tradeService.findAll();
    }

    @GetMapping(TRADE_BY_TRADE_ID_URL)
    public Trade findTradeById(@PathVariable String tradeId) {
        return tradeService.findById(tradeId);
    }

    @GetMapping("/trades/{tradeId}/status")
    public TradeStatusOnly findStatusById(@PathVariable String tradeId) {
        return tradeService.findStatusById(tradeId);
    }

}