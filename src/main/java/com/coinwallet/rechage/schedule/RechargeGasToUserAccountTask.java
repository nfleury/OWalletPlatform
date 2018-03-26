//package com.coinwallet.rechage.schedule;
//
//import com.alibaba.fastjson.JSON;
//import com.coinwallet.common.config.InitConfig;
//import com.coinwallet.common.util.AES;
//import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
//import com.coinwallet.common.web3j.service.CustomNodeService;
//import com.coinwallet.common.web3j.transaction.TransactionOnNode;
//import com.coinwallet.common.web3j.utils.CommonUtils;
//import com.coinwallet.rechage.dao.*;
//import com.coinwallet.rechage.entity.*;
//import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.web3j.crypto.Credentials;
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.core.DefaultBlockParameterName;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
///**
// * Created by liuhuan on 2018/3/22.
// */
//@Component
//public class RechargeGasToUserAccountTask {
//
//    @Autowired
//    private TransactionOrderMapper transactionOrderMapper;
//
//    @Autowired
//    private CustomNodeService customNodeService;
//
//    @Autowired
//    private InitConfig initConfig;
//
//
//    @Autowired
//    private WalletGroupMapper walletGroupMapper;
//
//    @Autowired
//    private WalletGroupDetailMapper walletGroupDetailMapper;
//
//    @Autowired
//    private MerchantWalletGroupRelationMapper merchantWalletGroupRelationMapper;
//
//    @Autowired
//    private UserCoinBalanceMapper userCoinBalanceMapper;
//
//    @Autowired
//    private UserWalletInfoMapper userWalletInfoMapper;
//
//
//    @Autowired
//    private CoinInfoMapper coinInfoMapper;
//
//    @Scheduled(cron = "0 */5 * * * ?")
//    public void checkOrderStausFromNode() {
//        try {
//            List<CoinInfo> coinInfos = coinInfoMapper.selectByExample(new CoinInfoExample());
//            for (CoinInfo coinInfo : coinInfos) {
//                UserCoinBalanceExample coinBalanceExample = new UserCoinBalanceExample();
//                UserCoinBalanceExample.Criteria criteria = coinBalanceExample.createCriteria();
//                criteria.andCoinBalanceGreaterThan(new BigDecimal(coinInfo.getCoinHigherLimit().toString())).andCoinIdEqualTo(coinInfo.getId()).andTransferStatusEqualTo(RabbitRechargeConfig.NO_TRANSFER_ORDER.byteValue()) ;
//
//                List<UserCoinBalance> userCoinBalances = userCoinBalanceMapper.selectByExample(coinBalanceExample);
//                if (userCoinBalances!=null&&userCoinBalances.size()>0){
//
//                    for (UserCoinBalance userCoinBalance : userCoinBalances) {
//
//                        UserWalletInfo userWalletInfo = userWalletInfoMapper.selectByUserIdAndMerchantInfoId(userCoinBalance.getUserid(), userCoinBalance.getMerchantId());
//
//                        Web3j web3j = Web3j.build(customNodeService);
//                        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
//                        String privateKey = AES.decrypt(userWalletInfo.getPrivatekey(), initConfig.deskey);
//                        Credentials credentials = Credentials.create(privateKey);
//                        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
//
//                        //todo gas账户  credentials2账户
//                        String gasPrivateKey = getWalletPrivateKeyByTypeAndMerchantId(1, userCoinBalance.getMerchantId());
//                        if (StringUtils.isNotBlank(gasPrivateKey)){
//
//                            String decrypt = AES.decrypt(gasPrivateKey, initConfig.deskey);
//
//                            Credentials credentials2 = Credentials.create(decrypt);
//                            BigInteger nonce2 = web3j.ethGetTransactionCount(credentials2.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
//                            //需要的gas
//                            BigInteger needGasLimit = TransactionOnNode.getContractTransactionGasLimit(web3j, credentials.getAddress(), nonce, gasPrice);
//                            //gas账户打过来需要的提币的gas
//                            BigInteger ethTransactionGasLimit = TransactionOnNode.getEthTransactionGasLimit(web3j, credentials2.getAddress(), nonce2, gasPrice);
//
//                            //需要gas的eth
//                            BigDecimal needGas = CommonUtils.bit18(needGasLimit.multiply(gasPrice));
//
//                            String txHash = TransactionOnNode.transactionEth(web3j, credentials2.getEcKeyPair(), needGas.toPlainString(), credentials.getAddress(), gasPrice.toString(), ethTransactionGasLimit.toString(), "");
//
//                            //记录充值邮费交易订单
//                            TransactionOrder transactionOrder = new TransactionOrder();
//                            transactionOrder.setTxHash(txHash);
//                            transactionOrder.setCoinNum(needGas);
//                            transactionOrder.setCoinName(initConfig.gasName);
//                            transactionOrder.setFromAddress(credentials2.getAddress());
//                            transactionOrder.setToAddress(credentials.getAddress());
//                            transactionOrder.setCreatedTime(new Date());
//                            transactionOrder.setGasPrice(new BigDecimal(gasPrice.toString()));
//                            transactionOrder.setOrderStatus(RabbitRechargeConfig.ORDER_STATUS_PEEDING);
//                            transactionOrder.setTranscationType(RabbitRechargeConfig.ORDER_TYPE_GAS_RECHARGE);
//                            transactionOrderMapper.insertSelective(transactionOrder);
//                        }
//                    }
//
//
//
//                }
//            }
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    public String getWalletPrivateKeyByTypeAndMerchantId(int type,Integer merchantId) {
//
//        //查询该商户下所有邮费组
//        MerchantWalletGroupRelationExample merchantWalletGroupRelationExample = new MerchantWalletGroupRelationExample();
//
//        merchantWalletGroupRelationExample.or().andMerchantIdEqualTo(merchantId);
//
//        List<MerchantWalletGroupRelation> merchantWalletGroupRelations =
//                merchantWalletGroupRelationMapper.selectByExample(merchantWalletGroupRelationExample);
//
//
//        if (merchantWalletGroupRelations != null && merchantWalletGroupRelations.size()>0){
//            ArrayList<WalletGroupDetail> list = new ArrayList<>();
//            for (MerchantWalletGroupRelation mwgr : merchantWalletGroupRelations){
//
//                //查询所有邮费组
//                WalletGroup walletGroup = walletGroupMapper.selectByPrimaryKey(mwgr.getWalletGroupId());
//                if (walletGroup != null && walletGroup.getGroupType().intValue()==type){
//                    //查询地址
//                    WalletGroupDetailExample walletGroupDetailExample = new WalletGroupDetailExample();
//                    walletGroupDetailExample.or().andGroupIdEqualTo(walletGroup.getId());
//
//                    List<WalletGroupDetail> walletGroupDetailList = walletGroupDetailMapper.selectByExample(walletGroupDetailExample);
//
//                    if (walletGroupDetailList != null && walletGroupDetailList.size()>0){
//                        list.addAll(walletGroupDetailList);
//                    }
//                }
//
//            }
//
//            if (list.size()>0){
//                Random random = new Random();
//                int i = random.nextInt();
//                WalletGroupDetail walletGroupDetail = list.get(i % list.size());
//                return walletGroupDetail.getPrivateKey();
//            }
//
//
//        }
//
//        return null;
//
//
//    }
//
//}
