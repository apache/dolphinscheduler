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
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * task definition service impl
 */
@Service
public class ProcessTaskRelationServiceImpl extends BaseService implements
        ProcessTaskRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskRelationServiceImpl.class);

    //@Autowired
    //private ProjectMapper projectMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    /**
     * create process task relation
     *
     * @param loginUser login user
     * @param name relation name
     * @param projectCode process code
     * @param processDefinitionCode process definition code
     * @param preTaskCode pre task code
     * @param postTaskCode post task code
     * @param conditionType condition type
     * @param conditionParams condition params
     * @return create result code
     */
    @Transactional
    @Override
    public Map<String, Object> createProcessTaskRelation(User loginUser,
                                                         String name,
                                                         Long projectCode,
                                                         Long processDefinitionCode,
                                                         Long preTaskCode,
                                                         Long postTaskCode,
                                                         String conditionType,
                                                         String conditionParams) {
        Map<String, Object> result = new HashMap<>();
        // TODO check projectCode
        // TODO check processDefinitionCode
        // TODO check preTaskCode and postTaskCode
        Date now = new Date();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation(name,
                1,
                projectCode,
                processDefinitionCode,
                preTaskCode,
                postTaskCode,
                ConditionType.of(conditionType),
                conditionParams,
                now,
                now);
        // save process task relation
        processTaskRelationMapper.insert(processTaskRelation);
        // save process task relation log
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.set(processTaskRelation);
        processTaskRelationLog.setOperator(loginUser.getId());
        processTaskRelationLog.setOperateTime(now);
        processTaskRelationLogMapper.insert(processTaskRelationLog);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}

