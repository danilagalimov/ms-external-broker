package com.broker.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateTradeParam {
    private static final String QUANTIY_ERROR_MESSAGE = "must be greater than 0 and less than or equal to 1M";
    private static final String SYMBOL_ERROR_MESSAGE = "valid values: USD/JPY, EUR/USD";

    @NotNull
    @Pattern(regexp = "^USD/JPY$|^EUR/USD$", message = SYMBOL_ERROR_MESSAGE)
    String symbol;

    @NotNull
    @Positive(message = QUANTIY_ERROR_MESSAGE)
    @Max(value = 1_000_000, message = QUANTIY_ERROR_MESSAGE)
    Long quantity;

    @NotNull
    @Positive
    BigDecimal price;
}
