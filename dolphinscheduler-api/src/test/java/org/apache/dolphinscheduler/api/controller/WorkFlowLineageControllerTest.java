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

package org.apache.dolphinscheduler.api.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.WorkFlowLineageServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * work flow lineage controller test
 */
public class WorkFlowLineageControllerTest extends AbstractControllerTest {

    @InjectMocks
    private WorkFlowLineageController workFlowLineageController;

    @Mock
    private WorkFlowLineageServiceImpl workFlowLineageService;

    @Test
    public void testQueryWorkFlowLineageByName() {
        long projectCode = 1L;
        String searchVal = "test";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByName(searchVal, projectCode)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByName(user, projectCode, searchVal);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public  void testQueryWorkFlowLineageByIds() {
        long projectCode = 1L;
        String ids = "1";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Set<Integer> idSet = new HashSet<>();
        idSet.add(1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByIds(idSet, projectCode)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByIds(user, projectCode, ids);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

}
