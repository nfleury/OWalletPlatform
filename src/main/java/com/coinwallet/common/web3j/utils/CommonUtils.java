package com.coinwallet.common.web3j.utils;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by y on 2018/3/7.
 */
public class CommonUtils {



    public static String getSTAmount(String input) {
        if (input.startsWith("0x")) {
            input = input.replace("0x", "");
        }
        String hexAmount = input.substring(input.length() - 64, input.length());
        BigDecimal amount = new BigDecimal(new BigInteger(hexAmount, 16).toString());
        BigDecimal bigDecimal = amount.divide(new BigDecimal(100000000000000000d), 18, BigDecimal.ROUND_UP);
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


}

