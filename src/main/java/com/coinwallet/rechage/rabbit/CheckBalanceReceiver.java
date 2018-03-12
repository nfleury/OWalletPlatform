package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.common.web3j.response.TransactionsResponse;
import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.dao.UserCoinLogMapper;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.entity.UserCoinBalanceExample;
import com.coinwallet.rechage.entity.UserCoinLog;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckBalanceReceiver {

    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;

    @Autowired
    private UserCoinLogMapper userCoinLogMapper;

    @RabbitListener(queues= RabbitRechargeConfig.CHECK_BALANCE_QUEUE_NAME)
    public void coinCharge(String msg) {
        try {
            TransactionsResponse.Result record = JSON.parseObject(msg, TransactionsResponse.Result.class);
            UserCoinBalanceExample userCoinBalanceExample = new UserCoinBalanceExample();
            userCoinBalanceExample.createCriteria().andCoinAddressEqualTo(record.getTransactionTo());
            UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByExample(userCoinBalanceExample).get(0);

            UserCoinLog userCoinLog = new UserCoinLog();
            createUserCoinLog(record, userCoinBalance, userCoinLog,RabbitRechargeConfig.USER_COIN_RECHARGE);
            userCoinBalance.setCoinBalance(record.getTransactionAmount());
            userCoinBalance.setShowBalance(record.getTransactionAmount());
            userCoinBalanceMapper.updateByPrimaryKeySelective(userCoinBalance);

            if (RabbitRechargeConfig.CHECK_PERSONAGE_AMOUNT_UP.compareTo(record.getTransactionAmount())<0){
                //todo
                createUserCoinLog(record, userCoinBalance, userCoinLog,RabbitRechargeConfig.USER_COIN_COLLECT);
            }

        }catch (Exception e){

        }

    }

    /**
     *  record user coin log
     * @param record
     * @param userCoinBalance
     * @param userCoinLog
     * @param chargeType
     */
    private void createUserCoinLog(TransactionsResponse.Result record, UserCoinBalance userCoinBalance, UserCoinLog userCoinLog, Integer chargeType) {
        userCoinLog.setChangeNum(record.getTransactionAmount());
        userCoinLog.setChangeType(chargeType);
        userCoinLog.setMerchantId(userCoinBalance.getMerchantId());
        userCoinLog.setUserid(userCoinBalance.getUserid());
        userCoinLogMapper.insertSelective(userCoinLog);
    }

}
