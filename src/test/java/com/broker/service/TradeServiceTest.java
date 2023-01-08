package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.ExecutionStatus;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {
    private static final String TIMEOUT_REASON = "timeout reason";
    @InjectMocks
    private TradeService testedInstance;

    @Mock
    private  TradeRepository tradeRepository;
    @Mock
    private  TradeBrokerService tradeBrokerService;
    @Mock
    private TradeTimeoutService tradeTimeoutService;

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
        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MIN);
        when(tradeTimeoutService.getTradeTimeoutReason()).thenReturn(TIMEOUT_REASON);

        Trade executedTrade = createTradeWithStatus(ExecutionStatus.EXECUTED);
        Trade notExecutedTrade = createTradeWithStatus(ExecutionStatus.NOT_EXECUTED);
        Trade pendingTrade = createTradeWithStatus(ExecutionStatus.PENDING_EXECUTION);
        when(tradeRepository.findAll()).thenReturn(Arrays.asList(executedTrade, notExecutedTrade, pendingTrade));

        Iterable<Trade> trades = testedInstance.findAll();

        assertThat(trades, is(Arrays.asList(executedTrade, notExecutedTrade, pendingTrade)));

        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.PENDING_EXECUTION));
        assertThat(pendingTrade.getReason(), is(nullValue()));

        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MAX);

        trades = testedInstance.findAll();

        assertThat(trades, is(Arrays.asList(executedTrade, notExecutedTrade, pendingTrade)));
        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(pendingTrade.getReason(), is(TIMEOUT_REASON));
    }

    @Test
    void testFindById() throws TradeNotFoundException {
        when(tradeRepository.findById(any())).thenReturn(Optional.empty());

        try {
            testedInstance.findById(UUID.randomUUID().toString());

            throw new IllegalStateException("Should throw an exception");
        } catch (TradeNotFoundException expected) {
        }

        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MIN);
        when(tradeTimeoutService.getTradeTimeoutReason()).thenReturn(TIMEOUT_REASON);

        Trade executedTrade = createTradeWithStatus(ExecutionStatus.EXECUTED);

        when(tradeRepository.findById(executedTrade.getId())).thenReturn(Optional.of(executedTrade));
        assertThat(testedInstance.findById(executedTrade.getId().toString()), is(executedTrade));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));

        Trade notExecutedTrade = createTradeWithStatus(ExecutionStatus.NOT_EXECUTED);
        when(tradeRepository.findById(notExecutedTrade.getId())).thenReturn(Optional.of(notExecutedTrade));
        assertThat(testedInstance.findById(notExecutedTrade.getId().toString()), is(notExecutedTrade));
        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));

        Trade pendingTrade = createTradeWithStatus(ExecutionStatus.PENDING_EXECUTION);
        when(tradeRepository.findById(pendingTrade.getId())).thenReturn(Optional.of(pendingTrade));
        assertThat(testedInstance.findById(pendingTrade.getId().toString()), is(pendingTrade));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.PENDING_EXECUTION));
        assertThat(pendingTrade.getReason(), is(nullValue()));

        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MAX);

        assertThat(testedInstance.findById(executedTrade.getId().toString()), is(executedTrade));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));

        assertThat(testedInstance.findById(notExecutedTrade.getId().toString()), is(notExecutedTrade));
        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));

        assertThat(testedInstance.findById(pendingTrade.getId().toString()), is(pendingTrade));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(pendingTrade.getReason(), is(TIMEOUT_REASON));
    }

    @Test
    void testFindStatusById() throws TradeNotFoundException {
        when(tradeRepository.findById(any())).thenReturn(Optional.empty());

        try {
            testedInstance.findStatusById(UUID.randomUUID().toString());

            throw new IllegalStateException("Should throw an exception");
        } catch (TradeNotFoundException expected) {
        }

        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MIN);
        when(tradeTimeoutService.getTradeTimeoutReason()).thenReturn(TIMEOUT_REASON);

        Trade executedTrade = createTradeWithStatus(ExecutionStatus.EXECUTED);

        when(tradeRepository.findById(executedTrade.getId())).thenReturn(Optional.of(executedTrade));
        assertThat(testedInstance.findStatusById(executedTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.EXECUTED)));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));

        Trade notExecutedTrade = createTradeWithStatus(ExecutionStatus.NOT_EXECUTED);
        when(tradeRepository.findById(notExecutedTrade.getId())).thenReturn(Optional.of(notExecutedTrade));
        assertThat(testedInstance.findStatusById(notExecutedTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.NOT_EXECUTED)));
        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));

        Trade pendingTrade = createTradeWithStatus(ExecutionStatus.PENDING_EXECUTION);
        when(tradeRepository.findById(pendingTrade.getId())).thenReturn(Optional.of(pendingTrade));
        assertThat(testedInstance.findStatusById(pendingTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.PENDING_EXECUTION)));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.PENDING_EXECUTION));
        assertThat(pendingTrade.getReason(), is(nullValue()));

        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(LocalDateTime.MAX);

        assertThat(testedInstance.findStatusById(executedTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.EXECUTED)));
        assertThat(executedTrade.getStatus(), is(ExecutionStatus.EXECUTED));
        assertThat(executedTrade.getReason(), is(nullValue()));

        assertThat(testedInstance.findStatusById(notExecutedTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.NOT_EXECUTED)));
        assertThat(notExecutedTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(notExecutedTrade.getReason(), is(nullValue()));

        assertThat(testedInstance.findStatusById(pendingTrade.getId().toString()), is(new TradeStatusOnly(ExecutionStatus.NOT_EXECUTED)));
        assertThat(pendingTrade.getStatus(), is(ExecutionStatus.NOT_EXECUTED));
        assertThat(pendingTrade.getReason(), is(TIMEOUT_REASON));

    }

    private Trade createTradeWithStatus(ExecutionStatus status) {
        Trade trade = new Trade();

        trade.setId(UUID.randomUUID());

        trade.setTimestamp(LocalDateTime.now());
        trade.setStatus(status);

        return trade;
    }

}