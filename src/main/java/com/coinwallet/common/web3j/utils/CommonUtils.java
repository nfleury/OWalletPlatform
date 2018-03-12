package com.coinwallet.common.web3j.utils;

import com.coinwallet.common.web3j.bean.TransactionVerificationInfo;
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

    static String test_OCoin = "0x44c93945Be58d30D89643ECdeC1e3C8005cd2413";

    public static void main(String[] arg) throws IOException {

        TransactionVerificationInfo transactionVerificationInfo = OWalletTransaction.verifyTransaction(txHash_1);
        System.out.println("status" + transactionVerificationInfo.isVerification());
        System.out.println("status" + transactionVerificationInfo.getGasUsed().toString());

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


}

