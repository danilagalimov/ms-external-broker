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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String symbol;
    private long quantity;
    private BrokerTradeSide side;
    private BigDecimal price;

    private ExecutionStatus status;
    private String reason;
    private LocalDateTime timestamp;
}
