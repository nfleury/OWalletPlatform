package com.coinwallet.rechage.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fly on 2017/11/26.
 */
@Configuration
public class RabbitRechargeConfig {
    public static final String CHECK_BALANCE_QUEUE_NAME = "check_coin_recharge";
    public static final BigDecimal CHECK_PERSONAGE_AMOUNT_UP = new BigDecimal("10000.000000000000000000");
    public static final BigDecimal CHECK_ALL_AMOUNT_UP = new BigDecimal("1000000.000000000000000000");
    public static final Integer USER_COIN_RECHARGE = new Integer("1");
    public static final Integer USER_COIN_COLLECT = new Integer("2");

    @Bean
    public Queue  checkCoinBalance() {
        return new Queue(RabbitRechargeConfig.CHECK_BALANCE_QUEUE_NAME);
    }





}



