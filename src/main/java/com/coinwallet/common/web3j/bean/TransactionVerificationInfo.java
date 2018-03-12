package com.coinwallet.common.web3j.bean;

/**
 * Created by y on 2018/3/10.
 */
public class TransactionVerificationInfo {

    boolean isVerification;

    long timeStamp;

    public TransactionVerificationInfo(boolean isVerification, long timeStamp) {
        this.isVerification = isVerification;
        this.timeStamp = timeStamp;
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
