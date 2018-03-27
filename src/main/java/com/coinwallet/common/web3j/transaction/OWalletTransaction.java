package com.coinwallet.common.web3j.transaction;

import com.alibaba.fastjson.JSON;
import com.coinwallet.common.util.Constants;
import com.coinwallet.common.web3j.api.EtherScanApi;
import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
import com.coinwallet.common.web3j.response.BlockInfoResponse;
import com.coinwallet.common.web3j.response.EtherScanResponse;
import com.coinwallet.common.web3j.response.TransactionReceiptResponse;
import com.coinwallet.common.web3j.response.TransactionsResponse;
import com.coinwallet.common.web3j.utils.OWalletUtils;
import com.coinwallet.common.web3j.utils.RawTransactionUtils;
import com.coinwallet.common.web3j.utils.RequestUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static com.coinwallet.common.web3j.api.EtherScanApi.*;
import static com.coinwallet.common.web3j.utils.CommonUtils.Hex2Decimal;
import static org.web3j.abi.Utils.convert;

/**
 * Created by y on 2018/3/5.
 */
public class OWalletTransaction {

    static Logger logger = LoggerFactory.getLogger(OWalletTransaction.class);

    /**
     * @param walletAddress
     * @return
     */
    public static String balanceOfETH(String walletAddress) {
        String responseResult = RequestUtils.sendGet(EtherScanApi.getBalanceUrl(walletAddress));
        responseResult = responseResult.replace("/n", "");
        EtherScanResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });
        String balanceOfETH = new BigDecimal(responseToken.result).divide(new BigDecimal(1000000000000000000d), 7, BigDecimal.ROUND_UP).toPlainString();
        return balanceOfETH;

    }


    /**
     * @param walletAddress
     * @param contractAddress
     */
    public static String balanceOfContractToken(String walletAddress, String contractAddress) {
        Function function = new Function("balanceOf",
                Arrays.<Type>asList(new Address(walletAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        String data = FunctionEncoder.encode(function);
        String url = eth_call(contractAddress, data);
        String responseResult = RequestUtils.sendGet(url);
        responseResult = responseResult.replace("/n", "");

        EtherScanResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });
        String result = responseToken.result;
        if (!"".equals(result) && result.startsWith("0x")) {
            List<TypeReference<?>> typeReferences = Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            });
            List<Type> decode = FunctionReturnDecoder.decode(result, convert(typeReferences));
            BigDecimal ethbal = new BigDecimal(decode.get(0).getValue().toString());
            BigDecimal divide = ethbal.divide(new BigDecimal(1000000000000000000d), 3, BigDecimal.ROUND_UP);
            return divide.toString();
        }
        throw new RuntimeException("get token fail");
    }


    /**
     * @param ethAmount eg:"0.05";//(实际额为0.05 Eth)
     * @param toAddress eg:"0x7e8247c7d145debe8a8c2d2a2ab450992aa884c9";
     * @param gas_price eg:"30000000000";
     * @param gas_limit eg:"200000"
     * @param data      default ""
     * @param ecKeyPair
     * @return txHex
     * @throws IOException
     * @throws InterruptedException
     */
    public static String transactionEth(ECKeyPair ecKeyPair, String ethAmount, String toAddress, String gas_price, String gas_limit, String data) throws IOException, InterruptedException {
        Credentials credentials = Credentials.create(ecKeyPair);
        String walletAddress = credentials.getAddress();
        String responseResult = RequestUtils.sendGet(getNonceForAddress(walletAddress));
        responseResult = responseResult.replace("/n", "");
        EtherScanResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });
        BigInteger nonce = new BigInteger(responseToken.result.substring(2), 16);
        RawTransaction tx = RawTransactionUtils.getTransaction(nonce, null, ethAmount, gas_price, gas_limit, data, toAddress);

        byte[] signed = TransactionEncoder.signMessage(tx, (byte) EtherScanApi.CHAIN_ID, credentials);
        String url = forwardTransaction("0x" + Hex.toHexString(signed));
        //进行交易需要先获得nonce,该账号的交易次数
        String transactionResp = RequestUtils.sendGet(url);
        transactionResp = transactionResp.replace("/n", "");
        EtherScanResponse txHashResponse = JSON.parseObject(transactionResp, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });

        return txHashResponse.result;

    }


    /**
     * @param ecKeyPair
     * @param OCNAmount
     * @param toAddress
     * @param gas_price
     * @param gas_limit
     * @param data
     * @param ERC20Address
     * @return
     * @throws IOException
     * @throws InterruptedException
     */

    public static String transactionOnContract(ECKeyPair ecKeyPair, String OCNAmount, String toAddress, String gas_price, String gas_limit, String data, String ERC20Address) throws IOException, InterruptedException {
        Credentials credentials = Credentials.create(ecKeyPair);
        String walletAddress = credentials.getAddress();
        String responseResult = RequestUtils.sendGet(getNonceForAddress(walletAddress));
        responseResult = responseResult.replace("/n", "");
        EtherScanResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });
        BigInteger nonce = new BigInteger(responseToken.result.substring(2), 16);
        RawTransaction tx = RawTransactionUtils.getTransaction(nonce, ERC20Address, OCNAmount, gas_price, gas_limit, data, toAddress);

        byte[] signed = TransactionEncoder.signMessage(tx, (byte) EtherScanApi.CHAIN_ID, credentials);
        String url = forwardTransaction("0x" + Hex.toHexString(signed));
        String transactionResp = RequestUtils.sendGet(url);
        transactionResp = transactionResp.replace("/n", "");
        EtherScanResponse txHashResponse = JSON.parseObject(transactionResp, new com.alibaba.fastjson.TypeReference<EtherScanResponse>() {
        });
        return txHashResponse.result;

    }


    public static final String txHash = "0x1a763acca69a048d413311bdf2ce795b01415c5220892dcb5765a47fce559143";


    /**
     * @param txHash
     * @return
     */
    public static TransactionReceiptResponse transactionReceipt(String txHash) {
        String url = getTransactionReceipt(txHash);
        String responseResult = RequestUtils.sendGet(url);
        responseResult = responseResult.replace("/n", "");
        TransactionReceiptResponse transactionReceiptResponse = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<TransactionReceiptResponse>() {

        });
        return transactionReceiptResponse;

    }


    /**
     * @param address
     * @param startBlockNumber
     * @param endBlockNumber   eg:99999999
     * @return
     */
    public static List<TransactionsResponse.CustomTransaction> getTransactionList(String address, String startBlockNumber, String endBlockNumber) throws Exception {
        String url = transactions_by_address(address, startBlockNumber, endBlockNumber);
        String responseResult = RequestUtils.sendGet(url);
        responseResult = responseResult.replace("/n", "");
        TransactionsResponse transactionResponse = null;
        try {
            transactionResponse = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<TransactionsResponse>() {
            });
        } catch (Exception e) {

            logger.error(String.format("扫快失败,url:{%s},responseResult:{%s}", url, responseResult), e);
            throw new Exception("扫快失败");
        }
        return (transactionResponse == null) ? null : transactionResponse.getResult();

    }


    /**
     * @return
     */
    public static BigInteger getRecentBlockNumber() {
        String responseResult = RequestUtils.sendGet(getEthRecentBlockNumber());
        responseResult = responseResult.replace("/n", "");
        com.coinwallet.common.web3j.response.EtherScanResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<com.coinwallet.common.web3j.response.EtherScanResponse>() {
        });
        if (responseToken == null) {
            return null;
        }

        return new BigInteger(responseToken.result.replace("0x", ""), 16);
    }

    /**
     * 双重转代币
     * @param web3j
     * @param address
     * @param ecKeyPair
     * @param ocnAmount
     * @param gas_price
     * @param gas_limit
     * @param contractAddress
     * @param data
     * @return
     */
    public static String doubleTransactionCoin(Web3j web3j, String address, ECKeyPair ecKeyPair, String ocnAmount, String gas_price, String gas_limit, String contractAddress, String data) {
        try {
            return TransactionOnNode.transactionOnContract(web3j, ecKeyPair, ocnAmount, address, gas_price, gas_limit, data, contractAddress);
        } catch (Exception e) {
            try {
                return OWalletTransaction.transactionOnContract(ecKeyPair, ocnAmount, address, gas_price, gas_limit, data, contractAddress);
            } catch (Exception e1) {
                logger.error(e.getMessage(),e);
                return Constants.TRANSFER_ERROR;
            }
        }

    }

    public static String doubleTransactionETH(Web3j web3j, String address, ECKeyPair ecKeyPair, String ETHAmount, String gas_price, String gas_limit,  String data) {
        try {
            return TransactionOnNode.transactionEth(web3j, ecKeyPair, ETHAmount, address, gas_price, gas_limit, data);
        } catch (Exception e) {
            try {
                return OWalletTransaction.transactionEth(ecKeyPair, ETHAmount, address, gas_price, gas_limit, data);
            } catch (Exception e1) {

                return Constants.TRANSFER_ERROR;
            }
        }

    }



    /**
     * @param blockNo
     * @return
     */
    public static BlockInfoResponse getBlockInfo(String blockNo) {
        String responseResult = RequestUtils.sendGet(getEthRecentBlockInfo(blockNo));
        responseResult = responseResult.replace("/n", "");

        BlockInfoResponse responseToken = JSON.parseObject(responseResult, new com.alibaba.fastjson.TypeReference<BlockInfoResponse>() {
        });
        return responseToken;
    }


    /**
     * @param txHash
     * @return
     * @throws IOException
     */
    public static TransactionVerificationInfo verifyTransaction(String txHash) {
        try {
            Thread.sleep(100);
            BigInteger recentBlockNumber = OWalletAPI.getRecentBlockNumber();
            TransactionReceiptResponse transactionReceiptResponse = OWalletTransaction.transactionReceipt(txHash);
            if (recentBlockNumber == null || transactionReceiptResponse == null || transactionReceiptResponse.getResult() == null || transactionReceiptResponse.getResult().getBlockNumber() == null) {
                logger.info("根据txhash获取交易为空：txhash=", txHash);
                return null;
            }

            String blockNumberRaw = transactionReceiptResponse.getResult().getBlockNumber();
            String gasUsed = transactionReceiptResponse.getResult().getGasUsed();
            BigDecimal gasUsed_B = new BigDecimal(Hex2Decimal(gasUsed).toString());
            if (blockNumberRaw == null) return null;
            BigInteger txBlockNumber = blockNumberRaw.startsWith("0x") ? Hex2Decimal(blockNumberRaw) : new BigInteger(blockNumberRaw);
            boolean isConfirm12 = OWalletUtils.verify12Block(txBlockNumber, recentBlockNumber);
            boolean statusIsSuccess = "0x1".equals(transactionReceiptResponse.getResult().getStatus());
            BlockInfoResponse blockInfo = OWalletTransaction.getBlockInfo(txBlockNumber.toString());
            Long timeStamp = new Long(blockInfo.getResult().getTimeStamp());
            return new TransactionVerificationInfo(isConfirm12 && statusIsSuccess, timeStamp, gasUsed_B);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }


}