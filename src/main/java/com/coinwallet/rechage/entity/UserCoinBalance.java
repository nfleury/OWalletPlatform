package com.coinwallet.rechage.entity;

import java.math.BigDecimal;
import java.util.Date;

public class UserCoinBalance {
    private Integer id;

    private Integer userid;

    private Integer merchantId;

    private Integer coinId;

    private String coinName;

    private BigDecimal coinBalance;

    private BigDecimal showBalance;

    private Byte userStatus;

    private Date lastTradingTime;

    private Byte transferStatus;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getCoinId() {
        return coinId;
    }

    public void setCoinId(Integer coinId) {
        this.coinId = coinId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
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

    public Byte getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Byte userStatus) {
        this.userStatus = userStatus;
    }

    public Date getLastTradingTime() {
        return lastTradingTime;
    }

    public void setLastTradingTime(Date lastTradingTime) {
        this.lastTradingTime = lastTradingTime;
    }

    public Byte getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(Byte transferStatus) {
        this.transferStatus = transferStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}