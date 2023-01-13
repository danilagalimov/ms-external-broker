package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.repository.TradeRepository;
import com.broker.service.locker.Locker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokerResponseCallbackServiceTest {
    private static final String UNSUCCESSFUL_REASON = "unsuccessful reason";
    private static final String TIMEOUT_REASON = "timeout reason";

    private BrokerResponseCallbackService testedInstance;

    @Mock
    private Locker locker;
    @Mock
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        testedInstance = new BrokerResponseCallbackService(locker, tradeRepository, TIMEOUT_REASON);
    }

    @Test
    void testSuccessful() {
        UUID tradeId = UUID.randomUUID();

        when(locker.getSinglePermit(tradeId)).thenReturn(true);

        testedInstance.successful(tradeId);

        verify(tradeRepository).updateTradeStatus(ExecutionStatus.EXECUTED, tradeId);
        verifyNoMoreInteractions(tradeRepository);

        testedInstance.successful(UUID.randomUUID());
        verifyNoMoreInteractions(tradeRepository);
    }

    @Test
    void testUnsuccessful() {
        UUID tradeId = UUID.randomUUID();

        when(locker.getSinglePermit(tradeId)).thenReturn(true);

        testedInstance.unsuccessful(tradeId, UNSUCCESSFUL_REASON);

        verify(tradeRepository).updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, UNSUCCESSFUL_REASON, tradeId);
        verifyNoMoreInteractions(tradeRepository);

        testedInstance.unsuccessful(UUID.randomUUID(), UNSUCCESSFUL_REASON);
        verifyNoMoreInteractions(tradeRepository);

    }

    @Test
    void testTimeout() {
        UUID tradeId = UUID.randomUUID();

        when(locker.getSinglePermit(tradeId)).thenReturn(true);

        testedInstance.timeout(tradeId);

        verify(tradeRepository).updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, TIMEOUT_REASON, tradeId);

        testedInstance.timeout(UUID.randomUUID());

        verifyNoMoreInteractions(tradeRepository);
    }
}