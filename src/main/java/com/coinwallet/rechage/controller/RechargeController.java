package com.coinwallet.rechage.controller;

import com.alibaba.fastjson.JSON;
import com.coinwallet.common.apisecurity.AESCBCUtil;
import com.coinwallet.common.response.ResponseValue;
import com.coinwallet.rechage.controller.req.CreateWalletReq;
import com.coinwallet.rechage.entity.MerchantInfo;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.rabbit.RabbitOrderConfig;
import com.coinwallet.rechage.service.MerchantInfoService;
import com.coinwallet.rechage.service.RechargeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

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
                               @RequestParam(required = true) String seed) throws NoSuchAlgorithmException {
        ResponseValue responseValue = new ResponseValue();
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(merchatId);
        String jsonObject = AESCBCUtil.decrypt(in,merchantInfo.getMerchantName(),merchantInfo.getApikey(),merchantInfo.getSecurity(),seed);
        CreateWalletReq createWalletReq = JSON.parseObject(jsonObject, CreateWalletReq.class);
        rechargeService.initUserCoinWallet1(createWalletReq,merchantInfo);
        UserCoinBalance userCoinBalance = rechargeService.initUserCoinWallet(1,"prikey","addresss","OCN");
        responseValue.setData(userCoinBalance);
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
