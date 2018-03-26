package com.coinwallet.rechage.service;

import com.alibaba.fastjson.JSON;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.util.DateUtils;
import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.common.web3j.utils.OWalletUtils;
import com.coinwallet.rechage.dao.*;
import com.coinwallet.rechage.entity.*;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Created by fly on 18/3/23.
 */
@Service
public class CheckRechargeOrderOnNodeService {


    @Autowired
    private UserWalletInfoMapper userWalletInfoMapper;

    @Autowired
    private TransactionOrderMapper transactionOrderMapper;

    @Autowired
    private DefeatOrderLogMapper defeatOrderLogMapper;


    @Autowired
    private CoinInfoMapper coinInfoMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GasTransactionLogMapper gasTransactionLogMapper;



    /**
     * 扫描节点获得交易信息,记录peeding状态订单
     * @param transaction
     */
    public void checkNodeRecharge(Transaction transaction){


        String toAddress = CommonUtils.getContractAddressTo(transaction.getInput());
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectWalletInfoByAddress(toAddress);
        if (userWalletInfo != null) {
            CoinInfo coinInfo = coinInfoMapper.selectByContractAddress(transaction.getTo());
            TransactionOrder transcationOrder = new TransactionOrder();
            transcationOrder.setCreatedTime(new Date());
            insertTransacntionOrder(transaction, toAddress, Constants.ORDER_TYPE_USER_RECHARGE, coinInfo, Constants.ORDER_STATUS_PEEDING, transcationOrder);

        }

    }

    /**
     * 记录监听或扫描区块信息
     * 失败记录失败日志存储主键hash值
     *
     * @param transaction
     * @param toAddress
     * @param coinInfo
     * @param orderStatus
     * @param transcationOrder
     */
    public int insertTransacntionOrder(Transaction transaction, String toAddress, Integer transcationType, CoinInfo coinInfo, Integer orderStatus, TransactionOrder transcationOrder) {
        TransactionOrder order = transactionOrderMapper.selectByPrimaryKey(transaction.getHash());
        if (order != null) {
            return 0;
        }
        try {
            transcationOrder.setTxHash(transaction.getHash());
            transcationOrder.setTranscationType(transcationType);
            transcationOrder.setCoinName(coinInfo.getCoinName());
            transcationOrder.setCoinId(coinInfo.getId());
            transcationOrder.setCoinNum(CommonUtils.getSTAmount(transaction.getInput()));
            transcationOrder.setCreatedTime(new Date());
            transcationOrder.setGasPrice(new BigDecimal(transaction.getGasPriceRaw()));
            transcationOrder.setFromAddress(transaction.getFrom());
            transcationOrder.setToAddress(toAddress);
            transcationOrder.setOrderStatus(orderStatus);
            return transactionOrderMapper.insertSelective(transcationOrder);
        } catch (Exception e) {
            DefeatOrderLog defeatOrderLog = new DefeatOrderLog();
            defeatOrderLog.setTxHash(transaction.getHash());
            defeatOrderLog.setCreateTime(new Date());
            defeatOrderLogMapper.insertSelective(defeatOrderLog);
            return 0;
        }

    }

    /**
     * 确认订单是否成功
     * @param transactionOrder
     * @param verificationInfo
     * @throws Exception
     */
    public void confirmOrder(TransactionOrder transactionOrder,TransactionVerificationInfo verificationInfo) throws Exception{
        if (verificationInfo != null && verificationInfo.isVerification()) {
            transactionOrder.setUsedGas(verificationInfo.getGasUsed());
            transactionOrder.setTradingTime(DateUtils.TimeStamp2Date(verificationInfo.getTimeStamp()));
            transactionOrder.setFee(OWalletUtils.getTransactionFee(transactionOrder.getGasPrice(), transactionOrder.getUsedGas()));
            //用户充值确认
            if (Constants.ORDER_TYPE_USER_RECHARGE==transactionOrder.getTranscationType()) {
                rabbitTemplate.convertAndSend(RabbitRechargeConfig.CHECK_BALANCE, JSON.toJSONString(transactionOrder));
            }
            //gas转账确认
            if (Constants.ORDER_TYPE_GAS_RECHARGE==transactionOrder.getTranscationType()) {
                //修改转账gas订单
                TransactionOrder oldOrder = new TransactionOrder();
                oldOrder.setTxHash(transactionOrder.getTxHash());
                oldOrder.setOrderStatus(Constants.ORDER_STATUS_SUCCESS);
                oldOrder.setUpdatedTime(new Date());
                oldOrder.setTradingTime(DateUtils.TimeStamp2Date(verificationInfo.getTimeStamp()));
                oldOrder.setUsedGas(verificationInfo.getGasUsed());
                oldOrder.setFee(OWalletUtils.getTransactionFee(transactionOrder.getGasPrice(), transactionOrder.getUsedGas()));
                int i = transactionOrderMapper.updateByPrimaryKeyAndOrderStatus(oldOrder);
                if (i > 0){

                    rabbitTemplate.convertAndSend(RabbitRechargeConfig.RECHARGE_COIN_TO_GATHER_ACCOUNT, JSON.toJSONString(transactionOrder));
                    //记录充值邮费日志
                    GasTransactionLog gasTransactionLog = new GasTransactionLog();
                    gasTransactionLog.setCoinName(Constants.GAS_COIN_NAME);
                    gasTransactionLog.setCoinNum(transactionOrder.getCoinNum());
                    gasTransactionLog.setGasFee(transactionOrder.getFee());
                    gasTransactionLog.setCreatedAt(new Date());
                    gasTransactionLog.setOrderTxHash(transactionOrder.getTxHash());
                    gasTransactionLog.setFromAddress(transactionOrder.getFromAddress());
                    gasTransactionLog.setToAddress(transactionOrder.getToAddress());
                    gasTransactionLogMapper.insertSelective(gasTransactionLog);
                }

            }
            //提币订单确认
            if (Constants.ORDER_TYPE_GATHER_RECHARGE==transactionOrder.getTranscationType()) {
                rabbitTemplate.convertAndSend(RabbitRechargeConfig.AFFIRM_RECHARGE_TO_GATHER, JSON.toJSONString(transactionOrder));
            }
            //第三方平台转账
            if (Constants.ORDER_TYPE_PLATFORM_TRANSFER==transactionOrder.getTranscationType()) {
                rabbitTemplate.convertAndSend(RabbitRechargeConfig.AFFIRM_PLATFORM_TRANSFER, JSON.toJSONString(transactionOrder));
            }

        }
    }





}
