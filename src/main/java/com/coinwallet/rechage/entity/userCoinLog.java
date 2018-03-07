package com.coinwallet.rechage.entity;

import java.math.BigDecimal;

public class UserCoinLog {
    private Integer id;

    private Integer merchantId;

    private Integer userid;

    private Integer changeType;

    private BigDecimal changeNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public BigDecimal getChangeNum() {
        return changeNum;
    }

    public void setChangeNum(BigDecimal changeNum) {
        this.changeNum = changeNum;
    }
}