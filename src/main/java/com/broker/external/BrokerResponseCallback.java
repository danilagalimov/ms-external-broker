package com.broker.external;

import java.util.UUID;

public interface BrokerResponseCallback {

    void successful(final UUID tradeId);

    void unsuccessful(final UUID tradeId, final String reason);
}
