package com.coinwallet.common.web3j.utils;

import com.coinwallet.common.web3j.api.OWalletAPI;
import com.coinwallet.common.web3j.response.TransactionsResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by y on 2018/3/7.
 */
public class CommonUtils {
    static String testInput = "0xa9059cbb0000000000000000000000007e8247c7d145debe8a8c2d2a2ab450992aa884c900000000000000000000000000000000000000000000000000b1a2bc2ec50000";
//    String test


    String tempInfo = "http://api-ropsten.etherscan.io/api?module=account&action=txlist&address=0xd1bcbe82f40a9d7fbcbd28cca6043d72d66d8e9d&startblock=2747874&endblock=99999999&sort=asc&apikey=YourApiKeyToken";


    static String testAddress = "0xd1bcbe82f40a9d7fbcbd28cca6043d72d66d8e9d";
    static String testAddress1 = "0x4092678e4e78230f46a1534c0fbc8fa39780892b";
    static String startBlockNumber = "5210796";


    public static void main(String[] arg) {
//        String input = "000000000000000000000000000000000000000000000d042fdc68940ef00000";

//        String amount = getSTAmount(input);
//        System.out.println(amount);
        test();
    }

    private static void test() {
        String ss = "a9059cbb000000000000000000000000";
        String s1 = "7e8247c7d145debe8a8c2d2a2ab450992aa884c9";
        String s2 = "00000000000000000000000000000000000000000000000000b1a2bc2ec50000";

        System.out.println(ss.length());
        System.out.println(s1.length());
        System.out.println(s2.length());
        BigDecimal amount = new BigDecimal(Long.valueOf(s2, 16));
        BigDecimal bigDecimal = amount.divide(new BigDecimal(100000000000000000d), 18, BigDecimal.ROUND_UP);
        System.out.println(bigDecimal);


        List<TransactionsResponse.Result> transactionList = OWalletAPI.getTransactionList(testAddress1, startBlockNumber, "6210796");
        System.out.println(transactionList.size());

        if (transactionList.size() > 0) {
            for (TransactionsResponse.Result result : transactionList) {

                BigDecimal transactionAmount = result.getTransactionAmount();
                String walletAddress = result.getTransactionTo();
                System.out.println(transactionAmount + "__" + walletAddress);

            }
        }
    }


    public static BigDecimal getSTAmount(String input) {
        if (input.startsWith("0x")) {
            input = input.replace("0x", "");
        }
        String hexAmount = input.substring(input.length() - 64, input.length());
        BigDecimal amount = new BigDecimal(new BigInteger(hexAmount, 16).toString());
        BigDecimal bigDecimal = amount.divide(new BigDecimal(100000000000000000d), 18, BigDecimal.ROUND_UP);
        return bigDecimal;
    }
}

