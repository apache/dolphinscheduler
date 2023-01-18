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
import org.apache.dolphinscheduler.service.cron.CronUtilsTest;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger Relation Service Test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TriggerRelationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

    @InjectMocks
    private TriggerRelationServiceImpl triggerRelationService;
    @Mock
    private TriggerRelationMapper triggerRelationMapper;

    @Test
    public void saveTriggerToDb() {
        Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
        triggerRelationService.saveTriggerToDb(ApiTriggerType.COMMAND, 1234567890L, 100);
    }

    @Test
    public void queryByTypeAndJobId() {
        Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
        Mockito.when(triggerRelationMapper.queryByTypeAndJobId(ApiTriggerType.PROCESS.getCode(), 100))
                .thenReturn(getTriggerTdoDb());

        TriggerRelation triggerRelation1 = triggerRelationService.queryByTypeAndJobId(
                ApiTriggerType.PROCESS, 100);
        Assertions.assertNotNull(triggerRelation1);
        TriggerRelation triggerRelation2 = triggerRelationService.queryByTypeAndJobId(
                ApiTriggerType.PROCESS, 200);
        Assertions.assertNull(triggerRelation2);
    }

    @Test
    public void saveCommandTrigger() {
        Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
        Mockito.when(triggerRelationMapper.queryByTypeAndJobId(ApiTriggerType.PROCESS.getCode(), 100))
                .thenReturn(getTriggerTdoDb());
        int result = -1;
        result = triggerRelationService.saveCommandTrigger(1234567890, 100);
        Assertions.assertTrue(result > 0);
        result = triggerRelationService.saveCommandTrigger(1234567890, 200);
        Assertions.assertTrue(result == 0);

    }

    @Test
    public void saveProcessInstanceTrigger() {
        Mockito.doNothing().when(triggerRelationMapper).upsert(Mockito.any());
        Mockito.when(triggerRelationMapper.queryByTypeAndJobId(ApiTriggerType.COMMAND.getCode(), 100))
                .thenReturn(getTriggerTdoDb());
        int result = -1;
        result = triggerRelationService.saveProcessInstanceTrigger(100, 1234567890);
        Assertions.assertTrue(result > 0);
        result = triggerRelationService.saveProcessInstanceTrigger(200, 1234567890);
        Assertions.assertTrue(result == 0);
    }

    private TriggerRelation getTriggerTdoDb() {
        TriggerRelation triggerRelation = new TriggerRelation();
        triggerRelation.setTriggerType(ApiTriggerType.PROCESS.getCode());
        triggerRelation.setJobId(100);
        triggerRelation.setTriggerCode(1234567890L);
        triggerRelation.setCreateTime(new Date());
        triggerRelation.setUpdateTime(new Date());
        return triggerRelation;
    }
}
