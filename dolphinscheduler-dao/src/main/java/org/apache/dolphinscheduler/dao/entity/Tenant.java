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
import java.util.Objects;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_tenant")
public class Tenant {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * description
     */
    private String description;

    /**
     * queue id
     */
    private int queueId;

    /**
     * queue name
     */
    @TableField(exist = false)
    private String queueName;

    /**
     * queue
     */
    @TableField(exist = false)
    private String queue;

    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;

    public Tenant() {
    }

    public Tenant(String tenantCode, String description, int queueId) {
        Date now = new Date();
        this.tenantCode = tenantCode;
        this.description = description;
        this.queueId = queueId;
        this.createTime = now;
        this.updateTime = now;
    }

    public Tenant(int id, String tenantCode, String description, int queueId) {
        Date now = new Date();
        this.id = id;
        this.tenantCode = tenantCode;
        this.description = description;
        this.queueId = queueId;
        this.createTime = now;
        this.updateTime = now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tenant tenant = (Tenant) o;

        return id == tenant.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
