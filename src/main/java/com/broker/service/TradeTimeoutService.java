package com.broker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

@Component
public class TradeTimeoutService {
    private final Clock clock;
    private final TemporalAmount timeout;
    private final String tradeTimeoutReason;

    public TradeTimeoutService(Clock clock, @Value("${broker.service.timeout}") Duration timeout, @Value("${broker.message.trade.expired}") String tradeTimeoutReason) {
        this.clock = clock;
        this.timeout = timeout;
        this.tradeTimeoutReason = tradeTimeoutReason;
    }

    public LocalDateTime getLastAllowedTime() {
        return LocalDateTime.now(clock).minus(timeout);
    }

    public String getTradeTimeoutReason() {
        return tradeTimeoutReason;
    }
}
