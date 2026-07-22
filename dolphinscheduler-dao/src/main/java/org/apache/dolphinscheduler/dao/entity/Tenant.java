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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_tenant")
public class Tenant {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String tenantCode;

    private String description;

    private int queueId;

    @TableField(exist = false)
    private String queueName;

    @TableField(exist = false)
    private String queue;

    private Date createTime;
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

}
