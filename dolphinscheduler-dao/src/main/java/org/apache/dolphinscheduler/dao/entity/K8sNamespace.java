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
 * k8s namespace
 */
@TableName("t_ds_k8s_namespace")
public class K8sNamespace {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * cluster code
     */
    private Long code;

    /**
     * namespace name
     */
    @TableField(value = "namespace")
    private String namespace;

    /**
     * total cpu limit
     */
    @TableField(value = "limits_cpu")
    private Double limitsCpu;

    /**
     * total memory limit,mi
     */
    private Integer limitsMemory;

    /**
     * owner
     */
    @TableField(value = "user_id")
    private int userId;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

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
     * 1.00 = 1 cpu
     */
    @TableField("pod_request_cpu")
    private Double podRequestCpu = 0.0;

    /**
     * Mi
     */
    @TableField("pod_request_memory")
    private Integer podRequestMemory = 0;

    /**
     * replicas
     */
    @TableField("pod_replicas")
    private Integer podReplicas = 0;

    /**
     * cluster code
     */
    @TableField("cluster_code")
    private Long clusterCode;

    /**
     * k8s name
     */
    @TableField(exist = false)
    private String clusterName;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Double getLimitsCpu() {
        return limitsCpu;
    }

    public void setLimitsCpu(Double limitsCpu) {
        this.limitsCpu = limitsCpu;
    }

    public Integer getLimitsMemory() {
        return limitsMemory;
    }

    public void setLimitsMemory(Integer limitsMemory) {
        this.limitsMemory = limitsMemory;
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

    public Integer getPodRequestMemory() {
        return podRequestMemory;
    }

    public void setPodRequestMemory(Integer podRequestMemory) {
        this.podRequestMemory = podRequestMemory;
    }

    public Integer getPodReplicas() {
        return podReplicas;
    }

    public void setPodReplicas(Integer podReplicas) {
        this.podReplicas = podReplicas;
    }

    public Long getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(Long clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Double getPodRequestCpu() {
        return podRequestCpu;
    }

    public void setPodRequestCpu(Double podRequestCpu) {
        this.podRequestCpu = podRequestCpu;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "K8sNamespace{" +
            "id=" + id +
            "code=" + code +
            ", namespace=" + namespace +
            ", limitsCpu=" + limitsCpu +
            ", limitsMemory=" + limitsMemory +
            ", podRequestCpu=" + podRequestCpu +
            ", podRequestMemory=" + podRequestMemory +
            ", podReplicas=" + podReplicas +
            ", clusterCode=" + clusterCode +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        K8sNamespace k8sNamespace = (K8sNamespace) o;

        if (id.equals(k8sNamespace.id)) {
            return true;
        }

        return namespace.equals(k8sNamespace.namespace) && clusterName.equals(k8sNamespace.clusterName);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (clusterName + namespace).hashCode();
        return result;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }
}