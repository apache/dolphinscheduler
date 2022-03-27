package org.apache.dolphinscheduler.plugin.alert.wechat;

import java.util.Map;

public class WechatAppChatMessage {

    private String chatid;
    private String msgtype;
    private Map<String,String> text;
    private Integer safe;

    public String getChatid() {
        return chatid;
    }

    public void setChatid(String chatid) {
        this.chatid = chatid;
    }

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

    public Integer getSafe() {
        return safe;
    }

    public void setSafe(Integer safe) {
        this.safe = safe;
    }

    public WechatAppChatMessage() {
    }

    public WechatAppChatMessage(String chatid, String msgtype, Map<String, String> text, Integer safe) {
        this.chatid = chatid;
        this.msgtype = msgtype;
        this.text = text;
        this.safe = safe;
    }
}
