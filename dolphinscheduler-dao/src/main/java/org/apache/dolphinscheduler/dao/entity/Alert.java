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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  alert
 */
@TableName("t_ds_alert")
public class Alert {

    /**
     * id
     */
    @TableId(value="id", type=IdType.AUTO)
    private int id;

    /**
     * title
     */
    private String title;

    /**
     * show type
     */
    private ShowType showType;
    /**
     * content
     */
    private String content;

    /**
     * alert type
     */
    private AlertType alertType;



    /**
     * alert status
     */
    private AlertStatus alertStatus;

    /**
     * log
     */
    private String log;

    /**
     * alert group id
     */
    @TableField("alertgroup_id")
    private int alertGroupId;


    /**
     * receivers
     */
    private String receivers;

    /**
     * receivers cc
     */
    private String receiversCc;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;


    @TableField(exist = false)
    private Map<String,Object> info = new HashMap<>();

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    public Alert() {
    }

    public Alert(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ShowType getShowType() {
        return showType;
    }

    public void setShowType(ShowType showType) {
        this.showType = showType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
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

    public int getAlertGroupId() {
        return alertGroupId;
    }

    public void setAlertGroupId(int alertGroupId) {
        this.alertGroupId = alertGroupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Alert alert = (Alert) o;

        if (id != alert.id) {
            return false;
        }
        if (alertGroupId != alert.alertGroupId) {
            return false;
        }
        if (!title.equals(alert.title)) {
            return false;
        }
        if (showType != alert.showType) {
            return false;
        }
        if (!content.equals(alert.content)) {
            return false;
        }
        if (alertType != alert.alertType) {
            return false;
        }
        if (alertStatus != alert.alertStatus) {
            return false;
        }
        if (!log.equals(alert.log)) {
            return false;
        }
        if (!receivers.equals(alert.receivers)) {
            return false;
        }
        if (!receiversCc.equals(alert.receiversCc)) {
            return false;
        }
        if (!createTime.equals(alert.createTime)) {
            return false;
        }
        return updateTime.equals(alert.updateTime) && info.equals(alert.info);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + title.hashCode();
        result = 31 * result + showType.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + alertType.hashCode();
        result = 31 * result + alertStatus.hashCode();
        result = 31 * result + log.hashCode();
        result = 31 * result + alertGroupId;
        result = 31 * result + receivers.hashCode();
        result = 31 * result + receiversCc.hashCode();
        result = 31 * result + createTime.hashCode();
        result = 31 * result + updateTime.hashCode();
        result = 31 * result + info.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", showType=" + showType +
                ", content='" + content + '\'' +
                ", alertType=" + alertType +
                ", alertStatus=" + alertStatus +
                ", log='" + log + '\'' +
                ", alertGroupId=" + alertGroupId +
                ", receivers='" + receivers + '\'' +
                ", receiversCc='" + receiversCc + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", info=" + info +
                '}';
    }
}
