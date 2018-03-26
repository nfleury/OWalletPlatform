package com.coinwallet.rechage.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created by fly on 2017/11/26.
 */
@Configuration
public class RabbitRechargeConfig {
    public static final String CHECK_BALANCE = "check_balance";//用户确认成功
    public static final String RECHARGE_GAS_TO_USER_ACCOUNT = "recharge_gas_to_user_account";//充值gas费用到用户账上
    public static final String RECHARGE_COIN_TO_GATHER_ACCOUNT = "recharge_coin_to_gather_account";//提币到总账
    public static final String SCAN_BLOCK_ORDER = "scan_block_order";//扫描区块确认的订单
    public static final String CHECK_NODE_RECHARGE = "check_node_recharge";//监听节点符合合约地址的去记录订单
    public static final String AFFIRM_RECHARGE_TO_GATHER = "affirm_recharge_to_gather";//确认提币成功,记录user_coin_log日志
    public static final String AFFIRM_PLATFORM_TRANSFER = "affirm_platform_transfer";//确认第三方平台转账成功


    @Bean
    public Queue checkCoinBalance() {
        return new Queue(RabbitRechargeConfig.CHECK_BALANCE);
    }

    @Bean
    public Queue rechargeGasToUserAccount() {
        return new Queue(RabbitRechargeConfig.RECHARGE_GAS_TO_USER_ACCOUNT);
    }

    @Bean
    public Queue scanBlockOrder() {
        return new Queue(RabbitRechargeConfig.SCAN_BLOCK_ORDER);
    }

    @Bean
    public Queue checkNodeRecharge() {
        return new Queue(RabbitRechargeConfig.CHECK_NODE_RECHARGE);
    }


    @Bean
    public Queue rechargeCoinToGatherAccount() {
        return new Queue(RabbitRechargeConfig.RECHARGE_COIN_TO_GATHER_ACCOUNT);
    }

    @Bean
    public Queue affirmRechargeToGather() {
        return new Queue(RabbitRechargeConfig.AFFIRM_RECHARGE_TO_GATHER);
    }

    @Bean
    public Queue affirmPlatformTransfer() {
        return new Queue(RabbitRechargeConfig.AFFIRM_PLATFORM_TRANSFER);
    }

    @Bean(name = "myConnectionFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(5);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  //模式设置成不需要ack的模式
        factory.setPrefetchCount(2);
        return factory;
    }

}



