package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.entity.TransactionOrder;
import com.coinwallet.rechage.service.AffirmRechargeToGatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AffirmRechargeToGatherReceiver {

    Logger logger = LoggerFactory.getLogger(AffirmRechargeToGatherReceiver.class);


    @Autowired
    private AffirmRechargeToGatherService affirmRechargeToGatherService;



    @RabbitListener(queues = RabbitRechargeConfig.AFFIRM_RECHARGE_TO_GATHER)
    public void rechargeToGeneralLedger(String msg) {
        try {
            logger.warn("=============AFFIRM_RECHARGE_TO_GATHER==============");
            TransactionOrder transactionOrder = JSON.parseObject(msg, TransactionOrder.class);
            affirmRechargeToGatherService.affirmRechargeCoin(transactionOrder);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }
}
