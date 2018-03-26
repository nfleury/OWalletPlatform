package com.coinwallet.rechage.service;

import com.coinwallet.common.config.InitConfig;
import com.coinwallet.common.util.Constants;
import com.coinwallet.rechage.dao.TransactionOrderMapper;
import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.dao.UserCoinLogMapper;
import com.coinwallet.rechage.dao.UserWalletInfoMapper;
import com.coinwallet.rechage.entity.TransactionOrder;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.entity.UserCoinLog;
import com.coinwallet.rechage.entity.UserWalletInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by fly on 18/3/23.
 */
@Service
public class AffirmRechargeToGatherService {


    @Autowired
    private UserWalletInfoMapper userWalletInfoMapper;


    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;

    @Autowired
    private TransactionOrderMapper transactionOrderMapper;
    @Autowired
    private UserCoinLogMapper userCoinLogMapper;

    @Autowired
    private CheckSuccessRechargeOrderService checkSuccessRechargeOrderService;

    @Autowired
    private InitConfig initConfig;


    public void affirmRechargeCoin(TransactionOrder transactionOrder) throws Exception{
        //修改用户coin_balance
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectWalletInfoByAddress(transactionOrder.getFromAddress());
        UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByUserIdAndMerchantInfoId(userWalletInfo.getUserid(), userWalletInfo.getMerchantId());

        UserCoinBalance updateUserCoinBalance = new UserCoinBalance();
        updateUserCoinBalance.setId(userCoinBalance.getId());
        updateUserCoinBalance.setCoinBalance(Constants.COIN_TO_GATHER);
        updateUserCoinBalance.setTransferStatus((byte) Constants.USER_TRANS_NORMAL);
        userCoinBalanceMapper.updateByPrimaryKeySelective(updateUserCoinBalance);

        //确认修改转账提币订单
        checkSuccessRechargeOrderService.changeTransactionSuccess(transactionOrder);

        //记录日志
        UserCoinLog userCoinLog = new UserCoinLog();
        userCoinLog.setCoinId(userCoinBalance.getCoinId());
        userCoinLog.setCoinName(userCoinBalance.getCoinName());
        userCoinLog.setUserid(userCoinBalance.getUserid());
        userCoinLog.setMerchantId(userCoinBalance.getMerchantId());
        userCoinLog.setChangeNum(transactionOrder.getCoinNum());
        userCoinLog.setChangeType(Constants.USER_COIN_COLLECT);
        userCoinLog.setCreateTime(new Date());
        userCoinLogMapper.insertSelective(userCoinLog);

    }
}
