package com.coinwallet.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by wanglong on 15/8/4.
 * http 请求工具类（包括post和get方法）
 */
public class HttpRequestUtil {

    private static final String UTF8 = "UTF-8";

    //将Map转成可以拼接成url的String
    private static String urlFix(Map<String, Object> paramMap) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();

        if (paramMap == null) {
            return "";
        } else {
            for (String key : paramMap.keySet()) {
                String value = String.valueOf(paramMap.get(key));
                if (sb.length() < 1) {
                    sb.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } else {
                    sb.append("&").append(key).append("=").append(value);
                }
            }
            return sb.toString();
        }
    }


    public static String getUtf8(Map<String, String> paramMap) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();

        if (paramMap == null) {
            return "";
        } else {
            for (String key : paramMap.keySet()) {
                String value = String.valueOf(paramMap.get(key));
                if (sb.length() < 1) {
                    sb.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } else {
                    sb.append("&").append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                }
            }
            return sb.toString();
        }
    }

    public static String doPost(String urlStr, Map<String, Object> params) {
        String result = "";
        HttpURLConnection conn = null;
        BufferedReader br = null;
        try {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), UTF8));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), UTF8));
            }
            byte[] postDataBytes = postData.toString().getBytes(UTF8);

            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF8));
            String line;
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String doPostJSON(String urlStr, Map<String, Object> params) {
        String result = "";
        Object jsonObject = JSONObject.toJSON(params);
        String postData = jsonObject.toString();
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(urlStr);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity entity = new ByteArrayEntity(postData.getBytes(UTF8));
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
                httpclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String patchRequest(String urlStr, Map<String, Object> params) {
        String result = "";
        Object jsonObject = JSONObject.toJSON(params);
        String postData = jsonObject.toString();
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpPatch httpPatch = new HttpPatch(urlStr);
            httpPatch.setHeader("Accept", "application/json");
            httpPatch.setHeader("Content-Type", "application/json");
            HttpEntity entity = new ByteArrayEntity(postData.getBytes(UTF8));
            httpPatch.setEntity(entity);
            response = httpclient.execute(httpPatch);
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
                httpclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static String doGet(String urlStr, Map<String, Object> paramMap) throws UnsupportedEncodingException {

        urlStr += "?" + urlFix(paramMap);
        String responseBody = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = generateHttpGet(urlStr);
            httpget.addHeader("Content-Type", "text/html;charset=UTF-8");
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity, UTF8) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            responseBody = httpclient.execute(httpget, responseHandler);
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseBody;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&amp;”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, Object> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuffer prestr = new StringBuffer();
        int size = keys.size() - 1;
        for (int i = 0; i < size; i++) {
            String key = keys.get(i);
            Object value = params.get(key);
            prestr.append(key).append("=").append(value).append("&");
        }
        String key = keys.get(size);
        Object value = params.get(key);
        prestr.append(key).append("=").append(value);//拼接时，不包括最后一个&字符
        return prestr.toString();
    }

    private static final int TOTAL_CONN = 50;  // Increase max total connection to 50
    private static final int DEFAULT_CONN = 10; // Increase default max connection per route to 10
    private static PoolingHttpClientConnectionManager poolConnManager;

    //初始化代码
    static {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        //初始化连接管理器
        poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolConnManager.setMaxTotal(TOTAL_CONN);
        poolConnManager.setDefaultMaxPerRoute(DEFAULT_CONN);
    }

    /**
     * @return 构造请求超时配置对象
     */
    private static RequestConfig generateRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
    }

    public static JSONObject doGetJSONObject(String urlStr, Map<String, Object> params) throws IOException {
        return defaultDoGet(urlStr, params, new DefaultJsonHandler());
    }

    public static String doGetString(String urlStr, Map<String, Object> params) throws IOException {
        return defaultDoGet(urlStr, params, new DefaultStringHandler());
    }

    public static Document doGetXML(String urlStr, Map<String, Object> params) throws IOException {
        return defaultDoGet(urlStr, params, new DefaultXmlHandler());
    }

    //发送请求 url
    private static <T> T defaultDoGet(String urlStr, Map<String, Object> params, ResponseHandler<T> handler)
            throws IOException {
        //参数检测
        if (urlStr == null || "".equals(urlStr)) {
            return null;
        }
        if (params != null) {
            urlStr += "?" + urlFix(params);
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager).build();
        HttpGet httpGet = generateHttpGet(urlStr);
        return httpClient.execute(httpGet, handler);
    }

    /**
     * post请求
     * head 为"Content-Type", "application/json")
     *
     * @param urlStr       传入json 字符串
     * @param postJSONData 响应内容自动转换为JSON object
     * @return
     */
    public static JSONObject doPostFromJSONtoJSON(String urlStr, String postJSONData)
            throws IOException {
        HttpPost httpPost = generateJSONHttpPost(urlStr);
        HttpEntity entity = new ByteArrayEntity(postJSONData.getBytes(UTF8));
        httpPost.setEntity(entity);
        return defaultDoPOST(httpPost, new DefaultJsonHandler());
    }

    public static JSONObject doPostFromJSONtoJSON(String urlStr, Map<String, Object> params)
            throws IOException {
        Object jsonObject = JSONObject.toJSON(params);
        String postJSONData = jsonObject.toString();
        return doPostFromJSONtoJSON(urlStr, postJSONData);
    }

    public static String doPostFromJSONtoString(String urlStr, String postJSONData)
            throws IOException {
        HttpPost httpPost = generateJSONHttpPost(urlStr);
        HttpEntity entity = new ByteArrayEntity(postJSONData.getBytes(UTF8));
        httpPost.setEntity(entity);
        return defaultDoPOST(httpPost, new DefaultStringHandler());
    }
    public static String doHeaderJsonPostFromJSONtoString(String urlStr, String postJSONData)
            throws IOException {
        HttpPost httpPost = generateHeaderJSONHttpPost(urlStr);
        HttpEntity entity = new ByteArrayEntity(postJSONData.getBytes(UTF8));
        httpPost.setEntity(entity);
        return defaultDoPOST(httpPost, new DefaultStringHandler());
    }

    public static String doPostFromJSONtoString(String urlStr, Map<String, Object> params)
            throws IOException {
        Object jsonObject = JSONObject.toJSON(params);
        String postJSONData = jsonObject.toString();
        return doPostFromJSONtoString(urlStr, postJSONData);
    }

    /**
     * 请求头为application/json
     * @param urlStr
     * @param params
     * @return
     * @throws IOException
     */
    public static String doPostHeaderJsonFromJSONtoString(String urlStr, Map<String, Object> params)
            throws IOException {
        Object jsonObject = JSONObject.toJSON(params);
        String postJSONData = jsonObject.toString();
        return doHeaderJsonPostFromJSONtoString(urlStr, postJSONData);
    }

    public static String doPostFromParamtoString(String urlStr, Map<String, Object> params)
            throws IOException {
        HttpPost httpPost = generateParamHttpPost(urlStr, params);
        return defaultDoPOST(httpPost, new DefaultStringHandler());
    }

    public static JSONObject doPostFromParamtoJSON(String urlStr, Map<String, Object> params)
            throws IOException {
        HttpPost httpPost = generateParamHttpPost(urlStr, params);
        return defaultDoPOST(httpPost, new DefaultJsonHandler());
    }

    private static <T> T defaultDoPOST(HttpPost httpPost, ResponseHandler<T> handler)
            throws IOException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager).build();
        return httpClient.execute(httpPost, handler);
    }

    public static String doPatchString(String urlStr, String patchJSONData) throws IOException {
        HttpPatch httpPatch = generateHttpPatch(urlStr);
        HttpEntity entity = null;
        try {
            entity = new ByteArrayEntity(patchJSONData.getBytes(UTF8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpPatch.setEntity(entity);
        return defaultDoPatch(httpPatch, new DefaultStringHandler());
    }

    public static String doPatchString(String urlStr, Map<String, Object> params) throws IOException {
        Object jsonObject = JSONObject.toJSON(params);
        String postData = jsonObject.toString();
        return doPatchString(urlStr, postData);
    }

    private static <T> T defaultDoPatch(HttpPatch httpPatch, ResponseHandler<T> handler)
            throws IOException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager).build();
        return httpClient.execute(httpPatch, handler);
    }

    private static abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {
        private final static int BUF_SIZE = 0x2000;

        private byte[] mBuffer = new byte[BUF_SIZE];

        public interface FailureCallback {
            void onException(Throwable msg);

            void onFailure(int statusCode, HttpResponse response);
        }

        private FailureCallback mCallback;

        public void setFailureCallbak(FailureCallback c) {
            mCallback = c;
        }

        protected abstract T readObject(byte[] transTemp) throws IOException, DocumentException;

        public T handleResponse(HttpResponse response) {
            if (response != null) {
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    try (InputStream is = response.getEntity().getContent()) {
                        if (is != null) {
                            JSONObject retJsonObj = null;
                            int bytesRead;
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                while ((bytesRead = is.read(mBuffer)) != -1) {
                                    baos.write(mBuffer, 0, bytesRead);
                                }
                                byte[] transTemp = baos.toByteArray();
                                return readObject(transTemp);
                            }
                        }
                    } catch (Exception e) {
                        if (mCallback != null)
                            mCallback.onException(e);
                        e.printStackTrace();
                    }
                } else {
                    if (mCallback != null)
                        mCallback.onFailure(statusCode, response);
                }
            } else {
                if (mCallback != null) {
                    mCallback.onException(new IllegalArgumentException("response is null"));
                }
            }

            return null;
        }
    }

    private static class DefaultJsonHandler extends AbstractResponseHandler<JSONObject> {
        private final static int BUF_SIZE = 0x2000;

        private byte[] mBuffer;

        public DefaultJsonHandler() {
            mBuffer = new byte[BUF_SIZE];
        }

        @Override
        protected JSONObject readObject(byte[] transTemp) throws IOException {
            return JSON.parseObject(new String(transTemp, UTF8));
        }

    }

    private static class DefaultStringHandler extends AbstractResponseHandler<String> {

        private final static int BUF_SIZE = 0x2000;

        private byte[] mBuffer;

        public DefaultStringHandler() {
            mBuffer = new byte[BUF_SIZE];
        }

        @Override
        protected String readObject(byte[] transTemp) throws UnsupportedEncodingException {
            return new String(transTemp, UTF8);
        }

        private final void close(OutputStream o) {
            if (o != null) {
                try {
                    o.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class DefaultXmlHandler extends AbstractResponseHandler<Document> {

        @Override
        protected Document readObject(byte[] transTemp)
                throws UnsupportedEncodingException, DocumentException {
            Document document = null;
            SAXReader reader = new SAXReader();
            return reader.read(new String(transTemp, UTF8));
        }
    }

    private static HttpGet generateHttpGet(String urlStr) {
        HttpGet httpGet = new HttpGet(urlStr);
        httpGet.setConfig(generateRequestConfig());
        return httpGet;
    }

    private static HttpPatch generateHttpPatch(String urlStr) {
        HttpPatch httpPatch = new HttpPatch(urlStr);
        httpPatch.setHeader("Accept", "application/json");
        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setConfig(generateRequestConfig());
        return httpPatch;
    }
    private static HttpPost generateHeaderJSONHttpPost(String urlStr) {
        HttpPost httpPost = new HttpPost(urlStr);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setConfig(generateRequestConfig());
        return httpPost;
    }
    private static HttpPost generateJSONHttpPost(String urlStr) {
        HttpPost httpPost = new HttpPost(urlStr);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setConfig(generateRequestConfig());
        return httpPost;
    }

    private static HttpPost generateParamHttpPost(String urlStr, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(urlStr);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        StringBuilder postData = new StringBuilder();
        try {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), UTF8));
                postData.append('=').append(URLEncoder.encode(String.valueOf(param.getValue()), UTF8));
            }
            byte[] postDataBytes = postData.toString().getBytes(UTF8);
            HttpEntity entity = new ByteArrayEntity(postDataBytes);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpPost.setHeader("Content-Length", String.valueOf(postData.length()));
        httpPost.setConfig(generateRequestConfig());
        return httpPost;
    }

    public static String getPlatform(HttpServletRequest request) {
        return request.getHeader("platform");
    }

    public static Integer getVersion(HttpServletRequest request) {
        String version = request.getHeader("version");
        String versionStr = "1";
        if (version != null) versionStr = version.replace(".", "");
        return Integer.parseInt(versionStr);
    }
}
