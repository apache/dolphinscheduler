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

import java.util.Date;

/**
 * user alert group
 */
@TableName("t_ds_relation_user_alertgroup")
public class UserAlertGroup {

    /**
     * id
     */
    @TableId(value="id", type=IdType.AUTO)
    private int id;

    /**
     * id
     */
    @TableField("alertgroup_id")
    private int alertgroupId;

    /**
     * alert group name
     */
    @TableField(exist = false)
    private String alertgroupName;

    /**
     * user id
     */
    private int userId;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertgroupId() {
        return alertgroupId;
    }

    public void setAlertgroupId(int alertgroupId) {
        this.alertgroupId = alertgroupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getAlertgroupName() {
        return alertgroupName;
    }

    public void setAlertgroupName(String alertgroupName) {
        this.alertgroupName = alertgroupName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "UserAlertGroup{" +
                "id=" + id +
                ", alertgroupId=" + alertgroupId +
                ", alertgroupName='" + alertgroupName + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
