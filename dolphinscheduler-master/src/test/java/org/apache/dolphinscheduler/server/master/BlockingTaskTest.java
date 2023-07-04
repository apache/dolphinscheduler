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

// public class BlockingTaskTest {
//
// /**
// * TaskNode.runFlag : task can be run normally
// */
// public static final String FLOW_NODE_RUN_FLAG_NORMAL = "NORMAL";
//
// private ProcessService processService;
//
// private TaskInstanceDao taskInstanceDao;
//
// private TaskDefinitionDao taskDefinitionDao;
//
// private ProcessInstance processInstance;
//
// private MasterConfig config;
//
// private MockedStatic<SpringApplicationContext> mockedStaticSpringApplicationContext;
//
// @BeforeEach
// public void before() {
// // mock master
// config = new MasterConfig();
// config.setTaskCommitRetryTimes(3);
// config.setTaskCommitInterval(Duration.ofSeconds(1));
//
// mockedStaticSpringApplicationContext = Mockito.mockStatic(SpringApplicationContext.class);
// Mockito.when(SpringApplicationContext.getBean(MasterConfig.class)).thenReturn(config);
//
// // mock process service
// processService = Mockito.mock(ProcessService.class);
// Mockito.when(SpringApplicationContext.getBean(ProcessService.class)).thenReturn(processService);
//
// taskInstanceDao = Mockito.mock(TaskInstanceDao.class);
// Mockito.when(SpringApplicationContext.getBean(TaskInstanceDao.class)).thenReturn(taskInstanceDao);
//
// taskDefinitionDao = Mockito.mock(TaskDefinitionDao.class);
// Mockito.when(SpringApplicationContext.getBean(TaskDefinitionDao.class)).thenReturn(taskDefinitionDao);
//
// // mock process instance
// processInstance = getProcessInstance();
// Mockito.when(processService
// .findProcessInstanceById(processInstance.getId()))
// .thenReturn(processInstance);
//
// TaskDefinition taskDefinition = new TaskDefinition();
// taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
// taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
// taskDefinition.setTimeout(0);
// Mockito.when(taskDefinitionDao.findTaskDefinition(1L, 1))
// .thenReturn(taskDefinition);
// }
//
// @AfterEach
// public void after() {
// mockedStaticSpringApplicationContext.close();
// }
//
// private ProcessInstance getProcessInstance() {
// // mock process instance
// ProcessInstance processInstance = new ProcessInstance();
// processInstance.setId(1000);
// processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
// processInstance.setProcessDefinitionCode(1L);
//
// return processInstance;
// }
//
// private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
// // wrap taskNode
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setId(100);
// taskInstance.setName(taskNode.getName());
// taskInstance.setTaskType(taskNode.getType().toUpperCase());
// taskInstance.setTaskCode(taskNode.getCode());
// taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
// taskInstance.setProcessInstanceId(processInstance.getId());
// taskInstance.setTaskParams(taskNode.getTaskParams());
// taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
// taskInstance.setFirstSubmitTime(new Date());
// Mockito.when(processService
// .submitTaskWithRetry(Mockito.any(ProcessInstance.class), Mockito.any(TaskInstance.class),
// Mockito.any(Integer.class), Mockito.any(Long.class)))
// .thenReturn(taskInstance);
// return taskInstance;
// }
//
// private TaskNode getTaskNode(String blockingCondition) {
// // mock task nodes
// // 1----\
// // 2-----4(Blocking Node)
// // 3----/
// // blocking logic: 1-->SUCCESS 2-->SUCCESS 3-->SUCCESS
// TaskNode taskNode = new TaskNode();
// taskNode.setId("tasks-1000");
// taskNode.setName("4");
// taskNode.setCode(1L);
// taskNode.setVersion(1);
// taskNode.setType(TASK_TYPE_BLOCKING);
// taskNode.setRunFlag(FLOW_NODE_RUN_FLAG_NORMAL);
//
// DependentItem dependentItemA = new DependentItem();
// dependentItemA.setDepTaskCode(1L);
// dependentItemA.setStatus(TaskExecutionStatus.SUCCESS);
//
// DependentItem dependentItemB = new DependentItem();
// dependentItemB.setDepTaskCode(2L);
// dependentItemB.setStatus(TaskExecutionStatus.SUCCESS);
//
// DependentItem dependentItemC = new DependentItem();
// dependentItemC.setDepTaskCode(3L);
// dependentItemC.setStatus(TaskExecutionStatus.SUCCESS);
//
// // build relation
// DependentTaskModel dependentTaskModel = new DependentTaskModel();
// dependentTaskModel.setDependItemList(Stream.of(dependentItemA, dependentItemB, dependentItemC)
// .collect(Collectors.toList()));
// dependentTaskModel.setRelation(DependentRelation.AND);
//
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
// dependentParameters.setRelation(DependentRelation.AND);
//
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
// // set blocking node params
// BlockingParameters blockingParameters = new BlockingParameters();
// blockingParameters.setAlertWhenBlocking(false);
// blockingParameters.setBlockingCondition(blockingCondition);
//
// taskNode.setParams(JSONUtils.toJsonString(blockingParameters));
//
// return taskNode;
// }
//
// private TaskInstance testBasicInit(String blockingCondition, TaskExecutionStatus... expectResults) {
//
// TaskInstance taskInstance = getTaskInstance(getTaskNode(blockingCondition), processInstance);
//
// Mockito.when(processService
// .submitTask(processInstance, taskInstance))
// .thenReturn(taskInstance);
//
// Mockito.when(taskInstanceDao
// .findTaskInstanceById(taskInstance.getId()))
// .thenReturn(taskInstance);
//
// // for BlockingTaskExecThread.initTaskParameters
// Mockito.when(taskInstanceDao.upsertTaskInstance(taskInstance))
// .thenReturn(true);
//
// // for BlockingTaskExecThread.updateTaskState
// Mockito.when(taskInstanceDao
// .updateTaskInstance(taskInstance))
// .thenReturn(true);
//
// // for BlockingTaskExecThread.waitTaskQuit
// List<TaskInstance> conditions = getTaskInstanceForValidTaskList(expectResults);
// Mockito.when(
// taskInstanceDao.findValidTaskListByProcessId(processInstance.getId(), processInstance.getTestFlag()))
// .thenReturn(conditions);
// taskInstance.setProcessInstance(processInstance);
// return taskInstance;
// }
//
// /**
// * mock task instance and its execution result in front of blocking node
// */
// private List<TaskInstance> getTaskInstanceForValidTaskList(TaskExecutionStatus... status) {
// List<TaskInstance> taskInstanceList = new ArrayList<>();
// for (int i = 1; i <= status.length; i++) {
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setId(i);
// taskInstance.setName(String.valueOf(i));
// taskInstance.setState(status[i - 1]);
// taskInstanceList.add(taskInstance);
// }
// return taskInstanceList;
// }
//
// @Test
// public void testBlockingTaskSubmit() {
// TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
// TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
// BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
// blockingTaskProcessor.init(taskInstance, processInstance);
// boolean res = blockingTaskProcessor.action(TaskAction.SUBMIT);
// Assertions.assertTrue(res);
// }
//
// @Test
// public void testPauseTask() {
// TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
// TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
// BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
// blockingTaskProcessor.init(taskInstance, processInstance);
// blockingTaskProcessor.action(TaskAction.SUBMIT);
// blockingTaskProcessor.action(TaskAction.PAUSE);
// TaskExecutionStatus status = taskInstance.getState();
// Assertions.assertEquals(TaskExecutionStatus.PAUSE, status);
// }
//
// @Test
// public void testBlocking() {
// TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
// TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
// BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
// blockingTaskProcessor.init(taskInstance, processInstance);
// blockingTaskProcessor.action(TaskAction.SUBMIT);
// blockingTaskProcessor.action(TaskAction.RUN);
// WorkflowExecutionStatus status = processInstance.getState();
// Assertions.assertEquals(WorkflowExecutionStatus.READY_BLOCK, status);
// }
//
// @Test
// public void testNoneBlocking() {
// TaskInstance taskInstance = testBasicInit("BlockingOnSuccess",
// TaskExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS);
// BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
// blockingTaskProcessor.init(taskInstance, processInstance);
// blockingTaskProcessor.action(TaskAction.SUBMIT);
// blockingTaskProcessor.action(TaskAction.RUN);
// WorkflowExecutionStatus status = processInstance.getState();
// Assertions.assertEquals(WorkflowExecutionStatus.RUNNING_EXECUTION, status);
// }
// }
