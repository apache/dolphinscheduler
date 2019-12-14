package org.apache.dolphinscheduler.alert.utils;


import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;

public class DingTalkUtils {
    public static final Logger logger = LoggerFactory.getLogger(DingTalkUtils.class);

    private static final String webhook = PropertyUtils.getString(Constants.DINGTALK_WEBHOOK);
    private static final String keyword = PropertyUtils.getString(Constants.DINGTALK_KEYWORD);


    public static String sendDingTalkMsg(String charset, String msg) throws IOException {
        String dingTaskUrl = webhook;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(dingTaskUrl);

        String msgToJson = new TextMessage(msg + keyword).toDingTalkTextString();

        httpPost.setEntity(new StringEntity(msgToJson, charset));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String resp;
        try {
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, charset);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        logger.info("Ding Talk send [{}], resp:{}", msg, resp);
        return resp;
    }

    public static boolean isEnableDingTalk() {
       return ! webhook.isEmpty();
    }


}
