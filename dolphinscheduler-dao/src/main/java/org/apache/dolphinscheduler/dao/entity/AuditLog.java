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

import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_audit_log")
public class AuditLog {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * user id
     */
    private Integer userId;

    /**
     * model type
     */
    private String modelType;

    /**
     * operation type
     */
    private String operationType;

    /**
     * model id
     */
    private Long modelId;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * operation time
     */
    private Date createTime;

    private String detail;

    private String description;

    private String modelName;

    private long latency;

    public static AuditLog copyNewOne(AuditLog auditLog) {
        AuditLog auditLogNew = new AuditLog();
        BeanUtils.copyProperties(auditLog, auditLogNew);
        return auditLogNew;
    }
}
