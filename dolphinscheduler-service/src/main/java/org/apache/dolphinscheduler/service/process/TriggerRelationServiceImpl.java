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

package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.common.enums.ApiTriggerType;
import org.apache.dolphinscheduler.dao.entity.TriggerRelation;
import org.apache.dolphinscheduler.dao.mapper.TriggerRelationMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Trigger relation operator to db
 */
@Slf4j
@Component
public class TriggerRelationServiceImpl implements TriggerRelationService {

    @Autowired
    private TriggerRelationMapper triggerRelationMapper;

    @Override
    public void saveTriggerToDb(ApiTriggerType type, Long triggerCode, Integer jobId) {
        TriggerRelation triggerRelation = new TriggerRelation();
        triggerRelation.setTriggerType(type.getCode());
        triggerRelation.setJobId(jobId);
        triggerRelation.setTriggerCode(triggerCode);
        triggerRelation.setCreateTime(new Date());
        triggerRelation.setUpdateTime(new Date());
        triggerRelationMapper.upsert(triggerRelation);
    }

    @Override
    public List<TriggerRelation> queryByTypeAndJobId(ApiTriggerType apiTriggerType, int jobId) {
        return triggerRelationMapper.queryByTypeAndJobId(apiTriggerType.getCode(), jobId);
    }

    @Override
    public int saveCommandTrigger(Integer commandId, Integer processInstanceId) {
        List<TriggerRelation> existTriggers = queryByTypeAndJobId(ApiTriggerType.PROCESS, processInstanceId);
        if (CollectionUtils.isEmpty(existTriggers)) {
            return 0;
        }
        existTriggers.forEach(triggerRelation -> saveTriggerToDb(ApiTriggerType.COMMAND,
                triggerRelation.getTriggerCode(), commandId));
        int triggerRelationSize = existTriggers.size();
        if (triggerRelationSize > 1) {
            // Fix https://github.com/apache/dolphinscheduler/issues/15864
            // This case shouldn't happen
            log.error("The PROCESS TriggerRelation of command: {} is more than one", commandId);
        }
        return existTriggers.size();
    }

    @Override
    public int saveProcessInstanceTrigger(Integer commandId, Integer processInstanceId) {
        List<TriggerRelation> existTriggers = queryByTypeAndJobId(ApiTriggerType.COMMAND, commandId);
        if (CollectionUtils.isEmpty(existTriggers)) {
            return 0;
        }
        existTriggers.forEach(triggerRelation -> saveTriggerToDb(ApiTriggerType.PROCESS,
                triggerRelation.getTriggerCode(), processInstanceId));
        int triggerRelationSize = existTriggers.size();
        if (triggerRelationSize > 1) {
            // Fix https://github.com/apache/dolphinscheduler/issues/15864
            // This case shouldn't happen
            log.error("The COMMAND TriggerRelation of command: {} is more than one", commandId);
        }
        return existTriggers.size();
    }

}
