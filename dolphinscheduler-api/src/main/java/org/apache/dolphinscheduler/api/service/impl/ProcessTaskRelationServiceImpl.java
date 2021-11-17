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

import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * process task relation service impl
 */
@Service
public class ProcessTaskRelationServiceImpl extends BaseServiceImpl implements ProcessTaskRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskRelationServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    /**
     * create process task relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return create result code
     */
    @Override
    public Map<String, Object> createProcessTaskRelation(User loginUser, long projectCode, long processDefinitionCode, long preTaskCode, long postTaskCode) {
        return null;
    }

    /**
     * move task to other processDefinition
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param targetProcessDefinitionCode target process definition code
     * @param taskCode the current task code (the post task code)
     * @return move result code
     */
    @Override
    public Map<String, Object> moveTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode, long targetProcessDefinitionCode, long taskCode) {
        return null;
    }

    /**
     * delete process task relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param taskCode the post task code
     * @return delete result code
     */
    @Override
    public Map<String, Object> deleteTaskProcessRelation(User loginUser, long projectCode, long processDefinitionCode, long taskCode) {
        return null;
    }

    /**
     * delete task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param preTaskCodes the pre task codes, sep ','
     * @param taskCode the post task code
     * @return delete result code
     */
    @Override
    public Map<String, Object> deleteUpstreamRelation(User loginUser, long projectCode, String preTaskCodes, long taskCode) {
        return null;
    }

    /**
     * delete task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param postTaskCodes the post task codes, sep ','
     * @param taskCode the pre task code
     * @return delete result code
     */
    @Override
    public Map<String, Object> deleteDownstreamRelation(User loginUser, long projectCode, String postTaskCodes, long taskCode) {
        return null;
    }

    /**
     * query task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode current task code (post task code)
     * @return process task relation list
     */
    @Override
    public Map<String, Object> queryUpstreamRelation(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check task exist in project
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null || taskDefinition.getProjectCode() != projectCode) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode);
        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            // check taskCode equal to postTaskCode
            List<ProcessTaskRelation> upstreamRelationList = processTaskRelationList.stream().filter(processTask -> processTask.getPostTaskCode() == taskCode).collect(Collectors.toList());
            result.put(Constants.DATA_LIST, upstreamRelationList);
        } else {
            result.put(Constants.DATA_LIST, new ArrayList<>());
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode pre task code
     * @return process task relation list
     */
    @Override
    public Map<String, Object> queryDownstreamRelation(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check task exist in project
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null || taskDefinition.getProjectCode() != projectCode) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode);
        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            // check taskCode equal to preTaskCode
            List<ProcessTaskRelation> downstreamRelationList = processTaskRelationList.stream().filter(processTask -> processTask.getPreTaskCode() == taskCode).collect(Collectors.toList());
            result.put(Constants.DATA_LIST, downstreamRelationList);
        } else {
            result.put(Constants.DATA_LIST, new ArrayList<>());
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
