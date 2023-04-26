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

// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// public class ConditionsTaskTest {
//
// /**
// * TaskNode.runFlag : task can be run normally
// */
// public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";
//
// private ProcessService processService;
//
// private ProcessInstance processInstance;
//
// private TaskInstanceDao taskInstanceDao;
//
// private TaskDefinitionDao taskDefinitionDao;
//
// @BeforeEach
// public void before() {
// ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
// SpringApplicationContext springApplicationContext = new SpringApplicationContext();
// springApplicationContext.setApplicationContext(applicationContext);
//
// MasterConfig config = new MasterConfig();
// Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
// config.setTaskCommitRetryTimes(3);
// config.setTaskCommitInterval(Duration.ofSeconds(1));
//
// processService = Mockito.mock(ProcessService.class);
// Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
//
// taskInstanceDao = Mockito.mock(TaskInstanceDao.class);
// Mockito.when(applicationContext.getBean(TaskInstanceDao.class)).thenReturn(taskInstanceDao);
//
// taskDefinitionDao = Mockito.mock(TaskDefinitionDao.class);
// Mockito.when(SpringApplicationContext.getBean(TaskDefinitionDao.class)).thenReturn(taskDefinitionDao);
//
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
// private TaskInstance testBasicInit(TaskExecutionStatus expectResult) {
// TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);
//
// // for MasterBaseTaskExecThread.submit
// Mockito.when(processService
// .submitTask(processInstance, taskInstance))
// .thenReturn(taskInstance);
// // for MasterBaseTaskExecThread.call
// Mockito.when(taskInstanceDao
// .findTaskInstanceById(taskInstance.getId()))
// .thenReturn(taskInstance);
// // for ConditionsTaskExecThread.initTaskParameters
// Mockito.when(taskInstanceDao.upsertTaskInstance(taskInstance))
// .thenReturn(true);
// // for ConditionsTaskExecThread.updateTaskState
// Mockito.when(taskInstanceDao
// .updateTaskInstance(taskInstance))
// .thenReturn(true);
//
// // for ConditionsTaskExecThread.waitTaskQuit
// List<TaskInstance> conditions = Stream.of(
// getTaskInstanceForValidTaskList(expectResult)).collect(Collectors.toList());
// Mockito.when(taskInstanceDao
// .findValidTaskListByProcessId(processInstance.getId(), processInstance.getTestFlag()))
// .thenReturn(conditions);
// return taskInstance;
// }
//
// private TaskNode getTaskNode() {
// TaskNode taskNode = new TaskNode();
// taskNode.setId("tasks-1000");
// taskNode.setName("C");
// taskNode.setCode(1L);
// taskNode.setVersion(1);
// taskNode.setType("CONDITIONS");
// taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);
//
// DependentItem dependentItem = new DependentItem();
// dependentItem.setDepTaskCode(11L);
// dependentItem.setStatus(TaskExecutionStatus.SUCCESS);
//
// DependentTaskModel dependentTaskModel = new DependentTaskModel();
// dependentTaskModel.setDependItemList(Stream.of(dependentItem).collect(Collectors.toList()));
// dependentTaskModel.setRelation(DependentRelation.AND);
//
// DependentParameters dependentParameters = new DependentParameters();
// dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
// dependentParameters.setRelation(DependentRelation.AND);
//
// // in: AND(AND(1 is SUCCESS))
// taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
// ConditionsParameters conditionsParameters = new ConditionsParameters();
// conditionsParameters.setSuccessNode(Stream.of("2").collect(Collectors.toList()));
// conditionsParameters.setFailedNode(Stream.of("3").collect(Collectors.toList()));
//
// // out: SUCCESS => 2, FAILED => 3
// taskNode.setConditionResult(JSONUtils.toJsonString(conditionsParameters));
//
// return taskNode;
// }
//
// private ProcessInstance getProcessInstance() {
// ProcessInstance processInstance = new ProcessInstance();
// processInstance.setId(1000);
// processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
//
// return processInstance;
// }
//
// private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setId(1000);
// taskInstance.setName(taskNode.getName());
// taskInstance.setTaskType(taskNode.getType().toUpperCase());
// taskInstance.setTaskCode(taskNode.getCode());
// taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
// taskInstance.setProcessInstanceId(processInstance.getId());
// taskInstance.setTaskParams(taskNode.getTaskParams());
// return taskInstance;
// }
//
// private TaskInstance getTaskInstanceForValidTaskList(TaskExecutionStatus state) {
// TaskInstance taskInstance = new TaskInstance();
// taskInstance.setId(1001);
// taskInstance.setName("1");
// taskInstance.setState(state);
// return taskInstance;
// }
// }
