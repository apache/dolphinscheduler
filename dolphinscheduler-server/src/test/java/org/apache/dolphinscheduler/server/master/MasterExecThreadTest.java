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
package org.apache.dolphinscheduler.server.master;

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.MasterExecThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.*;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * test for MasterExecThread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MasterExecThread.class})
public class MasterExecThreadTest {

    private MasterExecThread masterExecThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private int processDefinitionId = 1;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception{
        processService = mock(ProcessService.class);

        applicationContext = mock(ApplicationContext.class);
        config = new MasterConfig();
        config.setMasterExecTaskNum(1);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);

        processInstance = mock(ProcessInstance.class);
        Mockito.when(processInstance.getProcessDefinitionId()).thenReturn(processDefinitionId);
        Mockito.when(processInstance.getState()).thenReturn(ExecutionStatus.SUCCESS);
        Mockito.when(processInstance.getHistoryCmd()).thenReturn(CommandType.COMPLEMENT_DATA.toString());
        Mockito.when(processInstance.getIsSubProcess()).thenReturn(Flag.NO);
        Mockito.when(processInstance.getScheduleTime()).thenReturn(DateUtils.stringToDate("2020-01-01 00:00:00"));
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, "2020-01-01 00:00:00");
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, "2020-01-31 23:00:00");
        Mockito.when(processInstance.getCommandParam()).thenReturn(JSON.toJSONString(cmdParam));
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGlobalParamMap(Collections.EMPTY_MAP);
        processDefinition.setGlobalParamList(Collections.EMPTY_LIST);
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        masterExecThread = PowerMockito.spy(new MasterExecThread(processInstance, processService,null));
        // prepareProcess init dag
        Field dag = MasterExecThread.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(masterExecThread, new DAG());
        PowerMockito.doNothing().when(masterExecThread, "executeProcess");
        PowerMockito.doNothing().when(masterExecThread, "postHandle");
        PowerMockito.doNothing().when(masterExecThread, "prepareProcess");
        PowerMockito.doNothing().when(masterExecThread, "runProcess");
        PowerMockito.doNothing().when(masterExecThread, "endProcess");
    }

    /**
     * without schedule
     * @throws ParseException
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {
        try{
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 1-30 for next save, and last day 31 no save
            verify(processService, times(31)).saveProcessInstance(processInstance);
        }catch (Exception e){
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * with schedule
     * @throws ParseException
     */
    @Test
    public void testParallelWithSchedule() throws ParseException {
        try{
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(oneSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 15(1 to 31 step 2) for next save, and last day 31 no save
            verify(processService, times(15)).saveProcessInstance(processInstance);
        }catch (Exception e){
            Assert.assertTrue(false);
        }
    }

    private List<Schedule> zeroSchedulerList(){
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList(){
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

}