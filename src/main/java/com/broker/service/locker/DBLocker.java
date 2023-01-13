package com.broker.service.locker;

import com.broker.data.Lock;
import com.broker.repository.LockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Scalable solution for locking (stores locks in DB)
 */
@Service
public class DBLocker implements Locker {
    private final LockRepository lockRepository;

    public DBLocker(LockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    @Override
    @Transactional
    public void addTrade(UUID tradeId) {
        lockRepository.save(new Lock(tradeId));
    }

    @Override
    @Transactional
    public boolean getSinglePermit(UUID tradeId) {
        return lockRepository.removeById(tradeId) != 0;
    }
}
