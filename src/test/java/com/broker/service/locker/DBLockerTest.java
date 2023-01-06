package com.broker.service.locker;

import com.broker.data.Lock;
import com.broker.repository.LockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DBLockerTest {
    @InjectMocks
    private DBLocker testedInstance;

    @Mock
    private LockRepository lockRepository;
    @Test
    void testAddTrade() {
        ArgumentCaptor<Lock> lock = ArgumentCaptor.forClass(Lock.class);
        when(lockRepository.save(lock.capture())).thenReturn(null);

        UUID tradeId = UUID.randomUUID();
        testedInstance.addTrade(tradeId, new CompletableFuture<>());


        verify(lockRepository).save(lock.getValue());
    }

    @Test
    void testGetSinglePermit() {
        UUID tradeId = UUID.randomUUID();

        assertThat(testedInstance.getSinglePermit(tradeId), is(nullValue()));

        when(lockRepository.removeById(tradeId)).thenReturn(1L);
        assertThat(testedInstance.getSinglePermit(tradeId), is(notNullValue()));
    }
}