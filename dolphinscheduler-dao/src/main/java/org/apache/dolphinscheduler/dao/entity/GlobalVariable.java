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

import java.util.Date;
import java.util.List;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

/**
 * calendar
 */
@TableName("t_yss_global_variable")
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class GlobalVariable {

    /**
     * id
     */
    @TableId(value="id", type=IdType.AUTO)
    private int id;

    /**
     * globalVariable name
     */
    private String name;

    /**
     * globalVariable key
     */
    private String keyData;

    /**
     * globalVariable value
     */
    private String valueData;

    /**
     * project id
     */
    private int projectId;

    /**
     * process is valid: yes/no
     */
    private Flag flag;

    /**
     * process user id
     */
    private int userId;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }

    public String getValueData() {
        return valueData;
    }

    public void setValueData(String valueData) {
        this.valueData = valueData;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
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
}
