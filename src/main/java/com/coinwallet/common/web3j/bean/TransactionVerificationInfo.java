package com.coinwallet.common.web3j.bean;

import java.math.BigDecimal;

/**
 * Created by y on 2018/3/10.
 */
public class TransactionVerificationInfo {

    boolean isVerification;

    long timeStamp;

    BigDecimal gasUsed;

    public TransactionVerificationInfo(boolean isVerification, long timeStamp, BigDecimal gasUsed) {
        this.isVerification = isVerification;
        this.timeStamp = timeStamp;
        this.gasUsed = gasUsed;
    }

    public TransactionVerificationInfo(boolean isVerification, long timeStamp) {
        this.isVerification = isVerification;
        this.timeStamp = timeStamp;
    }


    public BigDecimal getGasUsed() {
        return gasUsed;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isVerification() {
        return isVerification;
    }

    public void setVerification(boolean verification) {
        isVerification = verification;
    }
}
