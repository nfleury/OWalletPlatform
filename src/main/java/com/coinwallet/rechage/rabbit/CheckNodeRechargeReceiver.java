package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.service.CheckRechargeOrderOnNodeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Transaction;


/**
 * Created by liuhuan on 2018/3/15.
 */
@Component
public class CheckNodeRechargeReceiver {

    @Autowired
    private CheckRechargeOrderOnNodeService checkRechargeOrderOnNodeService;



    /**
     *
     * 扫描节点获得交易信息,记录peeding状态订单
     *
     * @param msg
     */
    @RabbitListener(queues = RabbitRechargeConfig.CHECK_NODE_RECHARGE)
    public void checkNodeRecharge(String msg) {
        try {
            Transaction transaction = JSON.parseObject(msg, Transaction.class);
            checkRechargeOrderOnNodeService.checkNodeRecharge(transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
