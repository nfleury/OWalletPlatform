package com.coinwallet.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HttpClient工具类
 * Created by tlw on 2017/5/5.
 */
public class HttpClientUtil {
    private Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    /**
     * 执行post请求
     * 可关闭
     * 设超时
     * @return
     */
    public boolean executeRequest(HttpPost post) {
        // set the connection timeout value to 30 seconds (30000 milliseconds)
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        try {
            CloseableHttpResponse response = client.execute(post);
            try {
                boolean result = isOk(response);
                return result;
            } finally {
                response.close();
            }
        } catch (IOException e) {
            logger.error(e.toString());
            return false;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
    }
    public String executeRequest(HttpGet get) {
        // set the connection timeout value to 30 seconds (30000 milliseconds)
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        String responseContent;
        try {
            CloseableHttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity,"utf-8");
            return responseContent;
        } catch (IOException e) {
            logger.error(e.toString());
            return null;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
    }

    public boolean isOk(HttpResponse response) {
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }
        return false;
    }
}
