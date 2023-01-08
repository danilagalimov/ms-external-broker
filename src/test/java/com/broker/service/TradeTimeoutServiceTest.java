package com.broker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeTimeoutServiceTest {
    private static final String TIMEOUT_REASON = "Timeout reason";
    private TradeTimeoutService testedInstance;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final Duration timeout = Duration.parse("P2DT3H4M");

    @BeforeEach
    void setUp() {
        testedInstance = new TradeTimeoutService(clock, timeout, TIMEOUT_REASON);
    }

    @Test
    void TestGetLastAllowedTime() {
        LocalDateTime lastAllowedTime = testedInstance.getLastAllowedTime();
        lastAllowedTime = lastAllowedTime.plus(timeout);
        assertEquals(clock.instant(), lastAllowedTime.toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())));
    }

    @Test
    void testGetTradeTimeoutReason() {
        assertEquals(TIMEOUT_REASON, testedInstance.getTradeTimeoutReason());
    }
}