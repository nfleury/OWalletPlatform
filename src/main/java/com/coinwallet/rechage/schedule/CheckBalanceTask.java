package com.coinwallet.rechage.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coinwallet.common.web3j.response.TransactionsResponse;
import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.dao.UserCoinLogMapper;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.entity.UserCoinLog;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CheckBalanceTask {

    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;

    @Autowired
    private UserCoinLogMapper userCoinLogMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;


    public void checkBalance() {

    }

    //@Scheduled(cron = "0 */1 * * * ?")
    public void rechargeRecord(){
        List<TransactionsResponse.Result> results = new ArrayList<>()   ;
        List<String> addresses = userCoinBalanceMapper.selectUserAddress();
        for (TransactionsResponse.Result record : results) {
            if (addresses.contains(record.getTransactionTo())){
                JSONObject jsonObject = (JSONObject) JSON.toJSON(record);
                rabbitTemplate.convertAndSend(RabbitRechargeConfig.CHECK_BALANCE_QUEUE_NAME,jsonObject);
            }
        }
    }

    //@Scheduled(cron = "59 59 23 * * ?")
    public void scanAccountOcoin(){
        List<UserCoinBalance> userCoinBalances = userCoinBalanceMapper.selectAll();
        BigDecimal allCoinBalance = new BigDecimal("0.000000000000000000");
        for (UserCoinBalance userCoinBalance : userCoinBalances) {
            allCoinBalance.add(userCoinBalance.getCoinBalance());
        }
        if (allCoinBalance.compareTo(RabbitRechargeConfig.CHECK_ALL_AMOUNT_UP)>0){
            userCoinBalances.forEach(r -> {
                if (RabbitRechargeConfig.CHECK_PERSONAGE_AMOUNT_UP.compareTo(r.getCoinBalance())>0
                        &&new BigDecimal("0").compareTo(r.getCoinBalance())<0){
                    //todo tran

                    UserCoinLog userCoinLog = new UserCoinLog();
                    userCoinLog.setChangeNum(r.getCoinBalance());
                    userCoinLog.setChangeType(RabbitRechargeConfig.USER_COIN_COLLECT);
                    userCoinLog.setMerchantId(r.getMerchantId());
                    userCoinLog.setUserid(r.getUserid());
                    userCoinLogMapper.insertSelective(userCoinLog);
                }
            });
        }

    }

}
