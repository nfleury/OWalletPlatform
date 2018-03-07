package com.coinwallet.rechage.service;

import com.coinwallet.common.util.AES;
import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.bean.WalletInfo;
import com.coinwallet.rechage.controller.req.CreateWalletReq;
import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.entity.MerchantInfo;
import com.coinwallet.rechage.entity.UserCoinBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RechargeService {

    @Autowired(required = true)
    UserCoinBalanceMapper userCoinBalanceMapper;
    /**
 * 创建钱包
 */
    public UserCoinBalance initUserCoinWallet(int userid, String privateKey, String address, String coinName){
        UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByPrimaryKey(userid);
        if(userCoinBalance==null){
            userCoinBalance =  new UserCoinBalance();
        }else{
            return  null;
        }
        userCoinBalance.setCoinAddress(address);
        userCoinBalance.setCoinName(coinName);
        userCoinBalance.setPrivatekey(privateKey);
        userCoinBalance.setUserid(userid);
        userCoinBalanceMapper.insertSelective(userCoinBalance);
        return  userCoinBalance;
    }


    /**
     * 创建钱包
     * @param createWalletReq 用户id
     * @param merchantInfo 商户信息
     */
    public UserCoinBalance initUserCoinWallet1(CreateWalletReq createWalletReq, MerchantInfo merchantInfo) {
        UserCoinBalance userCoinBalance = userCoinBalanceMapper.selectByPrimaryKey(createWalletReq.getUserid());
        if(userCoinBalance==null){
            userCoinBalance =  new UserCoinBalance();
        }else{
            return  null;
        }
        try {
            WalletInfo walletInfo = OWalletAPI.generateWallet();
            userCoinBalance.setCoinAddress(walletInfo.getWalletAddress());
            userCoinBalance.setCoinName(merchantInfo.getMerchantName());
            userCoinBalance.setMerchantId(merchantInfo.getId());
            userCoinBalance.setPrivatekey(AES.encrypt(walletInfo.getPrivateKey(),AES.DESKEY));
            userCoinBalance.setUserid(createWalletReq.getUserid());
            userCoinBalanceMapper.insertSelective(userCoinBalance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return userCoinBalance;
    }
}
