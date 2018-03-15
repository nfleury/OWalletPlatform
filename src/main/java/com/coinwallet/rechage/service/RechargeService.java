package com.coinwallet.rechage.service;

import com.coinwallet.common.InitConfig;
import com.coinwallet.common.util.AES;
import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.bean.WalletInfo;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.rechage.controller.req.CreateWalletReq;
import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.entity.MerchantInfo;
import com.coinwallet.rechage.entity.UserCoinBalance;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class RechargeService {

    @Autowired(required = true)
    UserCoinBalanceMapper userCoinBalanceMapper;

    @Autowired
    private InitConfig initConfig;

    /**
     *
     * @param transaction
     */
    public void rechargeRecord(Transaction transaction) {
        String toAddress = CommonUtils.getContractAddressTo(transaction.getInput());
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectWalletInfoByAddress(toAddress);
        if (userWalletInfo != null) {
            CoinInfo coinInfo = coinInfoMapper.selectByContravtAddress(transaction.getTo());
            TranscationOrder transcationOrder = new TranscationOrder();
            transcationOrder.setCreatedTime(new Date());
            insertTransacntionOrder(transaction, toAddress, RabbitRechargeConfig.ORDER_TYPE_USER_RECHARGE,coinInfo, RabbitRechargeConfig.ORDER_STATUS_PEEDING, transcationOrder);

        }


    }

    /**
     *
     * @param transaction
     * @param toAddress
     * @param coinInfo
     * @param orderStatus
     * @param transcationOrder
     */
    public void insertTransacntionOrder(Transaction transaction, String toAddress, Integer transcationType, CoinInfo coinInfo, Integer orderStatus, TranscationOrder transcationOrder) {

        transcationOrder.setTxHash(transaction.getHash());
        transcationOrder.setTranscationType(transcationType);
        transcationOrder.setCoinName(coinInfo.getCoinName());
        transcationOrder.setCoinId(coinInfo.getId());
        transcationOrder.setCoinNum(CommonUtils.getSTAmount(transaction.getInput()));
        transcationOrder.setCreatedTime(new Date());
        transcationOrder.setGasPrice(new BigDecimal(transaction.getGasPrice()));
        transcationOrder.setFromAddress(transaction.getFrom());
        transcationOrder.setToAddress(toAddress);
        transcationOrder.setOrderStatus(orderStatus);
        transcationOrderMapper.insertSelective(transcationOrder);
    }
}
