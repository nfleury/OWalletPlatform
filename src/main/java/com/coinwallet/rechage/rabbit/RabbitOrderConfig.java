package com.coinwallet.rechage.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fly on 2017/11/26.
 */
@Configuration
public class RabbitOrderConfig {
    public static final String CHECK_BALANCE_QUEUE_NAME = "check_coin_balance";
    @Bean
    public Queue  checkCoinBalance() {
        return new Queue(RabbitOrderConfig.CHECK_BALANCE_QUEUE_NAME);
    }





}



