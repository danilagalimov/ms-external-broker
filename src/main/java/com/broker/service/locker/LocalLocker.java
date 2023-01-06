package com.broker.service.locker;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
@Profile("singleInstanceOnly")
public class LocalLocker implements Locker {
    private static final Map<UUID, Future<?>> ALL_PENDING_TRADES = new ConcurrentHashMap<>();

    @Override
    public void addTrade(UUID tradeId, Future<?> future) {
        ALL_PENDING_TRADES.put(tradeId, future);
    }

    @Override
    public Future<?> getSinglePermit(UUID tradeId) {
        return ALL_PENDING_TRADES.remove(tradeId);
    }

}
