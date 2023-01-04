package com.broker.external;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.UUID;

public class BrokerTrade {

    private final UUID id;
    private final String symbol;
    private final long quantity;
    private final BrokerTradeSide side;
    private final BigDecimal price;

    public BrokerTrade(UUID id, String symbol, long quantity, BrokerTradeSide side, BigDecimal price) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.side = side;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public BrokerTradeSide getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        BrokerTrade mtTrade = (BrokerTrade) o;

        return new EqualsBuilder().append(quantity, mtTrade.quantity)
            .append(id, mtTrade.id)
            .append(symbol, mtTrade.symbol)
            .append(side, mtTrade.side)
            .append(price, mtTrade.price)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id)
            .append(symbol)
            .append(quantity)
            .append(side)
            .append(price)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("symbol", symbol)
            .append("quantity", quantity)
            .append("side", side)
            .append("price", price)
            .toString();
    }
}
