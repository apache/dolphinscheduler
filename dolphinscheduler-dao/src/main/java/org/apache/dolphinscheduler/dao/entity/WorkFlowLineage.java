package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

public class WorkFlowLineage {
    private int workFlowId;
    private String workFlowName;
    private String workFlowPublishStatus;
    private Date scheduleStartTime;
    private Date scheduleEndTime;
    private String crontab;
    private int schedulePublishStatus;
    private String sourceWorkFlowId;

    public String getSourceWorkFlowId() {
        return sourceWorkFlowId;
    }

    public void setSourceWorkFlowId(String sourceWorkFlowId) {
        this.sourceWorkFlowId = sourceWorkFlowId;
    }

    public int getWorkFlowId() {
        return workFlowId;
    }

    public void setWorkFlowId(int workFlowId) {
        this.workFlowId = workFlowId;
    }

    public String getWorkFlowName() {
        return workFlowName;
    }

    public void setWorkFlowName(String workFlowName) {
        this.workFlowName = workFlowName;
    }

    public String getWorkFlowPublishStatus() {
        return workFlowPublishStatus;
    }

    public void setWorkFlowPublishStatus(String workFlowPublishStatus) {
        this.workFlowPublishStatus = workFlowPublishStatus;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(Date scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

    public String getCrontab() {
        return crontab;
    }

    public void setCrontab(String crontab) {
        this.crontab = crontab;
    }

    public int getSchedulePublishStatus() {
        return schedulePublishStatus;
    }

    public void setSchedulePublishStatus(int schedulePublishStatus) {
        this.schedulePublishStatus = schedulePublishStatus;
    }
}
