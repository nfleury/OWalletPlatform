package com.coinwallet.rechage.service;

import com.coinwallet.rechage.dao.UserCoinBalanceMapper;
import com.coinwallet.rechage.entity.UserCoinBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RechargeService {

    @Autowired
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






}
