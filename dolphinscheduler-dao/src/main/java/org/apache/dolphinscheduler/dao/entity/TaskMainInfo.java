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

import org.apache.dolphinscheduler.common.enums.ReleaseState;

import java.util.Date;
import java.util.Map;

import lombok.Data;

/**
 * task main info
 */
@Data
public class TaskMainInfo {

    private long id;

    /**
     * task name
     */
    private String taskName;

    /**
     * task code
     */
    private long taskCode;

    /**
     * task version
     */
    private int taskVersion;

    /**
     * task type
     */
    private String taskType;

    /**
     * create time
     */
    private Date taskCreateTime;

    /**
     * update time
     */
    private Date taskUpdateTime;

    /**
     * processDefinitionCode
     */
    private long processDefinitionCode;

    /**
     * processDefinitionVersion
     */
    private int processDefinitionVersion;

    /**
     * processDefinitionName
     */
    private String processDefinitionName;

    /**
     * processReleaseState
     */
    private ReleaseState processReleaseState;

    /**
     * upstreamTaskMap(k:code,v:name)
     */
    private Map<Long, String> upstreamTaskMap;

    /**
     * upstreamTaskCode
     */
    private long upstreamTaskCode;

    /**
     * upstreamTaskName
     */
    private String upstreamTaskName;
}
