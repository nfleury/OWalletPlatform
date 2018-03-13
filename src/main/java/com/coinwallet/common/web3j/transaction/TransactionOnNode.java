package com.coinwallet.common.web3j.transaction;

import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
import com.coinwallet.common.web3j.service.CustomNodeService;
import com.coinwallet.common.web3j.utils.CommonUtils;
import com.coinwallet.common.web3j.utils.RawTransactionUtils;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
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
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.TransactionManager;
import rx.functions.Func1;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static com.coinwallet.common.web3j.utils.CommonUtils.Hex2Decimal;
import static com.coinwallet.common.web3j.utils.CommonUtils.bit18;

/**
 * Created by y on 2018/3/7.
 */
public class TransactionOnNode {


    /**
     * @param web3j
     * @param contractAddress
     * @param walletAddress
     * @return
     * @throws IOException
     */
    public static BigDecimal balanceOfContractToken(Web3j web3j, String contractAddress, String walletAddress) throws IOException {
        Function function = new Function("balanceOf",
                Arrays.<Type>asList(new Address(walletAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        TransactionManager transactionManager = new TransactionManager(web3j, walletAddress) {
            @Override
            public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
                return null;
            }
        };
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall ethCall = web3j.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        transactionManager.getFromAddress(), contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .send();
        return new BigDecimal(CommonUtils.Hex2Decimal(ethCall.getValue()));
    }


    /**
     * @param web3j
     * @param walletAddress
     * @return
     * @throws IOException
     */
    public static BigDecimal balanceOfETH(Web3j web3j, String walletAddress) throws IOException {
        EthGetBalance ethGetBalance = web3j.ethGetBalance(walletAddress,
                DefaultBlockParameterName.LATEST).send();
        return CommonUtils.bit18(ethGetBalance.getBalance());


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
    public static String transactionEth(Web3j web3j, ECKeyPair ecKeyPair, String ethAmount, String toAddress, String gas_price, String gas_limit, String data) throws IOException, InterruptedException {
        Credentials credentials = Credentials.create(ecKeyPair);
        String walletAddress = credentials.getAddress();
        EthGetTransactionCount nonce = web3j.ethGetTransactionCount(walletAddress, DefaultBlockParameterName.LATEST).send();
        RawTransaction tx = RawTransactionUtils.getTransaction(nonce.getTransactionCount(), null, ethAmount, gas_price, gas_limit, data, toAddress);

        System.out.println("Nonce:" + tx.getNonce() + "\n" +
                "gasPrice: " + tx.getGasPrice() + "\n" +
                "gasLimit: " + tx.getGasLimit() + "\n" +
                "To: " + tx.getTo() + "\n" +
                "Amount: " + tx.getValue() + "\n" +
                "Data: " + tx.getData());
        byte[] signed = TransactionEncoder.signMessage(tx, credentials);
        String transactionData = "0x" + Hex.toHexString(signed);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(transactionData).send();
        return ethSendTransaction.getTransactionHash();

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

    public static String transactionOnContract(Web3j web3j, ECKeyPair ecKeyPair, String OCNAmount, String toAddress, String gas_price, String gas_limit, String data, String ERC20Address) throws IOException, InterruptedException {
        Credentials credentials = Credentials.create(ecKeyPair);
        String walletAddress = credentials.getAddress();
        EthGetTransactionCount nonce = web3j.ethGetTransactionCount(walletAddress, DefaultBlockParameterName.LATEST).send();
        RawTransaction tx = RawTransactionUtils.getTransaction(nonce.getTransactionCount(), ERC20Address, OCNAmount, gas_price, gas_limit, data, toAddress);
        byte[] signed = TransactionEncoder.signMessage(tx, credentials);
        String transactionData = "0x" + Hex.toHexString(signed);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(transactionData).send();
        return ethSendTransaction.getTransactionHash();
    }


    /**
     * @param web3j
     * @param contractAddress
     */
    public static void subscribeContractAddress(Web3j web3j, String contractAddress) {
        web3j.transactionObservable().map(tx -> tx).filter(new Func1<Transaction, Boolean>() {
            @Override
            public Boolean call(Transaction transaction) {
                return contractAddress.equals(transaction.getTo());
            }
        }).forEach(tx -> {
            //todo transaction parse
            System.out.println("tx-getHash:" + tx.getHash());
            System.out.println("tx-getFrom:" + tx.getFrom());
            System.out.println("tx-getTo:" + tx.getTo());
            System.out.println("tx-getNonce:" + tx.getNonce());
            System.out.println("tx-getGasPrice:" + tx.getGasPrice());
            System.out.println("tx-getGas:" + tx.getGas());
            System.out.println("tx-getInput:" + tx.getInput());
            System.out.println("tx-getValue:" + tx.getValue());
            System.out.println("tx-getGas:" + tx.getGas());
            String stAmount = CommonUtils.getSTAmount(tx.getInput());
            String contractAddressTo = CommonUtils.getContractAddressTo(tx.getInput());
            System.out.println("tx-getTokenAmount:" + stAmount);
            System.out.println("tx-getAddressTo:" + contractAddressTo);
        });
    }

    /**
     * get the transaction Receipt
     *
     * @param web3j
     * @param txHash
     * @return
     * @throws IOException
     */
    public static TransactionReceipt getTransactionReceipt(Web3j web3j, String txHash) throws IOException {
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
        return receipt.getResult();
    }


    /**
     * @param web3j
     * @return
     * @throws IOException
     */
    public static BigInteger getGasPrice(Web3j web3j) throws IOException {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        return ethGasPrice.getGasPrice();
    }


    /**
     * @param web3j
     * @param fromAddress
     * @param nonce
     * @param gasPrice
     * @param gasLimit
     * @param addressTo
     * @param amount
     * @param data
     * @return
     * @throws IOException
     */
    public static BigInteger getAmountUsedGas(Web3j web3j, String fromAddress, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String addressTo, String amount, String data) throws IOException {
        EthEstimateGas gas = web3j.ethEstimateGas(new org.web3j.protocol.core.methods.request.Transaction(fromAddress, nonce, gasPrice, gasLimit, addressTo, new BigInteger(amount), data)).send();
        return gas.getAmountUsed();
    }


    /**
     * @param web3j
     * @return
     * @throws IOException
     */
    public static BigInteger getRecentBlockNumber(Web3j web3j) throws IOException {
        EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
        return ethBlockNumber.getBlockNumber();


    }


    /**
     * @param web3j
     * @param from
     * @param nonce
     * @param gasPrice
     * @return
     * @throws IOException
     */
    public static BigInteger getEthTransactionGasLimit(Web3j web3j, String from, BigInteger nonce, BigInteger gasPrice) throws IOException {

        EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(
                org.web3j.protocol.core.methods.request.Transaction.createContractTransaction(
                        from, nonce,
                        gasPrice, "")).send();
        return ethEstimateGas.getAmountUsed();
    }


    /**
     * @param web3j
     * @param from
     * @param nonce
     * @param gasPrice
     * @return
     * @throws IOException
     */
    public static BigInteger getContractTransactionGasLimit(Web3j web3j, String from, BigInteger nonce, BigInteger gasPrice) throws IOException {
        EthEstimateGas gasContract = web3j.ethEstimateGas(
                org.web3j.protocol.core.methods.request.Transaction.createContractTransaction(
                        from, nonce,
                        gasPrice, "")).send();
        return gasContract.getAmountUsed();
    }


    public static EthBlock getBlockBuNumber(Web3j web3j, BigInteger blockNumber) throws IOException {
        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
        return ethBlock;
    }

    /**
     * @param txHash
     * @return
     * @throws IOException
     */
    public static TransactionVerificationInfo verifyTransaction(String txHash) throws IOException {
        Web3j web3j = Web3j.build(new CustomNodeService());
        BigInteger recentBlockNumber = getRecentBlockNumber(web3j);
        System.out.println("txHash" + txHash);
        TransactionReceipt transactionReceipt = getTransactionReceipt(web3j, txHash);
        String blockNumberRaw = transactionReceipt.getBlockNumber().toString();
        String gasUsed = transactionReceipt.getGasUsed().toString();
        BigDecimal gasUsed_B = bit18(Hex2Decimal(gasUsed));
        if (blockNumberRaw == null) return null;
        BigInteger txBlockNumber;
        if (blockNumberRaw.startsWith("0x")) {
            txBlockNumber = Hex2Decimal(blockNumberRaw);
        } else {
            txBlockNumber = new BigInteger(blockNumberRaw);
        }
        boolean isConfirm12 = recentBlockNumber.compareTo(txBlockNumber.add(new BigInteger("12"))) > 0;
        boolean statusIsSuccess = "0x1".equals(transactionReceipt.getStatus());
        EthBlock block = getBlockBuNumber(web3j, txBlockNumber);
        Long timeStamp = new Long(block.getBlock().getTimestamp().toString());
        return new TransactionVerificationInfo(isConfirm12 && statusIsSuccess, timeStamp, gasUsed_B);
    }

}
