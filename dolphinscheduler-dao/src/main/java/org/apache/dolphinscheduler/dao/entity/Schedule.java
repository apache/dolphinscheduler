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

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_schedules")
public class Schedule {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * process definition name
     */
    @TableField(exist = false)
    private String processDefinitionName;

    /**
     * project name
     */
    @TableField(exist = false)
    private String projectName;

    /**
     * schedule description
     */
    @TableField(exist = false)
    private String definitionDescription;

    /**
     * schedule start time
     */
    private Date startTime;

    /**
     * schedule end time
     */
    private Date endTime;

    /**
     * timezoneId
     * <p>see {@link java.util.TimeZone#getTimeZone(String)}
     */
    private String timezoneId;

    /**
     * crontab expression
     */
    private String crontab;

    /**
     * failure strategy
     */
    private FailureStrategy failureStrategy;

    /**
     * warning type
     */
    private WarningType warningType;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * created user id
     */
    private int userId;

    /**
     * created user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * release state
     */
    private ReleaseState releaseState;

    /**
     * warning group id
     */
    private int warningGroupId;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     *  worker group
     */
    private String workerGroup;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment name
     */
    @TableField(exist = false)
    private String environmentName;
}
