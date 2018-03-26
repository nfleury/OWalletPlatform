package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.entity.*;
import com.coinwallet.rechage.service.CheckSuccessRechargeOrderService;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CheckBalanceReceiver {

    Logger logger = org.slf4j.LoggerFactory.getLogger(CheckBalanceReceiver.class);


    @Autowired
    private CheckSuccessRechargeOrderService checkSuccessRechargeOrderService;

    /**
     * 用户充值成功,修改余额,记录日志
     * 当此用户coin_balance大于提币上限去给获取gas费用 然后提币到总账
     * 回调第三方充值成功
     *
     * @param msg
     */
    @RabbitListener(queues = RabbitRechargeConfig.CHECK_BALANCE,containerFactory = "myConnectionFactory")
    public void coinCharge(String msg) {
        try {
            TransactionOrder transactionOrder = JSON.parseObject(msg, TransactionOrder.class);
            checkSuccessRechargeOrderService.checkRechargeOrder(transactionOrder);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }




}
