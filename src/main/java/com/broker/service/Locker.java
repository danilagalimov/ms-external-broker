package com.broker.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class Locker {
    private static final Map<UUID, Future<?>> ALL_PENDING_TRADES = new ConcurrentHashMap<>();

    public void addTrade(UUID tradeId, Future<?> future) {
        ALL_PENDING_TRADES.put(tradeId, future);
    }

    public Future<?> getSinglePermit(UUID tradeId) {
        return ALL_PENDING_TRADES.remove(tradeId);
    }

}
