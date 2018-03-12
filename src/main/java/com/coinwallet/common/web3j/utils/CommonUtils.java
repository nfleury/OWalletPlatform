package com.coinwallet.common.web3j.utils;

import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
import com.coinwallet.common.web3j.response.BlockInfoResponse;
import com.coinwallet.common.web3j.response.TransactionReceiptResponse;
import com.coinwallet.common.web3j.transaction.OWalletTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by y on 2018/3/7.
 */
public class CommonUtils {
    static String ss = "0xe70b0c5fb00092e7039de36058867b6217ec3ac7f9d54bb6723f6457854c2343";
    static String txHash_1 = "0x4c1c2e26df7a0356333ff4493bc3b90d48c7a88ec7ea719f7009a1e925a39289";

    public static void main(String[] arg) throws IOException {

        TransactionVerificationInfo transactionVerificationInfo = verifyTransaction(txHash_1);
        System.out.println("status"+transactionVerificationInfo.isVerification());
        System.out.println("status"+transactionVerificationInfo.getGasUsed().toString());

    }

    public static String getSTAmount(String input) {
        if (input.startsWith("0x")) {
            input = input.replace("0x", "");
        }
        String hexAmount = input.substring(input.length() - 64, input.length());
        BigDecimal amount = new BigDecimal(new BigInteger(hexAmount, 16).toString());
        BigDecimal bigDecimal = amount.divide(new BigDecimal(1000000000000000000d), 18, BigDecimal.ROUND_UP);
        return bigDecimal.toString();
    }

    public static String getContractAddressTo(String input) {
        if (input == null || "".equals(input)) return null;
        return "0x" + input.substring(34, input.length() - 64);
    }


    public static BigDecimal bit18(BigInteger bigInteger) {
        BigDecimal amount = new BigDecimal(bigInteger);
        BigDecimal bigDecimal = amount.divide(new BigDecimal(1000000000000000000d), 18, BigDecimal.ROUND_UP);

        return bigDecimal;
    }

    /**
     * @param transactionReceipt
     * @return
     * @throws IOException
     */
    public static boolean verifyTransaction(TransactionReceipt transactionReceipt, BigInteger ethBlockNumber) throws IOException {
        String status = transactionReceipt.getStatus();
        boolean statusIsSuccess = "0x1".equals(status);
        boolean verifyTrain = ethBlockNumber.subtract(transactionReceipt.getBlockNumber()).compareTo(new BigInteger("12")) > 0;
        return statusIsSuccess && verifyTrain;
    }


    /**
     * @param x
     * @return
     */
    public static BigInteger Hex2Decimal(String x) {
        if (x.startsWith("0x")) {
            x = x.replace("0x", "");
        }
        return new BigInteger(x, 16);
    }


    /**
     * 为了确保交易安全,通过两套机制验证.
     *
     * @param txHash
     * @return
     * @throws IOException
     */
    public static TransactionVerificationInfo verifyTransaction(String txHash) throws IOException {
        BigInteger recentBlockNumber = OWalletAPI.getRecentBlockNumber();
        TransactionReceiptResponse transactionReceiptResponse = OWalletTransaction.transactionReceipt(txHash);
        String blockNumberRaw = transactionReceiptResponse.getResult().getBlockNumber();
        String gasUsed = transactionReceiptResponse.getResult().getGasUsed();
        BigDecimal gasUsed_B = bit18(Hex2Decimal(gasUsed));
        if (blockNumberRaw == null) return null;
        BigInteger txBlockNumber;
        if (blockNumberRaw.startsWith("0x")) {
            txBlockNumber = CommonUtils.Hex2Decimal(blockNumberRaw);
        } else {
            txBlockNumber = new BigInteger(blockNumberRaw);
        }
        boolean isConfirm12 = recentBlockNumber.compareTo(txBlockNumber.add(new BigInteger("12"))) > 0;
        boolean statusIsSuccess = "0x1".equals(transactionReceiptResponse.getResult().getStatus());
        BlockInfoResponse blockInfo = OWalletTransaction.getBlockInfo(txBlockNumber.toString());
        Long timeStamp = new Long(blockInfo.getResult().getTimeStamp());
        return new TransactionVerificationInfo(isConfirm12 && statusIsSuccess, timeStamp, gasUsed_B);
    }


}

