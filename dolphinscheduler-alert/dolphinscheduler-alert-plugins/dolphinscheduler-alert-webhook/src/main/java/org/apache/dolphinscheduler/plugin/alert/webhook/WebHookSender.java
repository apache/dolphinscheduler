package org.apache.dolphinscheduler.plugin.alert.webhook;

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebHookSender {

    private static final Logger logger = LoggerFactory.getLogger(WebHookSender.class);

    private static final String ALERT_STATUS = "false";

    private final String showType;

    private final String webHookURL;

    public WebHookSender(Map<String, String> paramsMap) {
        webHookURL = paramsMap.get(WebHookAlertConstants.WEBHOOK_URL);
        showType = paramsMap.get(AlertConstants.NAME_SHOW_TYPE);
    }

    /**
     * send WebHook
     *
     * @return WebHook resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     */
    public AlertResult send(String title, String content) {
        AlertResult alertResult;
        String data = markdownByAlert(title, content);
        if (null == webHookURL) {
            alertResult = new AlertResult();
            alertResult.setMessage("send webhook alert fail, get webhook is null ");
            alertResult.setStatus(ALERT_STATUS);
            return alertResult;
        }

        Map<String, String> contentMap = new HashMap<>();
        contentMap.put(WebHookAlertConstants.CONTENT_KEY, data);
        WebHookMessage webHookMessage = new WebHookMessage(showType, contentMap);
        String msgJson = JSONUtils.toJsonString(webHookMessage);
        try {
            return checkSendResult(post(webHookURL, msgJson));
        } catch (Exception e) {
            logger.info("send webhook alert msg exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setMessage("send webhook alert fail");
            alertResult.setStatus(ALERT_STATUS);
        }
        return alertResult;
    }

    private static String post(String url, String data) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, WebHookAlertConstants.CHARSET));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WebHookAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("WebHook send [{}], param:{}, resp:{}",
                    url, data, resp);
            return resp;
        }
    }

    /**
     * convert text to markdown style
     *
     * @param title   the title
     * @param content the content
     * @return markdown text
     */
    private static String markdownText(String title, String content) {
        if (StringUtils.isNotEmpty(content)) {
            StringBuilder contents = new StringBuilder(100);
            contents.append(title).append(": ").append(content);
            return contents.toString();
        }
        return title;
    }

    private static AlertResult checkSendResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(ALERT_STATUS);

        if (null == result) {
            alertResult.setMessage("webhook send fail");
            logger.info("send webhook msg error, resp is null");
            return alertResult;
        }
        WeChatSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, WeChatSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("webhook send fail");
            logger.info("send webhook msg error, resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("webhook alert send success");
            return alertResult;
        }
        alertResult.setStatus(ALERT_STATUS);
        alertResult.setMessage(sendMsgResponse.getErrmsg());
        return alertResult;
    }

    /**
     * Determine the mardown style based on the show type of the alert
     *
     * @return the markdown alert table/text
     */
    private String markdownByAlert(String title, String content) {
        return markdownText(title, content);
    }

    static final class WeChatSendMsgResponse {

        private Integer errcode;
        private String errmsg;

        public WeChatSendMsgResponse() {
        }

        public Integer getErrcode() {
            return this.errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return this.errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof WeChatSendMsgResponse)) {
                return false;
            }
            final WeChatSendMsgResponse other = (WeChatSendMsgResponse) o;
            final Object this$errcode = this.getErrcode();
            final Object other$errcode = other.getErrcode();
            if (!Objects.equals(this$errcode, other$errcode)) {
                return false;
            }
            final Object this$errmsg = this.getErrmsg();
            final Object other$errmsg = other.getErrmsg();
            if (!Objects.equals(this$errmsg, other$errmsg)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $errcode = this.getErrcode();
            result = result * PRIME + ($errcode == null ? 43 : $errcode.hashCode());
            final Object $errmsg = this.getErrmsg();
            result = result * PRIME + ($errmsg == null ? 43 : $errmsg.hashCode());
            return result;
        }

        public String toString() {
            return "WebHookSender.send(errcode=" + this.getErrcode() + ", errmsg=" + this.getErrmsg()
                    + ")";
        }
    }
}
