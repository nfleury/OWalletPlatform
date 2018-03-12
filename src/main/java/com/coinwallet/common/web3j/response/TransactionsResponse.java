package com.coinwallet.common.web3j.response;

import org.web3j.protocol.core.methods.response.Transaction;

import java.util.List;

public class TransactionsResponse {

    private String status;
    private String message;
    private List<CustomTransaction> result;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setResult(List<CustomTransaction> result) {
        this.result = result;
    }

    public List<CustomTransaction> getResult() {
        return result;
    }


    public class CustomTransaction extends Transaction {

        private String timeStamp;

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getTimeStamp() {
            return timeStamp;
        }


    }

}