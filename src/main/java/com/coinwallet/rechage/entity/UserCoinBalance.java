package com.coinwallet.rechage.entity;

import java.math.BigDecimal;

public class UserCoinBalance {
    private Integer userid;

    private Integer merchantId;

    private String coinName;

    private String coinAddress;

    private String privatekey;

    private BigDecimal coinBalance;

    private BigDecimal showBalance;

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public BigDecimal getCoinBalance() {
        return coinBalance;
    }

    public void setCoinBalance(BigDecimal coinBalance) {
        this.coinBalance = coinBalance;
    }

    public BigDecimal getShowBalance() {
        return showBalance;
    }

    public void setShowBalance(BigDecimal showBalance) {
        this.showBalance = showBalance;
    }
}