package org.apache.dolphinscheduler.plugin.alert.webhook;

import org.apache.dolphinscheduler.alert.api.ShowType;

import java.util.Map;

public class WebHookMessage {

    private String msgtype;
    private Map<String, String> text;
    private Map<String, String> markdown;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Map<String, String> getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Map<String, String> markdown) {
        this.markdown = markdown;
    }

    public WebHookMessage(String msgtype, Map<String, String> contentMap) {
        this.msgtype = msgtype;
        if (msgtype.equals(ShowType.MARKDOWN.getDescp())) {
            this.markdown = contentMap;
        } else {
            this.text = contentMap;
        }
    }
}
