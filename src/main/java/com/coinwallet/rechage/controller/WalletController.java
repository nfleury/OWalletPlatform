package com.coinwallet.rechage.controller;

import com.coinwallet.common.response.ResponseValue;
import com.coinwallet.rechage.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * create wallet by password
     * @param walletReq
     * @return
     */
    @PostMapping("/initialization")
    public ResponseValue createdWallet(@RequestBody WalletReq walletReq){

        //请求需要加解密

        boolean walletResult = walletService.createWallet(walletReq.getPassword(),walletReq.getMemberId());


        //返回私钥文件
        return null;

    }





}
