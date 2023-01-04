package com.broker.config;

import com.broker.external.BrokerResponseCallback;
import com.broker.external.ExternalBroker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalBrokerConfig {

    @Bean
    public ExternalBroker createExternalBroker(BrokerResponseCallback brokerResponseCallback) {
        return new ExternalBroker(brokerResponseCallback);
    }
}
