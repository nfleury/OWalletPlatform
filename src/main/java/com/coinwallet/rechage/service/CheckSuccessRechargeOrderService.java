package com.coinwallet.rechage.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coinwallet.common.apisecurity.AESCBC;
import com.coinwallet.common.apisecurity.AESCBCUtil;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.util.HttpClient;
import com.coinwallet.common.util.HttpRequestUtil;
import com.coinwallet.common.util.MySSLProtocolSocketFactory;
import com.coinwallet.rechage.dao.*;
import com.coinwallet.rechage.entity.*;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fly on 18/3/23.
 */
@Service
public class CheckSuccessRechargeOrderService {

    Logger logger = org.slf4j.LoggerFactory.getLogger(CheckSuccessRechargeOrderService.class);


    @Autowired
    private UserWalletInfoMapper userWalletInfoMapper;

    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;


    @Autowired
    private CoinInfoMapper coinInfoMapper;

    @Autowired
    private UserCoinLogMapper userCoinLogMapper;


    @Autowired
    private TransactionOrderMapper transactionOrderMapper;


    @Autowired
    private MerchantInfoService merchantInfoService;


    @Autowired
    RabbitTemplate rabbitTemplate;


    public void checkRechargeOrder(TransactionOrder transactionOrder) throws Exception {
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectWalletInfoByAddress(transactionOrder.getToAddress());
        String txHash = transactionOrder.getTxHash();

        if (userWalletInfo != null) {
            //修改交易状态
            int res = this.changeTransactionSuccess(transactionOrder);
            logger.warn("============updatetransactionOrder==============" + res);

            if (res > 0) {
                confirmRecharge(transactionOrder, userWalletInfo, txHash);

            }


        } else {
            //直接把状态改为成功
            this.changeTransactionSuccess(transactionOrder);
        }
    }


    /**
     * 修改交易状态
     *
     * @param transactionOrder
     */
    public int changeTransactionSuccess(TransactionOrder transactionOrder) {
        //todo   把交易状态修改为成功 放在最后
        TransactionOrder updateOrder = new TransactionOrder();
        updateOrder.setTxHash(transactionOrder.getTxHash());
        updateOrder.setUsedGas(transactionOrder.getUsedGas());
        updateOrder.setTradingTime(transactionOrder.getTradingTime());
        updateOrder.setFee(transactionOrder.getFee());
        updateOrder.setOrderStatus(Constants.ORDER_STATUS_SUCCESS);
        updateOrder.setUpdatedTime(new Date());
        return transactionOrderMapper.updateByPrimaryKeyAndOrderStatus(updateOrder);

    }


    public void confirmRecharge(TransactionOrder transactionOrder, UserWalletInfo userWalletInfo, String txHash) throws Exception {
        //用户UserCoinBalance存在修改余额，不存在创建  记录用户充值记录
        logger.warn("============USERCOINBALANCE==============" + txHash);
        UserCoinBalance userCoinBalance = getUserCoinBalance(transactionOrder, userWalletInfo, txHash);
        //满足条件的用户去获得邮费并提币到总账
        meetConditionsUserCoinBalance(transactionOrder, userWalletInfo, userCoinBalance);
        //回调通知
        callBack(transactionOrder, userWalletInfo, userCoinBalance, transactionOrder.getCoinNum());
    }


    /**
     * 满足条件的用户去获得邮费并提币到总账
     *
     * @param transactionOrder
     * @param userWalletInfo
     * @param userCoinBalance
     */
    private void meetConditionsUserCoinBalance(TransactionOrder transactionOrder, UserWalletInfo userWalletInfo, UserCoinBalance userCoinBalance) {
        CoinInfo coinInfo = coinInfoMapper.selectByPrimaryKey(transactionOrder.getCoinId());
        logger.warn("============GAS==============" + transactionOrder.getTxHash());
        if (new BigDecimal(coinInfo.getCoinHigherLimit().toString()).compareTo(userCoinBalance.getCoinBalance()) <= 0) {
            //去获取邮费
            TransactionMsg transactionMsg = new TransactionMsg();
            transactionMsg.setUserCoinBalance(userCoinBalance);
            transactionMsg.setUserWalletInfo(userWalletInfo);
            String toTransferMsg = JSON.toJSONString(transactionMsg);
            rabbitTemplate.convertAndSend(RabbitRechargeConfig.RECHARGE_GAS_TO_USER_ACCOUNT, toTransferMsg);
        }
    }

    /**
     * 用户UserCoinBalance存在修改余额，不存在创建
     * 记录用户充值记录
     *
     * @param transactionOrder
     * @param userWalletInfo
     * @param txHash
     * @return
     */
    private UserCoinBalance getUserCoinBalance(TransactionOrder transactionOrder, UserWalletInfo userWalletInfo, String txHash) {
        UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByUserIdAndMerchantInfoId(userWalletInfo.getUserid(), userWalletInfo.getMerchantId());
        CoinInfo coinInfo = coinInfoMapper.selectByPrimaryKey(transactionOrder.getCoinId());

        BigDecimal coinNum = transactionOrder.getCoinNum();
        UserCoinLog userCoinLog = new UserCoinLog();

        if (userCoinBalance == null) {
            userCoinBalance = new UserCoinBalance();
            userCoinBalance.setUserid(userWalletInfo.getUserid());
            userCoinBalance.setMerchantId(userWalletInfo.getMerchantId());
            userCoinBalance.setCoinBalance(coinNum);
            userCoinBalance.setShowBalance(coinNum);
            userCoinBalance.setCoinName(coinInfo.getCoinName());
            userCoinBalance.setLastTradingTime(new Date());
            userCoinBalance.setCoinId(coinInfo.getId());
            userCoinBalance.setCreateTime(new Date());
            userCoinBalanceMapper.insertSelective(userCoinBalance);
            createUserCoinLog(txHash, coinNum, coinInfo, userCoinBalance, userCoinLog, Constants.USER_COIN_RECHARGE);
        } else {
            userCoinBalance.setCoinBalance(userCoinBalance.getCoinBalance().add(coinNum).setScale(18, BigDecimal.ROUND_HALF_UP));
            userCoinBalance.setShowBalance(userCoinBalance.getShowBalance().add(coinNum).setScale(18, BigDecimal.ROUND_HALF_UP));
            userCoinBalance.setLastTradingTime(new Date());
            userCoinBalanceMapper.updateByPrimaryKeySelective(userCoinBalance);
            createUserCoinLog(txHash, coinNum, coinInfo, userCoinBalance, userCoinLog, Constants.USER_COIN_RECHARGE);
        }
        return userCoinBalance;
    }

    /**
     * 确认成功后回调通知
     *
     * @param transactionOrder
     * @param userWalletInfo
     * @param userCoinBalance
     * @param coinNum
     * @throws IOException
     */
    private void callBack(TransactionOrder transactionOrder, UserWalletInfo userWalletInfo, UserCoinBalance userCoinBalance, BigDecimal coinNum) throws Exception {
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(userCoinBalance.getMerchantId());

        //回调通知充值成功
        JSONObject callBackJSONObject = new JSONObject();
        callBackJSONObject.put("from", transactionOrder.getFromAddress());
        callBackJSONObject.put("to", transactionOrder.getToAddress());
        callBackJSONObject.put("userid", userWalletInfo.getUserid());
        callBackJSONObject.put("coinNum", coinNum);
        callBackJSONObject.put("fee", transactionOrder.getFee());
        callBackJSONObject.put("tradingTime", transactionOrder.getTradingTime() == null ? transactionOrder.getCreatedTime().getTime() : transactionOrder.getTradingTime().getTime());
        callBackJSONObject.put("txHash", transactionOrder.getTxHash());

        toCallBack(transactionOrder, merchantInfo, merchantInfo.getRechargeSuccessUrl(), callBackJSONObject);

    }

    public void toCallBack(TransactionOrder transactionOrder, MerchantInfo merchantInfo, String url, JSONObject callBackJSONObject) throws Exception {
        String seed = AESCBC.getRandomStringByLength(16);
        String in = AESCBCUtil.encrypt(callBackJSONObject, merchantInfo.getMerchantName(), merchantInfo.getApikey(), merchantInfo.getSecurity(), seed);
        Map map = new HashMap();
        map.put("in", in);
        map.put("seed", seed);
        String rechargeSuccessUrl = merchantInfo.getRechargeSuccessUrl();

        HttpClient client = new HttpClient(url, "post");
        client.setEntity(map);

        String s = client.request(url, "utf-8");

        logger.warn("===============callback============" + transactionOrder.getTxHash());

        JSONObject jsonObject = JSON.parseObject(s);
        while (!jsonObject.getBoolean("success")) {
            Thread.sleep(300000);
            logger.warn("===============callbackrepeat============" + transactionOrder.getTxHash());
            s = client.request(url, "utf-8");
            jsonObject = JSON.parseObject(s);
        }
    }


    /**
     * 记录充值日志
     *
     * @param rechargeAmount
     * @param userCoinBalance
     * @param userCoinLog
     * @param chargeType
     */
    private void createUserCoinLog(String txHash, BigDecimal rechargeAmount, CoinInfo coinInfo, UserCoinBalance userCoinBalance, UserCoinLog userCoinLog, Integer chargeType) {
        userCoinLog.setOrderTxHash(txHash);
        userCoinLog.setCoinName(coinInfo.getCoinName());
        userCoinLog.setCoinId(coinInfo.getId());
        userCoinLog.setChangeNum(rechargeAmount);
        userCoinLog.setChangeType(chargeType);
        userCoinLog.setMerchantId(userCoinBalance.getMerchantId());
        userCoinLog.setUserid(userCoinBalance.getUserid());
        userCoinLog.setCreateTime(new Date());
        userCoinLogMapper.insertSelective(userCoinLog);
    }

//    public static void main(String[] args) throws Exception{
//        Map map = new HashMap();
//        map.put("in", "4w0pOFEZWa2QcymNVSOR4a17iL%2B3I0cmigwo6IJIZptNRRisU91TRv5FSiK7pnVnpqlX4wsMHdpBR6roBnDl2F3uCXvoXyGFR8VjAcEFSsMyU%2F1H0BXC8BUl8sDUEsU64s1nKcVf0PIE4fJ4thYZzXoDLYY7AJwRFtwknJ5KZAutUAOOCwPrnDy0if1%2FcEqVprhW0kyH%2Fe0t7Vv8VChy7n8x0NMC1SLClrE%2BXuxhpAKu4rKOmadGQzXADqCI4XiVfWbYLWir4dD5rk6mRg7Fm5eAZIVy4dofiu3TcybhCVbfzLrD10S7TlH6%2BeB88YuwvpB2%2FfFEWE4C6yZ7JGQ0zXAJrYrStJ3YNFBFyX1COVLML870g7GpHe7mmEo1J0IAh99Z%2BCwoYOS9E8he%2B1oO62aesri8AvPc8rXFfM2Ah1387zP%2BFpPlI4svFqOrPw4v49IXOqpsXkHLQjF4J3YDA5ErmQvGXQdesrcxmvs6c606JGYOYW2C7Vd%2FMBLq1d86btqi9lHOnsQ26Kd8v0qjYqg%2FfixY12fWQ1maiBXAn7w%3D");
//        map.put("seed", "4ZsNsTw88S3LHf0Z");
//
//        HttpClient client = new HttpClient("https://mobile-test.o.bike/api/v1/ocnwallet/recharge", "POST");
//        client.setEntity(map);
//        String s = client.request();
//
//
////        PostMethod post = new PostMethod("https://mobile-test.o.bike/api/v1/ocnwallet/recharge");
////
////        HttpRequestUtil.doPostJSON("https://mobile-test.o.bike/api/v1/ocnwallet/recharge",map);
////
////        String s = HttpRequestUtil.doPostHeaderJsonFromJSONtoString("https://mobile-test.o.bike/api/v1/ocnwallet/recharge", map);
//        System.out.println(s);
//    }

}
