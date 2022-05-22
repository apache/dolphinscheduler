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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * multi-data centre k8s temporary structure, waiting for new feature to complete will switch
 */
@TableName("t_ds_k8s")
public class K8s {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * k8s name
     */
    @TableField(value = "k8s_name")
    private String k8sName;
    /**
     * k8s client config(yaml or json)
     */
    @TableField(value = "k8s_config")
    private String k8sConfig;

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

    public K8s() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getK8sName() {
        return k8sName;
    }

    public void setK8sName(String k8sName) {
        this.k8sName = k8sName;
    }

    public String getK8sConfig() {
        return k8sConfig;
    }

    public void setK8sConfig(String k8sConfig) {
        this.k8sConfig = k8sConfig;
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
