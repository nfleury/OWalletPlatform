package com.coinwallet.rechage.service;

import com.coinwallet.common.config.InitConfig;
import com.coinwallet.common.util.AES;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.util.Rand;
import com.coinwallet.common.web3j.service.CustomNodeService;
import com.coinwallet.common.web3j.transaction.OWalletTransaction;
import com.coinwallet.common.web3j.transaction.TransactionOnNode;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.rechage.dao.MerchantWalletGroupRelationMapper;
import com.coinwallet.rechage.dao.TransactionOrderMapper;
import com.coinwallet.rechage.dao.WalletGroupDetailMapper;
import com.coinwallet.rechage.dao.WalletGroupMapper;
import com.coinwallet.rechage.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fly on 18/3/23.
 */
@Service
public class RechargeGasToUserAccountService {

    Logger logger = LoggerFactory.getLogger(RechargeGasToUserAccountService.class);

    @Autowired
    private CustomNodeService customNodeService;


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

    public void rechargeGas(UserCoinBalance userCoinBalance, UserWalletInfo userWalletInfo) throws Exception {
        //todo gas账户  credentials2账户
        String gasPrivateKey = getWalletPrivateKeyByTypeAndMerchantId(1, userCoinBalance.getMerchantId());
        if (StringUtils.isNotBlank(gasPrivateKey)) {

            Web3j web3j = Web3j.build(customNodeService);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            String privateKey = AES.decrypt(userWalletInfo.getPrivatekey(), initConfig.deskey);
            Credentials credentials = Credentials.create(privateKey);
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();

            String decrypt = AES.decrypt(gasPrivateKey, initConfig.deskey);

            Credentials credentials2 = Credentials.create(decrypt);
            BigInteger nonce2 = web3j.ethGetTransactionCount(credentials2.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            //需要的gas
            BigInteger needGasLimit = TransactionOnNode.getContractTransactionGasLimit(web3j, credentials.getAddress(), nonce, gasPrice);
            //gas账户打过来需要的提币的gas
            BigInteger ethTransactionGasLimit = TransactionOnNode.getEthTransactionGasLimit(web3j, credentials2.getAddress(), nonce2, gasPrice);

            needGasLimit = needGasLimit.divide(new BigInteger("10")).multiply(new BigInteger("12"));

            //需要gas的eth
            BigDecimal needGas = CommonUtils.bit18(needGasLimit.multiply(gasPrice));

            String txHash = OWalletTransaction.doubleTransactionETH(web3j, credentials.getAddress(), credentials2.getEcKeyPair(), needGas.toPlainString(), gasPrice.toString(), ethTransactionGasLimit.toString(), "");

            if (StringUtils.isNotBlank(txHash)){
                //记录充值邮费交易订单
                TransactionOrder transactionOrder = new TransactionOrder();
                transactionOrder.setTxHash(txHash);
                transactionOrder.setCoinNum(needGas);
                transactionOrder.setCoinName("ETH");
                transactionOrder.setCoinId(3);
                transactionOrder.setFromAddress(credentials2.getAddress());
                transactionOrder.setToAddress(credentials.getAddress());
                transactionOrder.setCreatedTime(new Date());
                transactionOrder.setGasPrice(new BigDecimal(gasPrice.toString()));
                transactionOrder.setOrderStatus(Constants.ORDER_STATUS_PEEDING);
                transactionOrder.setTranscationType(Constants.ORDER_TYPE_GAS_RECHARGE);
                transactionOrderMapper.insertSelective(transactionOrder);
            }else {
                logger.warn("=============充值邮费获取订单id为空==================");
            }


        }
    }

    public String getWalletPrivateKeyByTypeAndMerchantId(int type, Integer merchantId) {

        //查询该商户下所有邮费组
        MerchantWalletGroupRelationExample merchantWalletGroupRelationExample = new MerchantWalletGroupRelationExample();

        merchantWalletGroupRelationExample.or().andMerchantIdEqualTo(merchantId);

        List<MerchantWalletGroupRelation> merchantWalletGroupRelations =
                merchantWalletGroupRelationMapper.selectByExample(merchantWalletGroupRelationExample);


        if (merchantWalletGroupRelations != null && merchantWalletGroupRelations.size() > 0) {
            ArrayList<WalletGroupDetail> list = new ArrayList<>();
            for (MerchantWalletGroupRelation mwgr : merchantWalletGroupRelations) {

                //查询所有邮费组
                WalletGroup walletGroup = walletGroupMapper.selectByPrimaryKey(mwgr.getWalletGroupId());
                if (walletGroup != null && walletGroup.getGroupType().intValue() == type) {
                    //查询地址
                    WalletGroupDetailExample walletGroupDetailExample = new WalletGroupDetailExample();
                    walletGroupDetailExample.or().andGroupIdEqualTo(walletGroup.getId());

                    List<WalletGroupDetail> walletGroupDetailList = walletGroupDetailMapper.selectByExample(walletGroupDetailExample);

                    if (walletGroupDetailList != null && walletGroupDetailList.size() > 0) {
                        list.addAll(walletGroupDetailList);
                    }
                }

            }

            if (list.size() > 0) {
                int i = Rand.RandNum(list.size());
                WalletGroupDetail walletGroupDetail = list.get(i);
                return walletGroupDetail.getPrivateKey();
            }


        }

        return null;


    }

    public static void main(String[] args) throws Exception{
//        Web3j web3j = Web3j.build(new CustomNodeService());
//        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
//        String privateKey = AES.decrypt("dwr5R+ycHBkbYeM+zh4ci46NSRsfiIT3FznIKXafl+mZVkr6gxjjs28VKgHi4+nc6uuJDCOQIhMgvgLoCshqNdTZNjmdxGr2UXf5OjjMz88=", "BZZOiT5mx66ifMQYj/ex/g==");
//        System.out.println(privateKey);
//        Credentials credentials = Credentials.create(privateKey);
//
//        BigInteger nonce2 = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
//
//        System.out.println(nonce2);
//
//        BigInteger ethTransactionGasLimit = TransactionOnNode.getEthTransactionGasLimit(web3j, credentials.getAddress(), nonce2, gasPrice);
//
//        //需要gas的eth
//
//        String txHash = OWalletTransaction.doubleTransactionETH(web3j, "0x10980F0f5bFA2d1F3a2DF420e94b3F7208e1c752", credentials.getEcKeyPair(), "0.001", gasPrice.toString(), ethTransactionGasLimit.toString(), "");
//
//        System.out.println(txHash);

        Web3j web3j = Web3j.build(new CustomNodeService());
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        String privateKey = AES.decrypt("dwr5R+ycHBkbYeM+zh4ci46NSRsfiIT3FznIKXafl+mZVkr6gxjjs28VKgHi4+nc6uuJDCOQIhMgvgLoCshqNdTZNjmdxGr2UXf5OjjMz88=", "BZZOiT5mx66ifMQYj/ex/g==");
        Credentials credentials = Credentials.create(privateKey);
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();

        String decrypt = AES.decrypt("wo2QvMmNZ9yhHDkQ+jh1YxEf81svZaAHYt+Ve71K8gP43NTFisSC+y2OybL++Piw/QpEDyT5HnvvhPzj+7mJkdTZNjmdxGr2UXf5OjjMz88=", "BZZOiT5mx66ifMQYj/ex/g==");

        Credentials credentials2 = Credentials.create(decrypt);
        BigInteger nonce2 = web3j.ethGetTransactionCount(credentials2.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
        //需要的gas
        BigInteger needGasLimit = TransactionOnNode.getContractTransactionGasLimit(web3j, credentials.getAddress(), nonce, gasPrice);
        //gas账户打过来需要的提币的gas
        BigInteger ethTransactionGasLimit = TransactionOnNode.getEthTransactionGasLimit(web3j, credentials2.getAddress(), nonce2, gasPrice);

        ethTransactionGasLimit = ethTransactionGasLimit.multiply(new BigInteger("2"));

        needGasLimit = needGasLimit.divide(new BigInteger("10")).multiply(new BigInteger("12"));

        //需要gas的eth
        BigDecimal needGas = CommonUtils.bit18(needGasLimit.multiply(gasPrice));

        String txHash = OWalletTransaction.doubleTransactionETH(web3j, credentials.getAddress(), credentials2.getEcKeyPair(), needGas.toPlainString(), gasPrice.toString(), ethTransactionGasLimit.toString(), "");

        System.out.println(
                txHash
        );






    }


}
