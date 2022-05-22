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

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Task Group
 */
@TableName("t_ds_task_group")
public class TaskGroup implements Serializable {
    /**
     * key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * task_group name
     */
    private String name;

    private String description;
    /**
     * 作业组大小
     */
    private int groupSize;
    /**
     * 已使用作业组大小
     */
    private int useSize;
    /**
     * creator id
     */
    private int userId;
    /**
     * 0 not available, 1 available
     */
    private Integer status;
    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;
    /**
     * project Id
     */
    private long projectCode;

    public TaskGroup(String name,long projectCode, String description, int groupSize, int userId,int status) {
        this.name = name;
        this.projectCode = projectCode;
        this.description = description;
        this.groupSize = groupSize;
        this.userId = userId;
        this.status = status;
        init();

    }

    public TaskGroup() {
        init();
    }

    public void init() {
        this.status = 1;
        this.useSize = 0;
    }

    @Override
    public String toString() {
        return "TaskGroup{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", groupSize=" + groupSize
                + ", useSize=" + useSize
                + ", userId=" + userId
                + ", status=" + status
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public int getUseSize() {
        return useSize;
    }

    public void setUseSize(int useSize) {
        this.useSize = useSize;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }
}
