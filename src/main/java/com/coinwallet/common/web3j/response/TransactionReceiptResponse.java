package com.coinwallet.common.web3j.response;

import org.web3j.protocol.core.methods.response.Transaction;

public class TransactionReceiptResponse {

    private String jsonrpc;
    private int id;
    private Transaction result;

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setResult(Transaction result) {
        this.result = result;
    }

    public Transaction getResult() {
        return result;
    }

}