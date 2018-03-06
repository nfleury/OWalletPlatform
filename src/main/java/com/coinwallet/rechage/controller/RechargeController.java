package com.coinwallet.rechage.controller;

import com.coinwallet.common.response.ResponseValue;
import com.coinwallet.rechage.entity.MerchantInfo;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.rabbit.RabbitOrderConfig;
import com.coinwallet.rechage.service.MerchantInfoService;
import com.coinwallet.rechage.service.RechargeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    @Autowired
    RechargeService rechargeService;

    @Autowired
    MerchantInfoService merchantInfoService;


    @Autowired(required=true)
    RedisTemplate redisTemplate;


    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 1001:创建钱包
     * localhost:9005/api/recharge/v1/create-wallet
      * @return
     * @throws Exception
     */
    @RequestMapping(value="/v1/create-wallet",method = RequestMethod.POST)
    public @ResponseBody
    ResponseValue createWallet(@RequestParam(required = true) Integer merchatId,
                               @RequestParam(required = true) String in,
                               @RequestParam(required = true) String seed)   {
        ResponseValue responseValue = new ResponseValue();
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(merchatId);

        UserCoinBalance userCoinBalance = rechargeService.initUserCoinWallet(1,"prikey","addresss","OCN");
        responseValue.setData(userCoinBalance);
        System.out.println("1111");
        return responseValue;
    }


    /**
     * localhost:9005/api/recharge/v1/test
     * @return
     */
    @RequestMapping(value="/v1/test",method = RequestMethod.GET)
    public @ResponseBody
    ResponseValue test()   {
        ResponseValue responseValue = new ResponseValue();
        redisTemplate.opsForValue().set("a","ddddddddddddd");
        System.out.println(redisTemplate.opsForValue().get("a"));
        rabbitTemplate.convertAndSend(RabbitOrderConfig.CHECK_BALANCE_QUEUE_NAME,"hello msg");
        responseValue.setData("fgggf");

        return responseValue;
    }







}
