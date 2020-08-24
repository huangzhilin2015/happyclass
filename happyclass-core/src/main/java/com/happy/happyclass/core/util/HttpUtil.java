package com.happy.happyclass.core.util;

import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author huangzhilin
 * Date 2019/12/18
 */
public class HttpUtil {
    private static HttpUtil instance;

    private OkHttpClient okHttpClient;

    private static final Object locker = new Object();

    private HttpUtil() {
        /**
         * 构建OkHttpClient
         */
        OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS);
        builder.sslSocketFactory(createSSLSocketFactory());
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        okHttpClient = builder.build();
    }

    /**
     * 单例模式
     *
     * @return
     */
    private static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        return instance;
    }

    /**
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {

        }
        return ssfFactory;
    }


    /**
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    public static String postJsonData(String url, String data) throws IOException {
        return getInstance().postDataRequest(url, "application/json;charset=utf-8", data).string();
    }

    /**
     * @param url
     * @return
     * @throws IOException
     */
    public static String get(String url) throws IOException {
        return getInstance().getRequest(url, null).string();
    }

    public static InputStream postFormData(String url, Map<String, String> params) throws IOException {
        return getInstance().postFormRequest(url, params).byteStream();
    }

    /**
     * 构造post表单请求
     *
     * @param url
     * @param params
     */
    private ResponseBody postFormRequest(String url, Map<String, String> params) throws IOException {
        Request request;
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            Iterator it = params.keySet().iterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                String value = params.get(name);
                builder.add(name, value);
            }
        }
        RequestBody requestBody = builder.build();
        request = new Request.Builder().addHeader("Connection", "close").url(url).post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body();
    }

    /**
     * @param url
     * @param headerMap
     * @return
     * @throws IOException
     */
    private ResponseBody getRequest(String url, Map<String, String> headerMap) throws IOException {
        Request.Builder builder = new Request.Builder().addHeader("Connection", "close");
        if (headerMap != null && headerMap.size() > 0) {
            Iterator<String> it = headerMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = headerMap.get(key);
                builder.addHeader(key, value);
            }
        }
        Request request = builder.url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body();
    }

    /**
     * @param url
     * @param mediaType
     * @param data
     * @return
     * @throws IOException
     */
    private ResponseBody postDataRequest(String url, String mediaType, String data) throws IOException {
        Request request = new Request.Builder().addHeader("Connection", "close").url(url).post(RequestBody.create(MediaType.parse(mediaType), data)).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body();
    }

    static class TrustAllCerts implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
