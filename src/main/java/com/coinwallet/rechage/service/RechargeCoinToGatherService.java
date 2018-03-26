package com.coinwallet.rechage.service;

import com.coinwallet.common.config.InitConfig;
import com.coinwallet.common.util.AES;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.util.Rand;
import com.coinwallet.common.web3j.service.CustomNodeService;
import com.coinwallet.common.web3j.transaction.OWalletTransaction;
import com.coinwallet.common.web3j.transaction.TransactionOnNode;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.rechage.dao.*;
import com.coinwallet.rechage.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fly on 18/3/23.
 */
@Service
public class RechargeCoinToGatherService {

    Logger logger = LoggerFactory.getLogger(RechargeCoinToGatherService.class);

    @Autowired
    private CustomNodeService customNodeService;

    @Autowired
    private UserWalletInfoMapper userWalletInfoMapper;


    @Autowired
    private UserCoinBalanceMapper userCoinBalanceMapper;


    @Autowired
    private CoinInfoMapper coinInfoMapper;


    @Autowired
    private TransactionOrderMapper transactionOrderMapper;


    @Autowired
    private InitConfig initConfig;

    @Autowired
    private WalletGroupMapper walletGroupMapper;

    @Autowired
    private WalletGroupDetailMapper walletGroupDetailMapper;

    @Autowired
    private MerchantWalletGroupRelationMapper merchantWalletGroupRelationMapper;

    public void rechargeCoin(TransactionOrder transactionOrder) throws Exception {
        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectWalletInfoByAddress(transactionOrder.getToAddress());

        UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByUserIdAndMerchantInfoId(userWalletInfo.getUserid(), userWalletInfo.getMerchantId());
        CoinInfo coinInfo = coinInfoMapper.selectByPrimaryKey(userCoinBalance.getCoinId());


        Web3j web3j = Web3j.build(customNodeService);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

//            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
//            BigInteger contractTransactionGasLimit = TransactionOnNode.getContractTransactionGasLimit(web3j, credentials.getAddress(), nonce, gasPrice);

        //todo 提币地址 address
        String address = getWalletAddressByTypeAndMerchantId(2, userCoinBalance.getMerchantId());//提币地址

        BigDecimal gasDecimal = CommonUtils.bit18(gasPrice);

        BigInteger gasLimit = transactionOrder.getCoinNum().divide(gasDecimal).toBigInteger();

        if (StringUtils.isNotBlank(address)) {

            String privateKey = AES.decrypt(userWalletInfo.getPrivatekey(), initConfig.deskey);
            Credentials credentials = Credentials.create(privateKey);

            BigDecimal userWalletOCN = TransactionOnNode.balanceOfContractToken(web3j, coinInfo.getContractAddress(), credentials.getAddress());
            BigDecimal userOCN = CommonUtils.bit18(new BigInteger(userWalletOCN.toString()));


            logger.warn("============实际余额====================="+userWalletOCN);
            logger.warn("============数据库中余额====================="+userCoinBalance.getCoinBalance());

            //提币到总账
            String txHash = OWalletTransaction.doubleTransactionCoin(web3j, address, credentials.getEcKeyPair(), userOCN.toString(), gasPrice.toString(), gasLimit.toString(), coinInfo.getContractAddress(), "");

            UserCoinBalance coinBalance = new UserCoinBalance();
            if (txHash.equals("error")) {
                coinBalance.setId(userCoinBalance.getId());
                coinBalance.setTransferStatus((byte) Constants.USER_GAS_ACQUIRE);
                userCoinBalanceMapper.updateByPrimaryKeySelective(coinBalance);

            } else {
                //转账中
                coinBalance.setId(userCoinBalance.getId());
                coinBalance.setTransferStatus((byte) Constants.USER_IN_TRANS);
                userCoinBalanceMapper.updateByPrimaryKeySelective(coinBalance);



                //记录提币交易订单
                TransactionOrder rechargeCoinOrder = new TransactionOrder();
                rechargeCoinOrder.setTxHash(txHash);
                rechargeCoinOrder.setCoinNum(userOCN);
                rechargeCoinOrder.setCoinName(coinInfo.getCoinName());
                rechargeCoinOrder.setCoinId(coinInfo.getId());
                rechargeCoinOrder.setFromAddress(credentials.getAddress());
                rechargeCoinOrder.setToAddress(address);
                rechargeCoinOrder.setCreatedTime(new Date());
                rechargeCoinOrder.setGasPrice(new BigDecimal(gasPrice.toString()));
                rechargeCoinOrder.setOrderStatus(Constants.ORDER_STATUS_PEEDING);
                rechargeCoinOrder.setTranscationType(Constants.ORDER_TYPE_GATHER_RECHARGE);
                transactionOrderMapper.insertSelective(rechargeCoinOrder);

                logger.warn("=============RECHARGE_COIN_TO_GATHER_ACCOUNT======end========");
            }


        }
    }

    public String getWalletAddressByTypeAndMerchantId(int type, Integer merchantId) {

        //查询该商户下所有邮费组
        MerchantWalletGroupRelationExample merchantWalletGroupRelationExample = new MerchantWalletGroupRelationExample();

        merchantWalletGroupRelationExample.or().andMerchantIdEqualTo(merchantId);

        List<MerchantWalletGroupRelation> merchantWalletGroupRelations =
                merchantWalletGroupRelationMapper.selectByExample(merchantWalletGroupRelationExample);


        if (merchantWalletGroupRelations != null && merchantWalletGroupRelations.size() > 0) {
            ArrayList<String> list = new ArrayList<>();
            for (MerchantWalletGroupRelation mwgr : merchantWalletGroupRelations) {

                //查询所有邮费组
                WalletGroup walletGroup = walletGroupMapper.selectByPrimaryKey(mwgr.getWalletGroupId());
                if (walletGroup != null && walletGroup.getGroupType().intValue() == type) {
                    //查询地址
                    WalletGroupDetailExample walletGroupDetailExample = new WalletGroupDetailExample();
                    walletGroupDetailExample.or().andGroupIdEqualTo(walletGroup.getId());

                    List<WalletGroupDetail> walletGroupDetailList = walletGroupDetailMapper.selectByExample(walletGroupDetailExample);

                    if (walletGroupDetailList != null && walletGroupDetailList.size() > 0) {
                        for (WalletGroupDetail wgd : walletGroupDetailList)
                            list.add(wgd.getWalletAddress());
                    }
                }

            }

            if (list.size() > 0) {
                int i = Rand.RandNum(list.size());
                String s = list.get(i);
                return s;
            }


        }

        return null;


    }

    //    public static void main(String[] args) throws  Exception{
//        Web3j web3j = Web3j.build(new CustomNodeService());
//        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
//
//        BigDecimal gasDecimal = CommonUtils.bit18(gasPrice);
//
//        System.out.println(gasDecimal);
//
//        BigInteger gasLimit = new BigDecimal("0.000217").divide(gasDecimal).toBigInteger();
//
//        System.out.println(gasLimit);
//
//        System.out.println(gasPrice);
//
//
//
//        String decrypt = AES.decrypt("2E0KVmK7/oDQGgWSwOmh1n7grTXz7F6K8nr6OKycqWmQyw/ZxjazaWN0CR/QOAMUXZziM2k01+Cb3GR7gtkBiFu6uwCnNSLoRvGT9uu0Lxg=", "nQeihnkM7Jh7e/WBmeV/3Q==");
//        Credentials credentials = Credentials.create(decrypt);
//        String txHash = TransactionOnNode.transactionOnContract(web3j, credentials.getEcKeyPair(), "9", "0x5623629c15733d9394706c6c9dd92ded590acbf5", gasPrice.toString(), gasLimit.toString(), "", "0x4092678e4e78230f46a1534c0fbc8fa39780892b");
//        System.out.println(txHash);
//    }

}
