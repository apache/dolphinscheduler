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

/**
 * DependentTaskTest
 */
// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// public class DependentTaskTest {
//
// /**
// * TaskNode.runFlag : task can be run normally
// */
// public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";
//
// public static final Long TASK_CODE = 1111L;
// public static final Long DEPEND_TASK_CODE_A = 110L;
// public static final Long DEPEND_TASK_CODE_B = 111L;
// public static final Long DEPEND_TASK_CODE_C = 112L;
// public static final Long DEPEND_TASK_CODE_D = 113L;
// public static final int TASK_VERSION = 1;
//
// private ProcessService processService;
//
// private ProcessInstanceDao processInstanceDao;
//
// private TaskInstanceDao taskInstanceDao;
//
// private TaskDefinitionDao taskDefinitionDao;
//
// /**
// * the dependent task to be tested
// * ProcessDefinition id=1
// * Task id=task-10, name=D
// * ProcessInstance id=100
// * TaskInstance id=1000
// * notice: must be initialized by setupTaskInstance() on each test case
// */
// private ProcessInstance processInstance;
// private TaskInstance taskInstance;
//
// @BeforeEach
// public void before() {
// ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
// SpringApplicationContext springApplicationContext = new SpringApplicationContext();
// springApplicationContext.setApplicationContext(applicationContext);
//
// MasterConfig config = new MasterConfig();
// config.setTaskCommitRetryTimes(3);
// config.setTaskCommitInterval(Duration.ofSeconds(1));
// Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
//
// processService = Mockito.mock(ProcessService.class);
// Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
//
// processInstanceDao = Mockito.mock(ProcessInstanceDao.class);
// Mockito.when(applicationContext.getBean(ProcessInstanceDao.class)).thenReturn(processInstanceDao);
//
// taskInstanceDao = Mockito.mock(TaskInstanceDao.class);
// Mockito.when(applicationContext.getBean(TaskInstanceDao.class)).thenReturn(taskInstanceDao);
//
// taskDefinitionDao = Mockito.mock(TaskDefinitionDao.class);
// Mockito.when(SpringApplicationContext.getBean(TaskDefinitionDao.class)).thenReturn(taskDefinitionDao);
//
// processInstance = getProcessInstance();
// taskInstance = getTaskInstance();
//
// // for MasterBaseTaskExecThread.call
// // for DependentTaskExecThread.waitTaskQuit
// Mockito.when(processService
// .findProcessInstanceById(100))
// .thenAnswer(i -> processInstance);
//
// // for MasterBaseTaskExecThread.submit
// Mockito.when(processService
// .submitTask(processInstance, taskInstance))
// .thenAnswer(i -> taskInstance);
//
// // for DependentTaskExecThread.initTaskParameters
// Mockito.when(taskInstanceDao
// .updateTaskInstance(Mockito.any()))
// .thenReturn(true);
// // for DependentTaskExecThread.updateTaskState
// Mockito.when(taskInstanceDao.upsertTaskInstance(Mockito.any()))
// .thenReturn(true);
//
// // for DependentTaskExecThread.waitTaskQuit
// Mockito.when(taskInstanceDao
// .findTaskInstanceById(1000))
// .thenAnswer(i -> taskInstance);
//
// Mockito.when(taskDefinitionDao.findTaskDefinition(TASK_CODE, TASK_VERSION))
// .thenReturn(getTaskDefinition());
// }
//
// private void testBasicInit() {
// TaskNode taskNode = getDependantTaskNode();
// DependentTaskModel dependentTaskModel = new DependentTaskModel();
// dependentTaskModel.setRelation(DependentRelation.AND);
// dependentTaskModel.setDependItemList(Stream.of(
// getDependentItemFromTaskNode(2L, DEPEND_TASK_CODE_A, "today", "day")).collect(Collectors.toList()));
//
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setRelation(DependentRelation.AND);
// dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
//
// // dependence: AND(AND(2-A-day-today))
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
// setupTaskInstance(taskNode);
// }
//
// @Test
// public void testBasicSuccess() {
// testBasicInit();
// ProcessInstance dependentProcessInstance =
// getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.FAILURE);
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(dependentProcessInstance);
//
// // for DependentExecute.getDependTaskResult
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(200, 0))
// .thenReturn(Stream.of(
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.SUCCESS, DEPEND_TASK_CODE_A,
// dependentProcessInstance),
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.FAILURE, DEPEND_TASK_CODE_B,
// dependentProcessInstance))
// .collect(Collectors.toList()));
//
// }
//
// @Test
// public void testBasicFailure() {
// testBasicInit();
// ProcessInstance dependentProcessInstance =
// getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.SUCCESS);
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(dependentProcessInstance);
//
// // for DependentExecute.getDependTaskResult
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(200, 0))
// .thenReturn(Stream.of(
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.FAILURE, DEPEND_TASK_CODE_A,
// dependentProcessInstance),
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.SUCCESS, DEPEND_TASK_CODE_B,
// dependentProcessInstance))
// .collect(Collectors.toList()));
// }
//
// @Test
// public void testDependentRelation() {
// DependentTaskModel dependentTaskModel1 = new DependentTaskModel();
// dependentTaskModel1.setRelation(DependentRelation.AND);
// dependentTaskModel1.setDependItemList(Stream.of(
// getDependentItemFromTaskNode(2L, DEPEND_TASK_CODE_A, "today", "day"),
// getDependentItemFromTaskNode(3L, DEPEND_TASK_CODE_B, "today", "day")).collect(Collectors.toList()));
//
// DependentTaskModel dependentTaskModel2 = new DependentTaskModel();
// dependentTaskModel2.setRelation(DependentRelation.OR);
// dependentTaskModel2.setDependItemList(Stream.of(
// getDependentItemFromTaskNode(2L, DEPEND_TASK_CODE_A, "today", "day"),
// getDependentItemFromTaskNode(3L, DEPEND_TASK_CODE_C, "today", "day")).collect(Collectors.toList()));
//
// /*
// * OR AND 2-A-day-today 3-B-day-today OR 2-A-day-today 3-C-day-today
// */
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setRelation(DependentRelation.OR);
// dependentParameters.setDependTaskList(Stream.of(
// dependentTaskModel1,
// dependentTaskModel2).collect(Collectors.toList()));
//
// TaskNode taskNode = getDependantTaskNode();
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
// setupTaskInstance(taskNode);
//
// ProcessInstance processInstance200 =
// getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.FAILURE);
// ProcessInstance processInstance300 =
// getProcessInstanceForFindLastRunningProcess(300, WorkflowExecutionStatus.SUCCESS);
//
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(processInstance200);
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(3L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(processInstance300);
//
// // for DependentExecute.getDependTaskResult
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(200, 0))
// .thenReturn(Stream.of(
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.FAILURE, DEPEND_TASK_CODE_A,
// processInstance200))
// .collect(Collectors.toList()));
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(300, 0))
// .thenReturn(Stream.of(
// getTaskInstanceForValidTaskList(3000, TaskExecutionStatus.SUCCESS, DEPEND_TASK_CODE_B,
// processInstance300),
// getTaskInstanceForValidTaskList(3001, TaskExecutionStatus.SUCCESS, DEPEND_TASK_CODE_C,
// processInstance300))
// .collect(Collectors.toList()));
//
// }
//
// /**
// * test when dependent on ALL tasks in another process
// */
// private void testDependentOnAllInit() {
// TaskNode taskNode = getDependantTaskNode();
// DependentTaskModel dependentTaskModel = new DependentTaskModel();
// dependentTaskModel.setRelation(DependentRelation.AND);
// dependentTaskModel.setDependItemList(Stream.of(
// getDependentItemFromTaskNode(2L, Constants.DEPENDENT_ALL_TASK_CODE, "today", "day"))
// .collect(Collectors.toList()));
//
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setRelation(DependentRelation.AND);
// dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
//
// // dependence: AND(AND(2:ALL today day))
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
// setupTaskInstance(taskNode);
// }
//
// @Test
// public void testDependentOnAllSuccess() {
// testDependentOnAllInit();
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.SUCCESS));
//
// }
//
// @Test
// public void testDependentOnAllFailure() {
// testDependentOnAllInit();
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.FAILURE));
//
// }
//
// /**
// * test whether waitTaskQuit has been well impl
// */
// @Test
// public void testWaitAndCancel() {
// // for the poor independence of UT, error on other place may causes the condition happens
// if (!ServerLifeCycleManager.isRunning()) {
// return;
// }
//
// TaskNode taskNode = getDependantTaskNode();
// DependentTaskModel dependentTaskModel = new DependentTaskModel();
// dependentTaskModel.setRelation(DependentRelation.AND);
// dependentTaskModel.setDependItemList(Stream.of(
// getDependentItemFromTaskNode(2L, DEPEND_TASK_CODE_A, "today", "day")).collect(Collectors.toList()));
//
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setRelation(DependentRelation.AND);
// dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
//
// // dependence: AND(AND(2:A today day))
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
// setupTaskInstance(taskNode);
//
// ProcessInstance dependentProcessInstance =
// getProcessInstanceForFindLastRunningProcess(200, WorkflowExecutionStatus.RUNNING_EXECUTION);
// // for DependentExecute.findLastProcessInterval
// Mockito.when(processInstanceDao
// .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.anyInt()))
// .thenReturn(dependentProcessInstance);
//
// // DependentTaskExecThread taskExecThread = new DependentTaskExecThread(taskInstance);
//
// // for DependentExecute.getDependTaskResult
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(200, 0))
// .thenAnswer(i -> {
// processInstance.setState(WorkflowExecutionStatus.READY_STOP);
// return Stream.of(
// getTaskInstanceForValidTaskList(2000, TaskExecutionStatus.RUNNING_EXECUTION,
// DEPEND_TASK_CODE_A, dependentProcessInstance))
// .collect(Collectors.toList());
// })
// .thenThrow(new IllegalStateException("have not been stopped as expected"));
//
// }
//
// @Test
// public void testIsSelfDependent() {
// DependentExecute dependentExecute =
// new DependentExecute(new ArrayList<>(), DependentRelation.AND, processInstance, taskInstance);
// DependentItem dependentItem = new DependentItem();
// dependentItem.setDefinitionCode(processInstance.getProcessDefinitionCode());
// dependentItem.setDepTaskCode(Constants.DEPENDENT_ALL_TASK_CODE);
// Assertions.assertTrue(dependentExecute.isSelfDependent(dependentItem));
//
// dependentItem.setDepTaskCode(taskInstance.getTaskCode());
// Assertions.assertTrue(dependentExecute.isSelfDependent(dependentItem));
//
// // no self task
// dependentItem.setDepTaskCode(12345678);
// Assertions.assertFalse(dependentExecute.isSelfDependent(dependentItem));
//
// // no self wf
// dependentItem.setDefinitionCode(processInstance.getProcessDefinitionCode());
// Assertions.assertFalse(dependentExecute.isSelfDependent(dependentItem));
// }
//
// @Test
// public void testIsFirstProcessInstance() {
// Mockito.when(processInstanceDao.queryFirstScheduleProcessInstance(processInstance.getProcessDefinitionCode()))
// .thenReturn(processInstance);
// DependentExecute dependentExecute =
// new DependentExecute(new ArrayList<>(), DependentRelation.AND, processInstance, taskInstance);
// DependentItem dependentItem = new DependentItem();
// dependentItem.setDefinitionCode(processInstance.getProcessDefinitionCode());
// Assertions.assertTrue(dependentExecute.isFirstProcessInstance(dependentItem));
//
// dependentItem.setDefinitionCode(12345678L);
// Assertions.assertFalse(dependentExecute.isFirstProcessInstance(dependentItem));
// }
//
// private ProcessInstance getProcessInstance() {
// ProcessInstance processInstance = new ProcessInstance();
// processInstance.setId(100);
// processInstance.setProcessDefinitionCode(10000L);
// processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
// return processInstance;
// }
//
// private TaskInstance getTaskInstance() {
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setId(1000);
// taskInstance.setTaskCode(10000L);
// return taskInstance;
// }
//
// /**
// * task that dependent on others (and to be tested here)
// * notice: should be filled with setDependence() and be passed to setupTaskInstance()
// */
// private TaskNode getDependantTaskNode() {
// TaskNode taskNode = new TaskNode();
// taskNode.setId("tasks-10");
// taskNode.setName("D");
// taskNode.setCode(DEPEND_TASK_CODE_D);
// taskNode.setType("DEPENDENT");
// taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);
// return taskNode;
// }
//
// private TaskDefinition getTaskDefinition() {
// TaskDefinition taskDefinition = new TaskDefinition();
// taskDefinition.setCode(TASK_CODE);
// taskDefinition.setVersion(TASK_VERSION);
// taskDefinition.setTimeoutFlag(TimeoutFlag.CLOSE);
// taskDefinition.setTimeout(0);
// return taskDefinition;
// }
//
// private void setupTaskInstance(TaskNode taskNode) {
// taskInstance = new TaskInstance();
// taskInstance.setId(1000);
// taskInstance.setTaskCode(TASK_CODE);
// taskInstance.setTaskDefinitionVersion(TASK_VERSION);
// taskInstance.setProcessInstanceId(processInstance.getId());
// taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
// taskInstance.setTaskType(taskNode.getType().toUpperCase());
// taskInstance.setDependency(JSONUtils.parseObject(taskNode.getDependence(), DependentParameters.class));
// taskInstance.setName(taskNode.getName());
// }
//
// /**
// * DependentItem defines the condition for the dependent
// */
// private DependentItem getDependentItemFromTaskNode(Long processDefinitionCode, long taskCode, String date,
// String cycle) {
// DependentItem dependentItem = new DependentItem();
// dependentItem.setDefinitionCode(processDefinitionCode);
// dependentItem.setDepTaskCode(taskCode);
// dependentItem.setDateValue(date);
// dependentItem.setCycle(cycle);
// // so far, the following fields have no effect
// dependentItem.setDependResult(DependResult.SUCCESS);
// dependentItem.setStatus(TaskExecutionStatus.SUCCESS);
// return dependentItem;
// }
//
// private ProcessInstance getProcessInstanceForFindLastRunningProcess(int processInstanceId,
// WorkflowExecutionStatus state) {
// ProcessInstance processInstance = new ProcessInstance();
// processInstance.setId(processInstanceId);
// processInstance.setState(state);
// processInstance.setTestFlag(0);
// return processInstance;
// }
//
// private TaskInstance getTaskInstanceForValidTaskList(
// int taskInstanceId, TaskExecutionStatus state,
// long taskCode, ProcessInstance processInstance) {
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setTaskType("DEPENDENT");
// taskInstance.setId(taskInstanceId);
// taskInstance.setTaskCode(taskCode);
// taskInstance.setProcessInstanceId(processInstance.getId());
// taskInstance.setState(state);
// return taskInstance;
// }
// }
