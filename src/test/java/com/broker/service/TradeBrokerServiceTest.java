package com.broker.service;

import com.broker.controller.CreateTradeParam;
import com.broker.data.ExecutionStatus;
import com.broker.data.Trade;
import com.broker.external.BrokerTrade;
import com.broker.external.BrokerTradeSide;
import com.broker.external.ExternalBroker;
import com.broker.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeBrokerServiceTest {
    private static final String SYMBOL = "symbol";
    private static final long QUANTITY = 567;
    private static final BigDecimal PRICE = BigDecimal.valueOf(354.4);
    @InjectMocks
    private TradeBrokerService testedInstance;

    @Mock
    private  TradeRepository tradeRepository;
    @Mock
    private  ExternalBroker externalBroker;

    @Test
    void testCreateTrade() {
        when(tradeRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        Trade trade = testedInstance.createTrade(new CreateTradeParam(SYMBOL, QUANTITY, PRICE), BrokerTradeSide.SELL);

        verify(tradeRepository).save(trade);

        assertThat(trade.getReason(), is(nullValue()));
        assertThat(trade.getSymbol(), is(SYMBOL));
        assertThat(trade.getId(), is(nullValue()));
        assertThat(trade.getQuantity(), is(QUANTITY));
        assertThat(trade.getStatus(), is(ExecutionStatus.PENDING_EXECUTION));
        assertThat(trade.getPrice(), is(PRICE));
        assertThat(trade.getSide(), is(BrokerTradeSide.SELL));
        assertThat(trade.getTimestamp(), is(notNullValue()));
    }

    @Test
    void testAddBrokerRequest() {

        Trade trade = new Trade();

        trade.setId(UUID.randomUUID());
        trade.setQuantity(QUANTITY);
        trade.setSymbol(SYMBOL);
        trade.setStatus(ExecutionStatus.EXECUTED);
        trade.setPrice(PRICE);
        trade.setSide(BrokerTradeSide.BUY);

        testedInstance.addBrokerRequest(trade);

        ArgumentCaptor<BrokerTrade> argument = ArgumentCaptor.forClass(BrokerTrade.class);

        verify(externalBroker).execute(argument.capture());

        BrokerTrade brokerTrade = argument.getValue();

        assertThat(brokerTrade.getId(), is(trade.getId()));
        assertThat(brokerTrade.getSymbol(), is(SYMBOL));
        assertThat(brokerTrade.getPrice(), is(PRICE));
        assertThat(brokerTrade.getSide(), is(BrokerTradeSide.BUY));
        assertThat(brokerTrade.getQuantity(), is(QUANTITY));

        verifyNoMoreInteractions(externalBroker);
    }
}