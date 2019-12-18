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
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

/**
 * schedule
 */
@Data
@NoArgsConstructor
@ToString
@TableName("t_ds_schedules")
public class Schedule {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private int processDefinitionId;
    @TableField(exist = false)
    private String processDefinitionName;
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String definitionDescription;
    private Date startTime;
    private Date endTime;
    private String crontab;
    private FailureStrategy failureStrategy;
    private WarningType warningType;
    private Date createTime;
    private Date updateTime;
    private int userId;
    @TableField(exist = false)
    private String userName;
    private ReleaseState releaseState;
    private int warningGroupId;
    private Priority processInstancePriority;
    private int workerGroupId;
}
