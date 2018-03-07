package com.coinwallet.common.web3j.utils;

import java.math.BigDecimal;

/**
 * Created by y on 2018/3/7.
 */
public class CommonUtils {



    public static String getAmount(String input) {
        if (input.startsWith("0x")) {
            input = input.replace("0x", "");
        }
        String hexAmount = input.substring(input.length() - 64, input.length());
        BigDecimal amount = new BigDecimal(Long.valueOf(hexAmount, 16));
        BigDecimal bigDecimal = amount.divide(new BigDecimal(100000000000000000d), 18, BigDecimal.ROUND_UP);
        return bigDecimal.toString();
    }
}

