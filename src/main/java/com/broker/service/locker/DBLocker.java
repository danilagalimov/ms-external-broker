package com.broker.service.locker;

import com.broker.data.Lock;
import com.broker.repository.LockRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@Profile("!singleInstanceOnly")
public class DBLocker implements Locker {
    private final LockRepository lockRepository;
    private final Future<?> dummy = new CompletableFuture<>() {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }
    };

    public DBLocker(LockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    @Override
    @Transactional
    public void addTrade(UUID tradeId, Future<?> future) {
        lockRepository.save(new Lock(tradeId));
    }

    @Override
    @Transactional
    public Future<?> getSinglePermit(UUID tradeId) {
        boolean isDeleted = lockRepository.removeById(tradeId) != 0;

        return isDeleted ? dummy : null;
    }
}
