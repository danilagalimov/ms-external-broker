package com.broker.service;

import com.broker.data.ExecutionStatus;
import com.broker.repository.TradeRepository;
import com.broker.service.locker.Locker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokerResponseCallbackServiceTest {
    private static final String UNSUCCESSFUL_REASON = "unsuccessful reason";
    @InjectMocks
    private BrokerResponseCallbackService testedInstance;

    @Mock
    private Locker locker;
    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private Future future;


    @Test
    void testSuccessful() {
        UUID tradeId = UUID.randomUUID();

        when(locker.getSinglePermit(tradeId)).thenReturn(future);

        testedInstance.successful(tradeId);

        verify(tradeRepository).updateTradeStatus(ExecutionStatus.EXECUTED, tradeId);
        verifyNoMoreInteractions(tradeRepository);

        verify(future).cancel(false);

        testedInstance.successful(UUID.randomUUID());
        verifyNoMoreInteractions(tradeRepository, future);
    }

    @Test
    void testUnsuccessful() {
        UUID tradeId = UUID.randomUUID();

        when(locker.getSinglePermit(tradeId)).thenReturn(future);

        testedInstance.unsuccessful(tradeId, UNSUCCESSFUL_REASON);

        verify(tradeRepository).updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, UNSUCCESSFUL_REASON, tradeId);
        verifyNoMoreInteractions(tradeRepository);

        verify(future).cancel(false);

        testedInstance.unsuccessful(UUID.randomUUID(), UNSUCCESSFUL_REASON);
        verifyNoMoreInteractions(tradeRepository, future);

    }

    @Test
    void testTimeout() {
        UUID tradeId = UUID.randomUUID();
        testedInstance.timeout(tradeId);

        verify(tradeRepository).updateTradeStatusAndReason(ExecutionStatus.NOT_EXECUTED, null, tradeId);

        verifyNoMoreInteractions(tradeRepository, future, locker);
    }
}