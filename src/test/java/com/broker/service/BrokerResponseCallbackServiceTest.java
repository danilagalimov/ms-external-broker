package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokerResponseCallbackServiceTest {
    private static final String UNSUCCESSFUL_REASON = "unsuccessful reason";
    @InjectMocks
    private BrokerResponseCallbackService testedInstance;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeTimeoutService tradeTimeoutService;


    @Test
    void testSuccessful() {
        LocalDateTime executionTime = LocalDateTime.MIN;
        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(executionTime);

        UUID tradeId = UUID.randomUUID();

        testedInstance.successful(tradeId);

        verify(tradeRepository).updateTradeStatus(ExecutionStatus.EXECUTED, tradeId, ExecutionStatus.PENDING_EXECUTION, executionTime);
        verifyNoMoreInteractions(tradeRepository);
    }

    @Test
    void testUnsuccessful() {
        LocalDateTime executionTime = LocalDateTime.MAX;
        when(tradeTimeoutService.getLastAllowedTime()).thenReturn(executionTime);

        UUID tradeId = UUID.randomUUID();

        testedInstance.unsuccessful(tradeId, UNSUCCESSFUL_REASON);

        verify(tradeRepository).updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, UNSUCCESSFUL_REASON, tradeId, ExecutionStatus.PENDING_EXECUTION, executionTime);
        verifyNoMoreInteractions(tradeRepository);
    }
}