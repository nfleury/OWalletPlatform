package com.coinwallet.rechage.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class CheckBalanceReceiver {



    @RabbitListener(queues= RabbitOrderConfig.CHECK_BALANCE_QUEUE_NAME) //未支付  下单成加入队列
    public void noPayedOrder(String msg) {
        long orderNo=0;
        try {
            System.out.println("receive msg="+msg);
        }catch (Exception e){

        }

    }

}
