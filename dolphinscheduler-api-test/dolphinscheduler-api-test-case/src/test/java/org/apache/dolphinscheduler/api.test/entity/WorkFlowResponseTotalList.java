/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkFlowResponseTotalList {

    private String code;

    private Date createTime;

    private String description;

    private String executionType;

    private String flag;

    private Arrays globalParamList;

    private HashMap globalParamMap;

    private String globalParams;

    private Integer id;

    private String locations;

    private String modifyBy;

    private String name;

    private String projectCode;

    private String projectName;

    private String releaseState;

    private String scheduleReleaseState;

    private String tenantCode;

    private String tenantId;

    private Integer timeout;

    private Date updateTime;

    private Integer userId;

    private String userName;

    private Integer version;

    private Integer warningGroupId;
}
