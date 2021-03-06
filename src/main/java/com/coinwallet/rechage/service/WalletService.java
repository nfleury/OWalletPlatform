package com.coinwallet.rechage.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coinwallet.common.config.InitConfig;
import com.coinwallet.common.apisecurity.AESCBCUtil;
import com.coinwallet.common.response.FailResponse;
import com.coinwallet.common.response.ResponseValue;
import com.coinwallet.common.response.SuccessResponse;
import com.coinwallet.common.util.AES;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.util.StringUtil;
import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.bean.WalletInfo;
import com.coinwallet.common.web3j.service.CustomNodeService;
import com.coinwallet.common.web3j.transaction.OWalletTransaction;
import com.coinwallet.common.web3j.transaction.TransactionOnNode;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.rechage.controller.req.CreateWalletReq;
import com.coinwallet.rechage.controller.resp.CreateWalletResp;
import com.coinwallet.rechage.dao.*;
import com.coinwallet.rechage.entity.*;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WalletService {

    Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private UserWalletInfoMapper userWalletInfoMapper;

    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    private CustomNodeService customNodeService;

    @Autowired
    private TransactionOrderMapper transactionOrderMapper;


    @Autowired
    private CoinInfoMapper coinInfoMapper;

    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;

    @Autowired
    private UserCoinLogMapper userCoinLogMapper;

    @Autowired
    private InitConfig initConfig;
    
    @Autowired
    private PlatformTransferMapper platformTransferMapper;

    /**
     * create-wallet
     *
     * @param userId
     * @param merchantInfo
     * @return
     */
    public UserWalletInfo initUserWallet(Integer userId, MerchantInfo merchantInfo) {
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectByUserIdAndMerchantInfoId(userId, merchantInfo.getId());
        if (userWalletInfo == null) {
            userWalletInfo = new UserWalletInfo();
        } else {
            return userWalletInfo;
        }
        try {
            WalletInfo walletInfo = OWalletAPI.generateWallet();
            userWalletInfo.setCoinAddress("0x" + walletInfo.getWalletAddress());
            userWalletInfo.setMerchantId(merchantInfo.getId());
            userWalletInfo.setUserid(userId);
            userWalletInfo.setPrivatekey(AES.encrypt(walletInfo.getPrivateKey(), initConfig.deskey));
            userWalletInfo.setCreatedAt(new Date());
            userWalletInfoMapper.insertSelective(userWalletInfo);
        } catch (Exception e) {
            return null;
        }
        return userWalletInfo;
    }

    /**
     * 创建钱包
     *
     * @param createWalletReq
     * @return
     */
    public ResponseValue initWallet(CreateWalletReq createWalletReq, HttpServletRequest request) {
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(createWalletReq.getMerchantId());
        if (merchantInfo == null) {
            return new FailResponse(1001, "param error");
        }
        String decrypt = null;
        try {
            decrypt = AESCBCUtil.decrypt(createWalletReq.getIn(), merchantInfo.getMerchantName(), merchantInfo.getApikey(), merchantInfo.getSecurity(), createWalletReq.getSeed());
        } catch (Exception e) {
            return new FailResponse(1001, "param error");
        }
        if (decrypt == null) {
            return new FailResponse(1001, "param error");
        }
        request.setAttribute("in",decrypt);
        request.setAttribute("seed",createWalletReq.getSeed());
        request.setAttribute("merchantId",merchantInfo.getId());
        JSONObject jsonObject = JSON.parseObject(decrypt);
        Integer userId = (Integer) jsonObject.get("userid");
        if (userId == null) {
            return new FailResponse(1001, "param error");
        }
        UserWalletInfo userWalletInfo = initUserWallet(userId, merchantInfo);
        ResponseValue responseValue = null;
        if (userWalletInfo == null) {
            responseValue = new FailResponse(1001, "create wallet failed");
        } else {
            CreateWalletResp createWalletResp = new CreateWalletResp();
            createWalletResp.setAddress(userWalletInfo.getCoinAddress());
            responseValue = new SuccessResponse(createWalletResp);
        }
        return responseValue;
    }

    /**
     * 交易通知
     *
     * @param transcationReq
     * @return
     */
    public ResponseValue changeBalace(CreateWalletReq transcationReq, HttpServletRequest request) {
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(transcationReq.getMerchantId());
        if (merchantInfo == null) {
            return new FailResponse(1001, "param error");
        }
        String decrypt = null;
        try {
            decrypt = AESCBCUtil.decrypt(transcationReq.getIn(), merchantInfo.getMerchantName(), merchantInfo.getApikey(), merchantInfo.getSecurity(), transcationReq.getSeed());
        } catch (Exception e) {
            return new FailResponse(1001, "param error");
        }
        if (decrypt == null) {
            return new FailResponse(1001, "param error");
        }

        request.setAttribute("in",decrypt);
        request.setAttribute("seed",transcationReq.getSeed());
        request.setAttribute("merchantId",merchantInfo.getId());
        JSONObject jsonObject = JSON.parseObject(decrypt);
        Integer userId = (Integer) jsonObject.get("userid");
        BigDecimal coinNum = new BigDecimal(jsonObject.get("coinNum").toString());
        Integer transactionType = (Integer) jsonObject.get("type");
        UserCoinBalance coinBalance = userCoinBalanceMapper.selectByUserIdAndMerchantInfoId(userId, merchantInfo.getId());
        UserCoinLog userCoinLog = new UserCoinLog();
        UserCoinBalance updateUserCoinBalance = new UserCoinBalance();
        updateUserCoinBalance.setId(coinBalance.getId());
        if (Constants.USER_COIN_CONSUME == transactionType) {
            updateUserCoinBalance.setShowBalance(coinBalance.getShowBalance().subtract(coinNum));
            updateUserBalanceAndRecordUserLog(coinNum, transactionType, coinBalance, userCoinLog, updateUserCoinBalance);

        }
        if (Constants.USER_COIN_OBTAIN == transactionType) {
            updateUserCoinBalance.setShowBalance(coinBalance.getShowBalance().add(coinNum));
            updateUserBalanceAndRecordUserLog(coinNum, transactionType, coinBalance, userCoinLog, updateUserCoinBalance);
        }
        Map map = new HashMap<>();
        map.put("balance", updateUserCoinBalance.getShowBalance());
        return new SuccessResponse(map);
    }

    /**
     * 修改用户余额，记录日志
     *
     * @param ocnNum
     * @param transactionType
     * @param coinBalance
     * @param userCoinLog
     * @param updateUserCoinBalance
     */
    private void updateUserBalanceAndRecordUserLog(BigDecimal ocnNum, Integer transactionType, UserCoinBalance coinBalance, UserCoinLog userCoinLog, UserCoinBalance updateUserCoinBalance) {
        updateUserCoinBalance.setLastTradingTime(new Date());
        userCoinLog.setCoinId(coinBalance.getCoinId());
        userCoinLog.setCoinName(coinBalance.getCoinName());
        userCoinLog.setUserid(coinBalance.getUserid());
        userCoinLog.setMerchantId(coinBalance.getMerchantId());
        userCoinLog.setChangeNum(ocnNum);
        userCoinLog.setChangeType(transactionType);
        userCoinLog.setCreateTime(new Date());
        userCoinBalanceMapper.updateByPrimaryKeySelective(updateUserCoinBalance);
        userCoinLogMapper.insertSelective(userCoinLog);
    }

    /**
     * 给第三方提供转账
     *
     * @param transferReq
     * @return
     */
    public ResponseValue transfer(CreateWalletReq transferReq, HttpServletRequest request) {
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoById(transferReq.getMerchantId());
        if (merchantInfo == null) {
            return new FailResponse(1001, "param error");
        }
        String decrypt = null;
        try {
            decrypt = AESCBCUtil.decrypt(transferReq.getIn(), merchantInfo.getMerchantName(), merchantInfo.getApikey(), merchantInfo.getSecurity(), transferReq.getSeed());
        } catch (Exception e) {
            return new FailResponse(1001, "param error");
        }
        if (decrypt == null) {
            return new FailResponse(1001, "param error");
        }
        request.setAttribute("in",decrypt);
        request.setAttribute("seed",transferReq.getSeed());
        request.setAttribute("merchantId",merchantInfo.getId());
        JSONObject jsonObject = JSON.parseObject(decrypt);
        Integer coinId = (Integer) jsonObject.get("coinId");
        Integer orderId = (Integer) jsonObject.get("orderId");
        BigDecimal coinNum = new BigDecimal(jsonObject.get("coinNum").toString());
        BigDecimal fee = new BigDecimal(jsonObject.get("fee").toString());
        String fromAddress = (String) jsonObject.get("fromAddress");
        String toAddress = (String) jsonObject.get("toAddress");
        String privateKey = (String) jsonObject.get("privateKey");
        if (coinId == null || orderId == null || coinNum == null
                || fee == null || StringUtils.isBlank(fromAddress)
                || StringUtils.isBlank(toAddress) || StringUtils.isBlank(toAddress)) {
            return new FailResponse(1001, "param error");
        }
        CoinInfo coinInfo = coinInfoMapper.selectByPrimaryKey(coinId);

        String result = null;
        BigInteger gasPrice = null;
        try {
            Web3j web3j = Web3j.build(customNodeService);
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
            result = toPlatformTransfer(coinInfo, web3j, gasPrice, coinNum, fee, fromAddress, toAddress, privateKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (Constants.USER_PRIVATEKEY_ERROR.equals(result)){
            return new FailResponse(1002,"privateKey is error");
        }else if (Constants.USER_LACK_OF_BALANCE.equals(result)){
            return new FailResponse(1003,"lack of balance");
        }else if (Constants.TRANSFER_ERROR.equals(result)){
            return new FailResponse(1002,"transfer is error");
        }else {
            transferToRecord(merchantInfo, orderId, coinNum, fromAddress, toAddress, coinInfo, result, gasPrice);
        }

        Map map = new HashMap();
        map.put("txHash", result);
        map.put("status", Constants.ORDER_STATUS_PEEDING);
        map.put("orderId", orderId);
        return new SuccessResponse(map);
    }

    /**
     * 记录平台转账与交易订单
     * @param merchantInfo
     * @param orderId
     * @param coinNum
     * @param fromAddress
     * @param toAddress
     * @param coinInfo
     * @param result
     * @param gasPrice
     */
    private void transferToRecord(MerchantInfo merchantInfo, Integer orderId, BigDecimal coinNum, String fromAddress, String toAddress, CoinInfo coinInfo, String result, BigInteger gasPrice) {
        //记录平台转账
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setAmount(coinNum);
        platformTransfer.setCoinId(coinInfo.getId());
        platformTransfer.setCreatedAt(new Date());
        platformTransfer.setFromAddress(fromAddress);
        platformTransfer.setOrderId(orderId);
        platformTransfer.setMerchantId(merchantInfo.getId());
        platformTransfer.setOrderTxHash(result);
        platformTransfer.setStatus(Constants.ORDER_STATUS_PEEDING);
        platformTransfer.setToAddress(toAddress);
        platformTransferMapper.insertSelective(platformTransfer);

        TransactionOrder transactionOrder = new TransactionOrder();
        //记录交易订单
        transactionOrder.setTxHash(result);
        transactionOrder.setCoinNum(coinNum);
        transactionOrder.setCoinName(coinInfo.getCoinName());
        transactionOrder.setCoinId(coinInfo.getId());
        transactionOrder.setFromAddress(fromAddress);
        transactionOrder.setToAddress(toAddress);
        transactionOrder.setCreatedTime(new Date());
        transactionOrder.setGasPrice(new BigDecimal(gasPrice.toString()));
        transactionOrder.setOrderStatus(Constants.ORDER_STATUS_PEEDING);
        transactionOrder.setTranscationType(Constants.ORDER_TYPE_PLATFORM_TRANSFER);
        transactionOrderMapper.insertSelective(transactionOrder);
    }

    /**
     * 第三方平台平台转账
     *
     * @param coinInfo
     * @param web3j
     * @param gasPrice
     * @param coinNum
     * @param fee
     * @param fromAddress
     * @param toAddress
     * @param privateKey
     * @return
     * @throws Exception
     */
    private String toPlatformTransfer(CoinInfo coinInfo, Web3j web3j, BigInteger gasPrice, BigDecimal coinNum, BigDecimal fee, String fromAddress, String toAddress, String privateKey) throws Exception {


        Credentials credentials = Credentials.create(privateKey);

        if (!fromAddress.equals(credentials.getAddress())) {
            return Constants.USER_PRIVATEKEY_ERROR;
        }
        BigDecimal gasDecimal = CommonUtils.bit18(gasPrice);
        BigInteger gasLimit = fee.divide(gasDecimal).toBigInteger();
        String txHash;
        //转代币
        if (StringUtils.isNotBlank(coinInfo.getContractAddress())) {
            BigDecimal userCoinNum = TransactionOnNode.balanceOfContractToken(web3j, coinInfo.getContractAddress(), credentials.getAddress());
            if (validateTransferNum(coinNum, userCoinNum)) return Constants.USER_LACK_OF_BALANCE;
            txHash = OWalletTransaction.doubleTransactionCoin(web3j, toAddress, credentials.getEcKeyPair(), coinNum.toString(), gasPrice.toString(), gasLimit.toString(), coinInfo.getContractAddress(), "");
            return txHash;
        } else {
            //转eth
            BigDecimal userCoinNum = TransactionOnNode.balanceOfETH(web3j, credentials.getAddress());
            if (validateTransferNum(coinNum, userCoinNum)) return Constants.USER_LACK_OF_BALANCE;
            txHash = OWalletTransaction.doubleTransactionETH(web3j, toAddress, credentials.getEcKeyPair(), coinNum.toString(), gasPrice.toString(), gasLimit.toString(), "");
            return txHash;

        }

    }

    /**
     * 比较账户余额与转账金额
     * @param coinNum
     * @param userCoinNum
     * @return
     */
    private boolean validateTransferNum(BigDecimal coinNum, BigDecimal userCoinNum) {
        if (userCoinNum.compareTo(coinNum) < 0) {
            return true;
        }
        return false;
    }
}
