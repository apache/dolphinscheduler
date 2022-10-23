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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.EnvironmentWorkerGroupRelationServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;

import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * environment service test
 */
@ExtendWith(MockitoExtension.class)
public class EnvironmentWorkerGroupRelationServiceTest {

    public static final Logger logger = LoggerFactory.getLogger(EnvironmentWorkerGroupRelationServiceTest.class);

    @InjectMocks
    private EnvironmentWorkerGroupRelationServiceImpl relationService;

    @Mock
    private EnvironmentWorkerGroupRelationMapper relationMapper;

    @Test
    public void testQueryEnvironmentWorkerGroupRelation() {
        Mockito.when(relationMapper.queryByEnvironmentCode(1L))
                .thenReturn(Lists.newArrayList(new EnvironmentWorkerGroupRelation()));
        Map<String, Object> result = relationService.queryEnvironmentWorkerGroupRelation(1L);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryAllEnvironmentWorkerGroupRelationList() {
        Mockito.when(relationMapper.selectList(Mockito.any()))
                .thenReturn(Lists.newArrayList(new EnvironmentWorkerGroupRelation()));
        Map<String, Object> result = relationService.queryAllEnvironmentWorkerGroupRelationList();
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

}
