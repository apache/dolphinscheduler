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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSON;

/**
 * test for MasterExecThread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MasterExecThread.class})
@PowerMockIgnore({"javax.script.*", "javax.management.*"})
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

    /**
     * Method: resetDagTaskNodesByDataLineage(List<TaskNode> taskNodeList,
     * List<String> startNodeNameList)
     */
    @Test
    public void testResetDagTaskNodesByDataLineage()
            throws Exception {
        try {
            Method method = MasterExecThread.class.getDeclaredMethod("resetDagTaskNodesByDataLineage", List.class, List.class);
            method.setAccessible(true);

            String processInstanceJson = "{\"tasks\":[{\"id\":\"tasks-32619\",\"name\":\"t_ds_version_copy1\",\"desc\":null,\"type\":\"SQL\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":[],\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy1#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy2#table_depend\",\"type\":\"MYSQL\",\"datasource\":5,\"sql\":\"INSERT INTO t_ds_version_copy1\\nselect * from t_ds_version_copy2\",\"sqlType\":1,\"udfs\":\"\",\"showType\":\"TABLE\",\"connParams\":\"\",\"preStatements\":[\"truncate table t_ds_version_copy1\"],\"postStatements\":[],\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"targetTable\":null,\"dtType\":null,\"dataTarget\":0,\"resourceFilesList\":[],\"localParametersMap\":{}},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}},{\"id\":\"tasks-12393\",\"name\":\"t_ds_version_copy\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy1#table_depend,127.0.0.1:escheduler:t_ds_version_copy5#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"SELECT * FROM t_ds_version_copy1\\nUNION\\nselect * FROM t_ds_version_copy5\",\"targetTable\":\"t_ds_version_copy\",\"preStatements\":[\"truncate table t_ds_version_copy\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[\"t_ds_version_copy1\"],\"extras\":null,\"depList\":[\"t_ds_version_copy1\"],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}";
            ProcessData processData = JSONUtils.parseObject(processInstanceJson, ProcessData.class);

            ProcessDefinition processDefinition_32619 = new ProcessDefinition();
            processDefinition_32619.setId(2);
            processDefinition_32619.setProcessDefinitionJson("{\"tasks\":[{\"id\":\"tasks-3877\",\"name\":\"4 and 3 -> 2\",\"desc\":null,\"type\":\"SQL\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":[],\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy2#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy4#table_depend,127.0.0.1:escheduler:t_ds_version_copy3#table_depend\",\"type\":\"MYSQL\",\"datasource\":5,\"sql\":\"insert into t_ds_version_copy2\\nselect * from t_ds_version_copy4\\nUNION\\nselect * from t_ds_version_copy3\",\"sqlType\":1,\"udfs\":\"\",\"showType\":\"TABLE\",\"connParams\":\"\",\"preStatements\":[\"truncate table t_ds_version_copy2\"],\"postStatements\":[],\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"targetTable\":null,\"dtType\":null,\"dataTarget\":0,\"resourceFilesList\":[],\"localParametersMap\":{}},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}");
            Mockito.when(processService.queryDependDefinitionList(new String[]{"127.0.0.1:escheduler:t_ds_version_copy2#table_depend"})).thenReturn(Arrays.asList(processDefinition_32619));

            ProcessDefinition processDefinition_12393_1 = new ProcessDefinition();
            processDefinition_12393_1.setId(3);
            processDefinition_12393_1.setProcessDefinitionJson("{\"tasks\":[{\"id\":\"tasks-34351\",\"name\":\"t_ds_version_copy5\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy6#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy6\",\"targetTable\":\"t_ds_version_copy5\",\"preStatements\":[\"truncate table t_ds_version_copy6\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}},{\"id\":\"tasks-43031\",\"name\":\"t_ds_version_copy4\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy4#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy5\",\"targetTable\":\"t_ds_version_copy4\",\"preStatements\":[\"truncate table t_ds_version_copy5\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}");

            ProcessDefinition processDefinition_12393_2 = new ProcessDefinition();
            processDefinition_12393_2.setId(1);
            processDefinition_12393_2.setProcessDefinitionJson("{\"tasks\":[{\"id\":\"tasks-32619\",\"name\":\"t_ds_version_copy1\",\"desc\":null,\"type\":\"SQL\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":[],\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy1#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy2#table_depend\",\"type\":\"MYSQL\",\"datasource\":5,\"sql\":\"INSERT INTO t_ds_version_copy1\\nselect * from t_ds_version_copy2\",\"sqlType\":1,\"udfs\":\"\",\"showType\":\"TABLE\",\"connParams\":\"\",\"preStatements\":[\"truncate table t_ds_version_copy1\"],\"postStatements\":[],\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"targetTable\":null,\"dtType\":null,\"dataTarget\":0,\"resourceFilesList\":[],\"localParametersMap\":{}},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}},{\"id\":\"tasks-12393\",\"name\":\"t_ds_version_copy\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy1#table_depend,127.0.0.1:escheduler:t_ds_version_copy5#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"SELECT * FROM t_ds_version_copy1\\nUNION\\nselect * FROM t_ds_version_copy5\",\"targetTable\":\"t_ds_version_copy\",\"preStatements\":[\"truncate table t_ds_version_copy\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[\"t_ds_version_copy1\"],\"extras\":null,\"depList\":[\"t_ds_version_copy1\"],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}");
            Mockito.when(processService.queryDependDefinitionList(new String[]{"127.0.0.1:escheduler:t_ds_version_copy1#table_depend", "127.0.0.1:escheduler:t_ds_version_copy5#table_depend"})).thenReturn(Arrays.asList(processDefinition_12393_1, processDefinition_12393_2));

            ProcessDefinition processDefinition_3877 = new ProcessDefinition();
            processDefinition_3877.setId(3);
            processDefinition_3877.setProcessDefinitionJson("{\"tasks\":[{\"id\":\"tasks-34351\",\"name\":\"t_ds_version_copy5\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy6#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy6\",\"targetTable\":\"t_ds_version_copy5\",\"preStatements\":[\"truncate table t_ds_version_copy6\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}},{\"id\":\"tasks-43031\",\"name\":\"t_ds_version_copy4\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy4#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy5\",\"targetTable\":\"t_ds_version_copy4\",\"preStatements\":[\"truncate table t_ds_version_copy5\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}");
            Mockito.when(processService.queryDependDefinitionList(new String[]{"127.0.0.1:escheduler:t_ds_version_copy4#table_depend", "127.0.0.1:escheduler:t_ds_version_copy3#table_depend"})).thenReturn(Arrays.asList(processDefinition_3877));

            ProcessDefinition processDefinition_43031 = new ProcessDefinition();
            processDefinition_43031.setId(3);
            processDefinition_43031.setProcessDefinitionJson("{\"tasks\":[{\"id\":\"tasks-34351\",\"name\":\"t_ds_version_copy5\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy6#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy6\",\"targetTable\":\"t_ds_version_copy5\",\"preStatements\":[\"truncate table t_ds_version_copy6\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}},{\"id\":\"tasks-43031\",\"name\":\"t_ds_version_copy4\",\"desc\":null,\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"localParams\":null,\"checkDependFlag\":1,\"targetNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy4#table_target\",\"dependNodeKeys\":\"127.0.0.1:escheduler:t_ds_version_copy5#table_depend\",\"customConfig\":0,\"json\":null,\"dsType\":\"MYSQL\",\"dataSource\":5,\"dtType\":\"MYSQL\",\"dataTarget\":5,\"sql\":\"select id, version from t_ds_version_copy5\",\"targetTable\":\"t_ds_version_copy4\",\"preStatements\":[\"truncate table t_ds_version_copy5\"],\"postStatements\":[],\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"resourceFilesList\":[],\"localParametersMap\":null},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":\"\"},\"forbidden\":false,\"conditionsTask\":false,\"taskTimeoutParameter\":{\"enable\":false,\"strategy\":null,\"interval\":0}}],\"globalParams\":[],\"timeout\":0,\"tenantId\":-1}");
            Mockito.when(processService.queryDependDefinitionList(new String[]{"127.0.0.1:escheduler:t_ds_version_copy5#table_depend"})).thenReturn(Arrays.asList(processDefinition_43031));

            // task 34351
            Mockito.when(processService.queryDependDefinitionList(new String[]{"127.0.0.1:escheduler:t_ds_version_copy6#table_depend"})).thenReturn(new ArrayList<>());

            method.invoke(masterExecThread, processData.getTasks(), new ArrayList<>());

            Assert.assertEquals(5, processData.getTasks().size());
        } catch(Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}