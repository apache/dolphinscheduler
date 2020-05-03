package org.apache.dolphinscheduler.alert.utils;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class DingTalkUtils {
    public static final Logger logger = LoggerFactory.getLogger(DingTalkUtils.class);


    public static final boolean isEnableDingTalk = PropertyUtils.getBoolean(Constants.DINGTALK_ENABLE);
    private static final String dingTaskUrl = PropertyUtils.getString(Constants.DINGTALK_WEBHOOK);
    private static final String keyword = PropertyUtils.getString(Constants.DINGTALK_KEYWORD);
    private static final Boolean isEnableProxy = PropertyUtils.getBoolean(Constants.DINGTALK_PROXY_ENABLE);
    private static final String proxy = PropertyUtils.getString(Constants.DINGTALK_PROXY);
    private static final String user = PropertyUtils.getString(Constants.DINGTALK_USER);
    private static final String passwd = PropertyUtils.getString(Constants.DINGTALK_PASSWORD);
    private static final Integer port = PropertyUtils.getInt(Constants.DINGTALK_PORT);


    private static final int SOCKETTIMEOUT = 5000;
    private static final int CONNECTTIMEOUT = 5000;

    public static String sendDingTalkMsg(String msg, String charset) throws IOException {
        String msgToJson = new DingTalkMsgFormatter(msg + "#" + keyword).toTextMsg();
        HttpPost httpPost = constructHttpPost(msgToJson, charset);

        CloseableHttpClient httpClient;
        if (isEnableProxy) {
            httpClient = getProxyClient();
            RequestConfig rcf = getProxyConfig();
            httpPost.setConfig(rcf);
        } else {
            httpClient = getDefaultClient();
        }

        CloseableHttpResponse response = httpClient.execute(httpPost);
        String resp;
        try {
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, charset);
            EntityUtils.consume(entity);
        } finally {
            response.close();
            httpClient.close();
        }
        logger.info("Ding Talk send [{}], resp:{%s}", msg, resp);
        return resp;
    }

    private static  HttpPost constructHttpPost(String msg, String charset) {
        HttpPost post =  new HttpPost(dingTaskUrl);
        StringEntity entity = new StringEntity(msg, charset);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }


    private static CloseableHttpClient getProxyClient() {
        HttpHost httpProxy = new HttpHost(proxy, port);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(httpProxy), new UsernamePasswordCredentials(user, passwd));
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
        return httpClient;
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    private static RequestConfig getProxyConfig() {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setSocketTimeout(SOCKETTIMEOUT).setConnectTimeout(CONNECTTIMEOUT).setProxy(httpProxy).build();
    }

}
