package org.apache.dolphinscheduler.plugin.model;

/**
 * AlertData
 */
public class AlertData {

    /**
     * alert primary key
     */
    private int id;
    /**
     * title
     */
    private String title;
    /**
     * content
     */
    private String content;
    /**
     * log
     */
    private String log;
    /**
     * alertgroup_id
     */
    private int alertGroupId;
    /**
     * receivers
     */
    private String receivers;
    /**
     * show_type
     */
    private String showType;
    /**
     * receivers_cc
     */
    private String receiversCc;

    public AlertData() {
    }

    public int getId() {
        return id;
    }

    public AlertData setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AlertData setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public AlertData setContent(String content) {
        this.content = content;
        return this;
    }

    public String getLog() {
        return log;
    }

    public AlertData setLog(String log) {
        this.log = log;
        return this;
    }

    public int getAlertGroupId() {
        return alertGroupId;
    }

    public AlertData setAlertGroupId(int alertGroupId) {
        this.alertGroupId = alertGroupId;
        return this;
    }

    public String getReceivers() {
        return receivers;
    }

    public AlertData setReceivers(String receivers) {
        this.receivers = receivers;
        return this;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public AlertData setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
        return this;
    }

    public String getShowType() {
        return showType;
    }

    public AlertData setShowType(String showType) {
        this.showType = showType;
        return this;
    }

    public AlertData(int id, String title, String content, String log, int alertGroupId, String receivers, String receiversCc) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.log = log;
        this.alertGroupId = alertGroupId;
        this.receivers = receivers;
        this.receiversCc = receiversCc;
    }
}
