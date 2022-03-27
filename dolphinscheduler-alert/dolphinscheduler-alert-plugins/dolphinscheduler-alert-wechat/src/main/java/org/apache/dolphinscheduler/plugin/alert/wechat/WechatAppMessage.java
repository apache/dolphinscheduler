package org.apache.dolphinscheduler.plugin.alert.wechat;

import java.util.Map;

public class WechatAppMessage {

    private String touser;
    private String msgtype;
    private Integer agentid;
    private Map<String,String> text;
    private Integer safe;
    private Integer enable_id_trans;
    private Integer enable_duplicate_check;

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public Integer getAgentid() {
        return agentid;
    }

    public void setAgentid(Integer agentid) {
        this.agentid = agentid;
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

    public Integer getEnable_id_trans() {
        return enable_id_trans;
    }

    public void setEnable_id_trans(Integer enable_id_trans) {
        this.enable_id_trans = enable_id_trans;
    }

    public Integer getEnable_duplicate_check() {
        return enable_duplicate_check;
    }

    public void setEnable_duplicate_check(Integer enable_duplicate_check) {
        this.enable_duplicate_check = enable_duplicate_check;
    }

    public WechatAppMessage() {
    }

    public WechatAppMessage(String touser, String msgtype, Integer agentid, Map<String, String> text, Integer safe, Integer enable_id_trans, Integer enable_duplicate_check) {
        this.touser = touser;
        this.msgtype = msgtype;
        this.agentid = agentid;
        this.text = text;
        this.safe = safe;
        this.enable_id_trans = enable_id_trans;
        this.enable_duplicate_check = enable_duplicate_check;
    }
}
