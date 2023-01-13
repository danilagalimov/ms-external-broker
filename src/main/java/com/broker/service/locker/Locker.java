package com.broker.service.locker;

import java.util.UUID;

public interface Locker {
    void addTrade(UUID tradeId);

    boolean getSinglePermit(UUID tradeId);
}
