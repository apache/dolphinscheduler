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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * task definition service impl
 */
@Service
public class ProcessTaskRelationServiceImpl extends BaseServiceImpl implements
        ProcessTaskRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskRelationServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    /**
     * query process task relation
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionCode process definition code
     */
    @Override
    public Map<String, Object> queryProcessTaskRelation(User loginUser, String projectName, Long processDefinitionCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByProcessCode(project.getCode(), processDefinitionCode);
        result.put(Constants.DATA_LIST, processTaskRelationList);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
