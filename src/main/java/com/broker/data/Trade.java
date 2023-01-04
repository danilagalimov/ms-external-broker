package com.broker.data;

import com.broker.external.BrokerTradeSide;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    String symbol;
    long quantity;
    BrokerTradeSide side;
    BigDecimal price;

    ExecutionStatus status;
    String reason;
}
