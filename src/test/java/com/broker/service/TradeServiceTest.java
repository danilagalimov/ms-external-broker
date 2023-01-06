package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.Trade;
import com.broker.data.TradeStatusOnly;
import com.broker.exception.TradeNotFoundException;
import com.broker.external.BrokerTradeSide;
import com.broker.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {
    @InjectMocks
    private TradeService testedInstance;

    @Mock
    private  TradeRepository tradeRepository;
    @Mock
    private  TradeBrokerService tradeBrokerService;

    @Mock
    private TradeStatusOnly status;

    @Test
    void testSubmitTrade() {
        CreateTradeParam tradeParam = new CreateTradeParam("xx", 22L, BigDecimal.TEN);
        BrokerTradeSide tradeSide = BrokerTradeSide.BUY;
        Trade trade = new Trade();

        when(tradeBrokerService.createTrade(tradeParam, tradeSide)).thenReturn(trade);

        testedInstance.submitTrade(tradeParam, tradeSide);

        verify(tradeBrokerService).createTrade(tradeParam, tradeSide);
        verify(tradeBrokerService).addBrokerRequest(trade);
    }

    @Test
    void testFindAll() {
        Trade trade = new Trade();
        when(tradeRepository.findAll()).thenReturn(Collections.nCopies(10, trade));

        Iterable<Trade> trades = testedInstance.findAll();

        assertThat(trades, is(Collections.nCopies(10, trade)));
    }

    @Test
    void testFindById() throws TradeNotFoundException {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade();

        when(tradeRepository.findById(tradeId, Trade.class)).thenReturn(Optional.of(trade));

        assertThat(testedInstance.findById(tradeId.toString()), is(trade));

        when(tradeRepository.findById(any(), eq(Trade.class))).thenReturn(Optional.empty());

        try {
            testedInstance.findById(UUID.randomUUID().toString());

            throw new IllegalStateException("Should throw an exception");
        } catch (TradeNotFoundException expected) {
        }
    }

    @Test
    void testFindStatusById() throws TradeNotFoundException {
        UUID tradeId = UUID.randomUUID();
        when(tradeRepository.findById(tradeId, TradeStatusOnly.class)).thenReturn(Optional.of(status));

        assertThat(testedInstance.findStatusById(tradeId.toString()), is(status));

        when(tradeRepository.findById(any(), eq(TradeStatusOnly.class))).thenReturn(Optional.empty());

        try {
            testedInstance.findStatusById(UUID.randomUUID().toString());

            throw new IllegalStateException("Should throw an exception");
        } catch (TradeNotFoundException expected) {
        }

    }
}