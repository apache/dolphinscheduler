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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionServiceImplTest {

    private WorkflowDefinitionServiceImpl workflowDefinitionService;

    @BeforeEach
    void setUp() {
        workflowDefinitionService = new WorkflowDefinitionServiceImpl();
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_replacesAllMappedCodes() throws Exception {
        long old1 = 100L, old2 = 200L, old3 = 300L;
        long new1 = 1000L, new2 = 2000L, new3 = 3000L;

        SwitchParameters.SwitchResult result = new SwitchParameters.SwitchResult();
        result.setNextNode(old2);
        result.setDependTaskList(Arrays.asList(
                createSwitchResultVo(old3),
                createSwitchResultVo(999L)));

        SwitchParameters params = new SwitchParameters();
        params.setNextBranch(old1);
        params.setSwitchResult(result);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(old1, new1);
        codeMap.put(old2, new2);
        codeMap.put(old3, new3);

        invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, codeMap);

        SwitchParameters updated = JSONUtils.parseObject(taskDef.getTaskParams(), SwitchParameters.class);
        assert updated != null;
        assertThat(updated.getNextBranch()).isEqualTo(new1);
        assertThat(updated.getSwitchResult().getNextNode()).isEqualTo(new2);
        assertThat(updated.getSwitchResult().getDependTaskList().get(0).getNextNode()).isEqualTo(new3);
        assertThat(updated.getSwitchResult().getDependTaskList().get(1).getNextNode()).isEqualTo(999L);
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_handlesNullFields() throws Exception {
        SwitchParameters params = new SwitchParameters();
        params.setNextBranch(null);
        params.setSwitchResult(null);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, Collections.emptyMap());

        SwitchParameters result = JSONUtils.parseObject(taskDef.getTaskParams(), SwitchParameters.class);
        assert result != null;
        assertThat(result.getNextBranch()).isNull();
        assertThat(result.getSwitchResult()).isNull();
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_throwsOnInvalidJson() {
        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams("{ broken json }");

        assertThatThrownBy(
                () -> invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, Collections.emptyMap()))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Failed to parse Switch task params");
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_replacesDepAndResultNodes() throws Exception {
        long oldDep = 500L, oldSuc = 600L, oldFail = 700L;
        long newDep = 5000L, newSuc = 6000L, newFail = 7000L;

        ConditionDependentItem item = new ConditionDependentItem();
        item.setDepTaskCode(oldDep);

        ConditionDependentTaskModel model = new ConditionDependentTaskModel();
        model.setDependItemList(Collections.singletonList(item));

        ConditionsParameters.ConditionDependency dep = new ConditionsParameters.ConditionDependency();
        dep.setDependTaskList(Collections.singletonList(model));

        ConditionsParameters.ConditionResult result = new ConditionsParameters.ConditionResult();
        result.setSuccessNode(Arrays.asList(oldSuc, 888L));
        result.setFailedNode(Collections.singletonList(oldFail));

        ConditionsParameters params = new ConditionsParameters();
        params.setDependence(dep);
        params.setConditionResult(result);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(oldDep, newDep);
        codeMap.put(oldSuc, newSuc);
        codeMap.put(oldFail, newFail);

        invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef, codeMap);

        ConditionsParameters updated = JSONUtils.parseObject(taskDef.getTaskParams(), ConditionsParameters.class);

        assert updated != null;
        long actualDepCode = updated.getDependence()
                .getDependTaskList().get(0).getDependItemList().get(0).getDepTaskCode();
        assertThat(actualDepCode).isEqualTo(newDep);

        assertThat(updated.getConditionResult().getSuccessNode())
                .containsExactly(newSuc, 888L);
        assertThat(updated.getConditionResult().getFailedNode())
                .containsExactly(newFail);
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_handlesNulls() throws Exception {
        ConditionsParameters params = new ConditionsParameters();
        params.setDependence(null);
        params.setConditionResult(null);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef, Collections.emptyMap());

        ConditionsParameters result = JSONUtils.parseObject(taskDef.getTaskParams(), ConditionsParameters.class);
        assert result != null;
        assertThat(result.getDependence()).isNull();
        assertThat(result.getConditionResult()).isNull();
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_throwsOnInvalidJson() {
        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams("{ invalid: , }");

        Assertions
                .assertThatThrownBy(() -> invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef,
                        Collections.emptyMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse Condition task params");
    }

    @Test
    void testReplaceInNodeList_withNullList() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(null);
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isNull();
    }

    @Test
    void testReplaceInNodeList_withEmptyList() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>());
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isEmpty();
    }

    @Test
    void testReplaceInNodeList_replacesMappedCodes() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(1L, 10L);
        codeMap.put(3L, 30L);

        invokeReplaceInNodeList(getter, setter, codeMap);

        assertThat(stateRef.get()).containsExactly(10L, 2L, 30L);
    }

    @Test
    void testReplaceInNodeList_preservesUnmappedAndNullElements() throws Exception {
        AtomicReference<List<Long>> stateRef =
                new AtomicReference<>(new ArrayList<>(Arrays.asList(null, 4L, 5L, null)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(4L, 40L);

        invokeReplaceInNodeList(getter, setter, codeMap);

        assertThat(stateRef.get()).containsExactly((Long) null, 40L, 5L, (Long) null);
    }

    @Test
    void testReplaceInNodeList_noOpWhenCodeMapIsEmpty() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(Arrays.asList(6L, 7L)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).containsExactly(6L, 7L);
    }

    @Test
    void testReplaceInNodeList_createsNewListInstance() throws Exception {
        List<Long> original = Arrays.asList(8L, 9L);
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(original));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isNotSameAs(original);
        assertThat(stateRef.get()).isEqualTo(original);
    }

    // Reflection Helper to call private replaceInNodeList
    private void invokeReplaceInNodeList(Supplier<List<Long>> getter,
                                         Consumer<List<Long>> setter,
                                         Map<Long, Long> codeMap) throws Exception {
        Method method = WorkflowDefinitionServiceImpl.class
                .getDeclaredMethod("replaceInNodeList", Supplier.class, Consumer.class, Map.class);
        method.setAccessible(true);

        try {
            method.invoke(workflowDefinitionService, getter, setter, codeMap);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Exception in private method", cause);
        }
    }

    // Reflection Helper to call private replaceTaskCodeForSwitchTaskParams and replaceTaskCodeForConditionTaskParams
    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        if ("replaceTaskCodeForSwitchTaskParams".equals(methodName) ||
                "replaceTaskCodeForConditionTaskParams".equals(methodName)) {
            argTypes[0] = TaskDefinitionLog.class;
            argTypes[1] = Map.class;
        }

        Method method = WorkflowDefinitionServiceImpl.class.getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);

        try {
            method.invoke(workflowDefinitionService, args);
        } catch (InvocationTargetException e) {
            // Unwrap the actual exception thrown by the private method
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Wrap checked exceptions in a RuntimeException or rethrow as Exception
                throw new RuntimeException("Checked exception thrown from private method", cause);
            }
        }
    }

    // Helper to create SwitchResultVo
    private SwitchResultVo createSwitchResultVo(Long nextNode) {
        SwitchResultVo vo = new SwitchResultVo();
        vo.setNextNode(nextNode);
        return vo;
    }
}
