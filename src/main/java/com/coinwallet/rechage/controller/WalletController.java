package com.coinwallet.rechage.controller;

import com.coinwallet.common.response.FailResponse;
import com.coinwallet.common.response.ResponseValue;
import com.coinwallet.rechage.controller.req.CreateWalletReq;
import com.coinwallet.rechage.schedule.CheckBalanceTask;
import com.coinwallet.rechage.service.WalletService;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;


    /**
     * 1001:create-wallet
     * localhost:9005/api/recharge/v1/create-wallet
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v1/create-wallet", method = RequestMethod.POST)
    public @ResponseBody
    ResponseValue createWallet(@RequestBody CreateWalletReq createWalletReq) {
        if (createWalletReq.getMerchantId() == null || StringUtils.isEmpty(createWalletReq.getIn()) || StringUtils.isEmpty(createWalletReq.getSeed())) {
            return new FailResponse(1001, "param error");
        }
        ResponseValue responseValue = walletService.initWallet(createWalletReq);
        return responseValue;
    }

    /**
     * 1001:create-wallet
     * localhost:9005/api/recharge/v1/create-wallet
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v1/change-balance", method = RequestMethod.POST)
    public @ResponseBody
    ResponseValue changeBalance(@RequestBody CreateWalletReq transactionReq) {
        if (transactionReq.getMerchantId() == null || StringUtils.isEmpty(transactionReq.getIn()) || StringUtils.isEmpty(transactionReq.getSeed())) {
            return new FailResponse(1001, "param error");
        }
        ResponseValue responseValue = walletService.changeBalace(transactionReq);
        return responseValue;
    }

    /**
     * 1001:create-wallet
     * localhost:9005/api/recharge/v1/create-wallet
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v1/transfer", method = RequestMethod.POST)
    public @ResponseBody
    ResponseValue transfer(@RequestBody CreateWalletReq transferReq) {
        if (transferReq.getMerchantId() == null || StringUtils.isEmpty(transferReq.getIn()) || StringUtils.isEmpty(transferReq.getSeed())) {
            return new FailResponse(1001, "param error");
        }
        ResponseValue responseValue = walletService.transfer(transferReq);
        return responseValue;
    }



}
