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
package cn.escheduler.api.service;


import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.mapper.ProjectMapper;
import cn.escheduler.dao.mapper.TaskInstanceMapper;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.Project;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * task instance service
 */
@Service
public class TaskInstanceService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceService.class);

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessDao processDao;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;


    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @param taskName
     * @param startDate
     * @param endDate
     * @param searchVal
     * @param stateType
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String,Object> queryTaskListPaging(User loginUser, String projectName,
                                                  Integer processInstanceId, String taskName, String startDate, String endDate,
                                                  String searchVal, ExecutionStatus stateType,String host,
                                                  Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }

        int[] statusArray = null;
        String statesStr = null;
        // filter by status
        if(stateType != null){
            statusArray = new int[]{stateType.ordinal()};
        }
        if(statusArray != null){
            statesStr = Arrays.toString(statusArray).replace("[", "").replace("]","");
        }

        Date start = null;
        Date end = null;
        try {
            if(StringUtils.isNotEmpty(startDate)){
                start = DateUtils.getScheduleDate(startDate);
            }
            if(StringUtils.isNotEmpty( endDate)){
                end = DateUtils.getScheduleDate(endDate);
            }
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "startDate,endDate"));
            return result;
        }
        Integer count = taskInstanceMapper.countTaskInstance(project.getId(), processInstanceId, taskName, statesStr,
                host,start, end, searchVal);

        PageInfo pageInfo = new PageInfo<ProcessInstance>(pageNo, pageSize);
        Set<String> exclusionSet = new HashSet<String>(){{
            add(Constants.CLASS);
            add("taskJson");
        }};
        List<TaskInstance> taskInstanceList = taskInstanceMapper.queryTaskInstanceListPaging(
                project.getId(), processInstanceId, searchVal, taskName, statesStr, host, start, end, pageInfo.getStart(), pageSize);
        pageInfo.setTotalCount(count);
        pageInfo.setLists(CollectionUtils.getListByExclusion(taskInstanceList,exclusionSet));
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }
}
