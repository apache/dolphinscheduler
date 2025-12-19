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

package org.apache.dolphinscheduler.service.expand;

import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubWorkflowParameters;

import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

@ExtendWith(MockitoExtension.class)
public class CuringParamsServiceImplTest {

    private static final String YESTERDAY_DATE_PLACEHOLDER = "$[yyyy-MM-dd-1]";

    @Mock
    private CuringParamsService curingParamsService;

    @InjectMocks
    private CuringParamsServiceImpl curingParamsServiceImpl;

    @Mock
    private ProjectParameterMapper projectParameterMapper;

    private final Map<String, Property> paramMap = new HashMap<>();

    @BeforeEach
    public void init() {
        paramMap.put("globalParams1", new Property("globalParams1", Direct.IN, DataType.VARCHAR, "Params1"));
    }

    @Test
    public void testConvertParameterPlaceholders() {
        Mockito.when(curingParamsService.convertParameterPlaceholders(YESTERDAY_DATE_PLACEHOLDER, paramMap))
                .thenReturn("2022-06-26");
        String result = curingParamsService.convertParameterPlaceholders(YESTERDAY_DATE_PLACEHOLDER, paramMap);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testCuringGlobalParams() {
        // define globalMap
        Map<String, String> globalParamMap = new HashMap<>();
        globalParamMap.put("globalParams1", "Params1");

        // define globalParamList
        List<Property> globalParamList = new ArrayList<>();

        // define scheduleTime
        Date scheduleTime = DateUtils.stringToDate("2019-12-20 00:00:00");

        // test globalParamList is null
        String result = curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertNull(result);
        Assertions.assertNull(curingParamsServiceImpl.curingGlobalParams(1, null, null,
                CommandType.START_CURRENT_TASK_PROCESS, null, null));
        Assertions.assertNull(curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, null,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null));

        // test globalParamList is not null
        Property property = new Property("testGlobalParam", Direct.IN, DataType.VARCHAR, "testGlobalParam");
        globalParamList.add(property);

        String result2 = curingParamsServiceImpl.curingGlobalParams(1, null, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result2, JSONUtils.toJsonString(globalParamList));

        String result3 = curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, null, null);
        Assertions.assertEquals(result3, JSONUtils.toJsonString(globalParamList));

        String result4 = curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result4, JSONUtils.toJsonString(globalParamList));

        // test var $ startsWith
        globalParamMap.put("bizDate", "${system.biz.date}");
        globalParamMap.put("b1zCurdate", "${system.biz.curdate}");

        Property property2 = new Property("testParamList1", Direct.IN, DataType.VARCHAR, "testParamList");
        Property property3 = new Property("testParamList2", Direct.IN, DataType.VARCHAR, "{testParamList1}");
        Property property4 = new Property("testParamList3", Direct.IN, DataType.VARCHAR, "${b1zCurdate}");

        globalParamList.add(property2);
        globalParamList.add(property3);
        globalParamList.add(property4);

        String result5 = curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result5, JSONUtils.toJsonString(globalParamList));

        Property testStartParamProperty = new Property("testStartParam", Direct.IN, DataType.VARCHAR, "");
        globalParamList.add(testStartParamProperty);
        Property testStartParam2Property =
                new Property("testStartParam2", Direct.IN, DataType.VARCHAR, "$[yyyy-MM-dd+1]");
        globalParamList.add(testStartParam2Property);
        globalParamMap.put("testStartParam", "");
        globalParamMap.put("testStartParam2", "$[yyyy-MM-dd+1]");

        Map<String, String> startParamMap = new HashMap<>(2);
        startParamMap.put("testStartParam", "$[yyyyMMdd]");

        for (Map.Entry<String, String> param : globalParamMap.entrySet()) {
            String val = startParamMap.get(param.getKey());
            if (val != null) {
                param.setValue(val);
            }
        }

        String result6 = curingParamsServiceImpl.curingGlobalParams(1, globalParamMap, globalParamList,
                CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result6, JSONUtils.toJsonString(globalParamList));
    }

    @Test
    public void testParamParsingPreparation() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setExecutePath("home/path/execute");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("TaskName-1");
        taskDefinition.setCode(1000001L);

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(2);
        final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                .timeZone("Asia/Shanghai")
                .build();
        workflowInstance.setCommandParam(JSONUtils.toJsonString(backfillWorkflowCommandParam));
        workflowInstance.setHistoryCmd(CommandType.COMPLEMENT_DATA.toString());
        Property property = new Property();
        property.setDirect(Direct.IN);
        property.setProp("global_params");
        property.setValue("hello world");
        property.setType(DataType.VARCHAR);
        List<Property> properties = Lists.newArrayList(property);
        workflowInstance.setGlobalParams(JSONUtils.toJsonString(properties));

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setName("ProcessName-1");
        workflowDefinition.setProjectName("ProjectName");
        workflowDefinition.setProjectCode(3000001L);
        workflowDefinition.setCode(200001L);

        Project project = new Project();
        project.setName("ProjectName");
        project.setCode(3000001L);

        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        taskInstance.setTaskCode(taskDefinition.getCode());
        taskInstance.setTaskDefinitionVersion(taskDefinition.getVersion());
        taskInstance.setProjectCode(workflowDefinition.getProjectCode());
        taskInstance.setWorkflowInstanceId(workflowInstance.getId());

        AbstractParameters parameters = new SubWorkflowParameters();

        Mockito.when(projectParameterMapper.queryByProjectCode(Mockito.anyLong())).thenReturn(Collections.emptyList());

        Map<String, Property> propertyMap =
                curingParamsServiceImpl.paramParsingPreparation(taskInstance, parameters, workflowInstance,
                        project.getName(), workflowDefinition.getName());
        Assertions.assertNotNull(propertyMap);
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_TASK_INSTANCE_ID).getValue(),
                String.valueOf(taskInstance.getId()));
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_TASK_EXECUTE_PATH).getValue(),
                taskInstance.getExecutePath());
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_WORKFLOW_INSTANCE_ID).getValue(),
                String.valueOf(workflowInstance.getId()));
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_PROJECT_NAME).getValue(),
                workflowDefinition.getProjectName());
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_PROJECT_CODE).getValue(),
                String.valueOf(workflowDefinition.getProjectCode()));
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_TASK_DEFINITION_CODE).getValue(),
                String.valueOf(taskDefinition.getCode()));
        Assertions.assertEquals(propertyMap.get(TaskConstants.PARAMETER_WORKFLOW_DEFINITION_CODE).getValue(),
                String.valueOf(workflowDefinition.getCode()));
    }

    @Test
    public void testParseWorkflowStartParam() {
        Map<String, Property> result;
        // empty cmd param
        Map<String, String> startParamMap = new HashMap<>();
        result = curingParamsServiceImpl.parseWorkflowStartParam(startParamMap);
        Assertions.assertTrue(MapUtils.isEmpty(result));

        // without key
        startParamMap.put("testStartParam", "$[yyyyMMdd]");
        result = curingParamsServiceImpl.parseWorkflowStartParam(startParamMap);
        Assertions.assertTrue(MapUtils.isEmpty(result));

        startParamMap.put("StartParams", "{\"param1\":\"11111\", \"param2\":\"22222\"}");
        result = curingParamsServiceImpl.parseWorkflowStartParam(startParamMap);
        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(2, result.keySet().size());
        Assertions.assertEquals("11111", result.get("param1").getValue());
        Assertions.assertEquals("22222", result.get("param2").getValue());
    }

    @Test
    public void testParseWorkflowFatherParam() {
        Map<String, Property> result;
        // empty cmd param
        Map<String, String> startParamMap = new HashMap<>();
        result = curingParamsServiceImpl.parseWorkflowFatherParam(startParamMap);
        Assertions.assertTrue(MapUtils.isEmpty(result));

        // without key
        startParamMap.put("testfatherParams", "$[yyyyMMdd]");
        result = curingParamsServiceImpl.parseWorkflowFatherParam(startParamMap);
        Assertions.assertTrue(MapUtils.isEmpty(result));

        startParamMap.put("fatherParams", "{\"param1\":\"11111\", \"param2\":\"22222\"}");
        result = curingParamsServiceImpl.parseWorkflowFatherParam(startParamMap);
        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(2, result.keySet().size());
        Assertions.assertEquals("11111", result.get("param1").getValue());
        Assertions.assertEquals("22222", result.get("param2").getValue());
    }

    @Test
    public void testParseGlobalParamsMap() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setGlobalParams(
                "[{\"prop\":\"param1\",\"value\":\"11111\"},{\"prop\":\"param2\",\"value\":\"22222\"}]");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseGlobalParamsMap", WorkflowInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Property> result = (Map<String, Property>) method.invoke(curingParamsServiceImpl, workflowInstance);

        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(2, result.keySet().size());
        Assertions.assertEquals("11111", result.get("param1").getValue());
        Assertions.assertEquals("22222", result.get("param2").getValue());
    }

    @Test
    public void testParseGlobalParamsMap_whenGlobalParamsIsNull() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setGlobalParams(null);

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseGlobalParamsMap", WorkflowInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Property> result = (Map<String, Property>) method.invoke(curingParamsServiceImpl, workflowInstance);

        Assertions.assertTrue(MapUtils.isEmpty(result));
    }

    @Test
    public void testParseGlobalParamsMap_whenGlobalParamsIsEmpty() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setGlobalParams("");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseGlobalParamsMap", WorkflowInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Property> result = (Map<String, Property>) method.invoke(curingParamsServiceImpl, workflowInstance);

        Assertions.assertTrue(MapUtils.isEmpty(result));
    }

    @Test
    public void testParseGlobalParamsMap_withNullProp() throws Exception {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setGlobalParams("[{\"prop\":null,\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseGlobalParamsMap", WorkflowInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Property> result = (Map<String, Property>) method.invoke(curingParamsServiceImpl, workflowInstance);

        // The current implementation will include a null key
        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(null));
        Assertions.assertEquals("", result.get(null).getValue());
    }

    @Test
    public void testParseVarPool_withValidVarPool() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setVarPool("[{\"prop\":\"var1\",\"value\":\"val1\"}]");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseVarPool", TaskInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Property> result = (List<Property>) method.invoke(curingParamsServiceImpl, taskInstance);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("var1", result.get(0).getProp());
        Assertions.assertEquals("val1", result.get(0).getValue());
    }

    @Test
    public void testParseVarPool_withNullVarPool() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setVarPool(null);

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseVarPool", TaskInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Property> result = (List<Property>) method.invoke(curingParamsServiceImpl, taskInstance);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseVarPool_withEmptyVarPool() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setVarPool("");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseVarPool", TaskInstance.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Property> result = (List<Property>) method.invoke(curingParamsServiceImpl, taskInstance);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseVarPool_withBlankVarPool_throwsException() throws NoSuchMethodException {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setVarPool("   ");

        Method method = CuringParamsServiceImpl.class.getDeclaredMethod("parseVarPool", TaskInstance.class);
        method.setAccessible(true);

        InvocationTargetException exception = Assertions.assertThrows(InvocationTargetException.class,
                () -> method.invoke(curingParamsServiceImpl, taskInstance));

        // Check the root cause
        Truth.assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
        Truth.assertThat(exception.getCause().getMessage()).contains("Parse json");
    }

    @Test
    public void testPreBuildBusinessParams_withScheduleTime() {
        // 1234567890 ms since epoch = 1970-01-15T06:56:07Z
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setScheduleTime(new Date(1234567890L));

        Map<String, Property> result = curingParamsServiceImpl.preBuildBusinessParams(workflowInstance);

        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(DateConstants.PARAMETER_DATETIME));
        // Expect UTC time string
        Assertions.assertEquals("19700115065607", result.get(DateConstants.PARAMETER_DATETIME).getValue());
    }

    @Test
    public void testPreBuildBusinessParams_withoutScheduleTime() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setScheduleTime(null);

        Map<String, Property> result = curingParamsServiceImpl.preBuildBusinessParams(workflowInstance);

        Assertions.assertTrue(MapUtils.isEmpty(result));
    }

    @Test
    public void testGetProjectParameterMap_withParameters() {
        long projectCode = 123456L;

        ProjectParameter param1 = new ProjectParameter();
        param1.setParamName("env");
        param1.setParamValue("prod");
        param1.setParamDataType("VARCHAR");

        ProjectParameter param2 = new ProjectParameter();
        param2.setParamName("timeout");
        param2.setParamValue("30");
        param2.setParamDataType("INTEGER");

        List<ProjectParameter> mockList = Arrays.asList(param1, param2);
        Mockito.when(projectParameterMapper.queryByProjectCode(projectCode)).thenReturn(mockList);

        Map<String, Property> result = curingParamsServiceImpl.getProjectParameterMap(projectCode);

        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.containsKey("env"));
        Assertions.assertTrue(result.containsKey("timeout"));

        Property envProp = result.get("env");
        Assertions.assertEquals("prod", envProp.getValue());
        Assertions.assertEquals(Direct.IN, envProp.getDirect());
        Assertions.assertEquals(DataType.VARCHAR, envProp.getType());

        Property timeoutProp = result.get("timeout");
        Assertions.assertEquals("30", timeoutProp.getValue());
        Assertions.assertEquals(DataType.INTEGER, timeoutProp.getType());
    }

    @Test
    public void testGetProjectParameterMap_withNullParamName() {
        long projectCode = 123456L;

        ProjectParameter param = new ProjectParameter();
        param.setParamName(null); // ← null paramName
        param.setParamValue("test-value");
        param.setParamDataType("VARCHAR");

        List<ProjectParameter> mockList = Collections.singletonList(param);
        Mockito.when(projectParameterMapper.queryByProjectCode(projectCode)).thenReturn(mockList);

        Map<String, Property> result = curingParamsServiceImpl.getProjectParameterMap(projectCode);

        Assertions.assertTrue(MapUtils.isNotEmpty(result));
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(null)); // null key is present
        Property prop = result.get(null);
        Assertions.assertEquals("test-value", prop.getValue());
        Assertions.assertEquals(DataType.VARCHAR, prop.getType());
    }

    @Test
    public void testGetProjectParameterMap_withNoParameters() {
        long projectCode = 999L;
        Mockito.when(projectParameterMapper.queryByProjectCode(projectCode)).thenReturn(Collections.emptyList());

        Map<String, Property> result = curingParamsServiceImpl.getProjectParameterMap(projectCode);

        Assertions.assertTrue(MapUtils.isEmpty(result));
    }

    @Test
    public void testSafePutAll() throws Exception {
        // Arrange
        Map<String, Property> target = new HashMap<>();
        Map<String, Property> source = new HashMap<>();

        Property prop1 = new Property();
        prop1.setProp("validKey");
        prop1.setValue("validValue");

        Property prop2 = new Property();
        prop2.setProp(""); // invalid: blank key
        prop2.setValue("shouldBeSkipped");

        Property prop3 = new Property();
        prop3.setProp("anotherValid");
        prop3.setValue("anotherValue");

        source.put("validKey", prop1);
        source.put("", prop2); // blank key → should be skipped
        source.put("anotherValid", prop3);
        source.put(null, prop1); // null key → should be skipped
        source.put("nullValueKey", null); // null value → should be skipped

        // Get private method
        Method method = CuringParamsServiceImpl.class.getDeclaredMethod(
                "safePutAll",
                Map.class,
                Map.class);
        method.setAccessible(true);

        // Act
        method.invoke(curingParamsServiceImpl, target, source);

        // Assert
        Assertions.assertEquals(2, target.size());
        Assertions.assertTrue(target.containsKey("validKey"));
        Assertions.assertTrue(target.containsKey("anotherValid"));
        Assertions.assertEquals("validValue", target.get("validKey").getValue());
        Assertions.assertEquals("anotherValue", target.get("anotherValid").getValue());

        // Ensure invalid entries were NOT added
        Assertions.assertFalse(target.containsKey(""));
        Assertions.assertFalse(target.containsKey(null));
    }

    @Test
    public void testResolvePlaceholders() throws Exception {
        // Arrange: prepare a paramsMap with placeholders and references
        Map<String, Property> paramsMap = new HashMap<>();

        Property p1 = new Property();
        p1.setProp("name");
        p1.setValue("Alice");

        Property p2 = new Property();
        p2.setProp("greeting");
        p2.setValue("Hello, ${name}!"); // contains placeholder

        Property p3 = new Property();
        p3.setProp("farewell");
        p3.setValue("${greeting} Goodbye."); // chained reference

        Property p4 = new Property();
        p4.setProp("static");
        p4.setValue("no placeholder"); // should remain unchanged

        paramsMap.put("name", p1);
        paramsMap.put("greeting", p2);
        paramsMap.put("farewell", p3);
        paramsMap.put("static", p4);

        // Get the private method via reflection
        Method method = CuringParamsServiceImpl.class.getDeclaredMethod(
                "resolvePlaceholders",
                Map.class);
        method.setAccessible(true);

        // Act: invoke the private method
        method.invoke(curingParamsServiceImpl, paramsMap);

        // Assert: check that placeholders were resolved correctly
        Assertions.assertEquals("Alice", paramsMap.get("name").getValue()); // unchanged
        Assertions.assertEquals("Hello, Alice!", paramsMap.get("greeting").getValue());
        Assertions.assertEquals("Hello, Alice! Goodbye.", paramsMap.get("farewell").getValue());
        Assertions.assertEquals("no placeholder", paramsMap.get("static").getValue());

        // Ensure no unintended side effects
        Assertions.assertEquals(4, paramsMap.size());
    }
}
