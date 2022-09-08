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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_dq_task_statistics_value")
public class DqTaskStatisticsValue implements Serializable {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * process defined id
     */
    @TableField(value = "process_definition_id")
    private long processDefinitionId;
    /**
     * process definition name
     */
    @TableField(exist = false)
    private String processDefinitionName;
    /**
     * task instance id
     */
    @TableField(value = "task_instance_id")
    private long taskInstanceId;
    /**
     * task name
     */
    @TableField(exist = false)
    private String taskName;
    /**
     * rule id
     */
    @TableField(value = "rule_id")
    private long ruleId;
    /**
     * rule type
     */
    @TableField(exist = false)
    private int ruleType;
    /**
     * rule name
     */
    @TableField(exist = false)
    private String ruleName;
    /**
     * statistics value
     */
    @TableField(value = "statistics_value")
    private double statisticsValue;
    /**
     * comparison value
     */
    @TableField(value = "statistics_name")
    private String statisticsName;
    /**
     * data time
     */
    @TableField(value = "data_time")
    private Date dataTime;
    /**
     * create time
     */
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * update time
     */
    @TableField(value = "update_time")
    private Date updateTime;
}
