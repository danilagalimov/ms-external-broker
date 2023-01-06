package com.broker.service.locker;

import java.util.UUID;
import java.util.concurrent.Future;

public interface Locker {
    void addTrade(UUID tradeId, Future<?> future);

    Future<?> getSinglePermit(UUID tradeId);
}
