package com.coinwallet.common.web3j.response;

import com.coinwallet.common.web3j.utils.CommonUtils;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class TransactionsResponse {

    private String status;
    private String message;
    private List<Transaction> result;

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

    public void setResult(List<Transaction> result) {
        this.result = result;
    }

    public List<Transaction> getResult() {
        return result;
    }


    public class Result {

        private String blockNumber;
        private String timeStamp;
        private String hash;
        private String nonce;
        private String blockHash;
        private String transactionIndex;
        private String from;
        private String to;
        private String value;
        private String gas;
        private String gasPrice;
        private String isError;
        private String txreceipt_status;
        private String input;
        private String contractAddress;
        private String cumulativeGasUsed;
        private String gasUsed;
        private String confirmations;

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getHash() {
            return hash;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getNonce() {
            return nonce;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getBlockHash() {
            return blockHash;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFrom() {
            return from;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getTo() {
            return to;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getGas() {
            return gas;
        }

        public void setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
        }

        public String getGasPrice() {
            return gasPrice;
        }

        public void setIsError(String isError) {
            this.isError = isError;
        }

        public String getIsError() {
            return isError;
        }

        public void setTxreceipt_status(String txreceipt_status) {
            this.txreceipt_status = txreceipt_status;
        }

        public String getTxreceipt_status() {
            return txreceipt_status;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getInput() {
            return input;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = cumulativeGasUsed;
        }

        public String getCumulativeGasUsed() {
            return cumulativeGasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setConfirmations(String confirmations) {
            this.confirmations = confirmations;
        }

        public String getConfirmations() {
            return confirmations;
        }


        //custom info about to address
        public String getTransactionTo() {
            if (input == null || "".equals(input)) return null;
            return "0x"+input.substring(34, input.length() - 64);

        }

        //custom info about contract amount
        public BigDecimal getTransactionAmount() {
            if (input == null || "".equals(input)) return null;
            return CommonUtils.getSTAmount(input);
        }


    }

}