/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ds_alert")
public class Alert {
    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * sign
     */
    @TableField(value = "sign")
    private String sign;
    /**
     * title
     */
    @TableField(value = "title")
    private String title;

    /**
     * content
     */
    @TableField(value = "content")
    private String content;

    /**
     * alert_status
     */
    @TableField(value = "alert_status")
    private AlertStatus alertStatus;

    /**
     * warning_type
     */
    @TableField(value = "warning_type")
    private WarningType warningType;

    /**
     * log
     */
    @TableField(value = "log")
    private String log;

    /**
     * alertgroup_id
     */
    @TableField("alertgroup_id")
    private Integer alertGroupId;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;
    /**
     * update_time
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * project_code
     */
    @TableField("project_code")
    private Long projectCode;

    /**
     * process_definition_code
     */
    @TableField("process_definition_code")
    private Long processDefinitionCode;

    /**
     * process_instance_id
     */
    @TableField("process_instance_id")
    private Integer processInstanceId;

    /**
     * alert_type
     */
    @TableField("alert_type")
    private AlertType alertType;

    @TableField(exist = false)
    private Map<String, Object> info = new HashMap<>();

    public Alert() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Integer getAlertGroupId() {
        return alertGroupId;
    }

    public void setAlertGroupId(Integer alertGroupId) {
        this.alertGroupId = alertGroupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public Long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(Long projectCode) {
        this.projectCode = projectCode;
    }

    public Long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(Long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Integer processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Alert alert = (Alert) o;
        return Objects.equals(id, alert.id)
                && Objects.equals(alertGroupId, alert.alertGroupId)
                && Objects.equals(sign, alert.sign)
                && Objects.equals(title, alert.title)
                && Objects.equals(content, alert.content)
                && alertStatus == alert.alertStatus
                && warningType == alert.warningType
                && Objects.equals(log, alert.log)
                && Objects.equals(createTime, alert.createTime)
                && Objects.equals(updateTime, alert.updateTime)
                && Objects.equals(info, alert.info)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sign, title, content, alertStatus, warningType, log, alertGroupId, createTime, updateTime, info);
    }

    @Override
    public String toString() {
        return "Alert{"
                + "id=" + id
                + ", sign='" + sign + '\''
                + ", title='" + title + '\''
                + ", content='" + content + '\''
                + ", alertStatus=" + alertStatus
                + ", warningType=" + warningType
                + ", log='" + log + '\''
                + ", alertGroupId=" + alertGroupId
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", info=" + info
                + '}';
    }
}
