package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.entity.*;
import com.coinwallet.rechage.service.RechargeCoinToGatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class RechargeCoinToGatherReceiver {

    Logger logger = LoggerFactory.getLogger(RechargeCoinToGatherReceiver.class);

    @Autowired
    private RechargeCoinToGatherService rechargeCoinToGatherService;


    @RabbitListener(queues = RabbitRechargeConfig.RECHARGE_COIN_TO_GATHER_ACCOUNT)
    public void rechargeToGeneralLedger(String msg) {
        try {

            logger.warn("=============RECHARGE_COIN_TO_GATHER_ACCOUNT======start========");
            TransactionOrder transactionOrder = JSON.parseObject(msg, TransactionOrder.class);
            rechargeCoinToGatherService.rechargeCoin(transactionOrder);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }



}



