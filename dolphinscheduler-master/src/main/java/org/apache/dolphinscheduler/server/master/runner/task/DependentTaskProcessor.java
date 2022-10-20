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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.master.utils.DependentExecute;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;

/**
 * dependent task processor
 */
@AutoService(ITaskProcessor.class)
public class DependentTaskProcessor extends BaseTaskProcessor {

    private DependentParameters dependentParameters;

    private final ProcessDefinitionMapper processDefinitionMapper =
            SpringApplicationContext.getBean(ProcessDefinitionMapper.class);

    private final TaskDefinitionMapper taskDefinitionMapper =
            SpringApplicationContext.getBean(TaskDefinitionMapper.class);

    private final ProjectMapper projectMapper = SpringApplicationContext.getBean(ProjectMapper.class);

    /**
     * dependent task list
     */
    private List<DependentExecute> dependentTaskList = new ArrayList<>();

    /**
     * depend item result map
     * save the result to log file
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    private Map<Long, Project> projectCodeMap = new HashMap<>();
    private Map<Long, ProcessDefinition> processDefinitionMap = new HashMap<>();
    private Map<Long, TaskDefinition> taskDefinitionMap = new HashMap<>();

    /**
     * dependent date
     */
    private Date dependentDate;

    DependResult result;

    /**
     * test flag
     */
    private int testFlag;

    boolean allDependentItemFinished;

    @Override
    public boolean submitTask() {
        try {
            this.taskInstance =
                    processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

            if (this.taskInstance == null) {
                return false;
            }
            this.setTaskExecutionLogger();
            logger.info("Dependent task submit success");
            taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(),
                    processInstance.getProcessDefinitionCode(),
                    processInstance.getProcessDefinitionVersion(),
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getId()));
            taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
            taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
            taskInstance.setStartTime(new Date());
            processService.updateTaskInstance(taskInstance);
            initDependParameters();
            logger.info("Success initialize dependent task parameters, the dependent data is: {}", dependentDate);
            return true;
        } catch (Exception ex) {
            logger.error("Submit/Initialize dependent task error", ex);
            return false;
        }
    }

    @Override
    public boolean runTask() {
        if (!allDependentItemFinished) {
            allDependentItemFinished = allDependentTaskFinish();
        }
        if (allDependentItemFinished) {
            getTaskDependResult();
            endTask();
        }
        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED != taskTimeoutStrategy
                && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy) {
            return true;
        }
        logger.info("dependent taskInstanceId: {} timeout, taskName: {}, strategy: {} ",
                taskInstance.getId(), taskInstance.getName(), taskTimeoutStrategy.getDescp());
        result = DependResult.FAILED;
        endTask();
        return true;
    }

    /**
     * init dependent parameters
     */
    private void initDependParameters() {
        this.dependentParameters = taskInstance.getDependency();
        if (processInstance.getScheduleTime() != null) {
            this.dependentDate = this.processInstance.getScheduleTime();
        } else {
            this.dependentDate = new Date();
        }
        this.testFlag = processInstance.getTestFlag();
        // check dependent project is exist
        List<DependentTaskModel> dependTaskList = dependentParameters.getDependTaskList();
        Set<Long> projectCodes = new HashSet<>();
        Set<Long> processDefinitionCodes = new HashSet<>();
        Set<Long> taskDefinitionCodes = new HashSet<>();
        dependTaskList.forEach(dependentTaskModel -> {
            dependentTaskModel.getDependItemList().forEach(dependentItem -> {
                projectCodes.add(dependentItem.getProjectCode());
                processDefinitionCodes.add(dependentItem.getDefinitionCode());
                taskDefinitionCodes.add(dependentItem.getDepTaskCode());
            });
        });
        projectCodeMap = projectMapper.queryByCodes(projectCodes).stream()
                .collect(Collectors.toMap(Project::getCode, Function.identity()));
        processDefinitionMap = processDefinitionMapper.queryByCodes(processDefinitionCodes).stream()
                .collect(Collectors.toMap(ProcessDefinition::getCode, Function.identity()));
        taskDefinitionMap = taskDefinitionMapper.queryByCodeList(taskDefinitionCodes).stream()
                .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));

        for (DependentTaskModel taskModel : dependentParameters.getDependTaskList()) {
            logger.info("Add sub dependent check tasks, dependent relation: {}", taskModel.getRelation());
            for (DependentItem dependentItem : taskModel.getDependItemList()) {
                Project project = projectCodeMap.get(dependentItem.getProjectCode());
                if (project == null) {
                    logger.error("The dependent task's project is not exist, dependentItem: {}", dependentItem);
                    throw new RuntimeException(
                            "The dependent task's project is not exist, dependentItem: " + dependentItem);
                }
                ProcessDefinition processDefinition = processDefinitionMap.get(dependentItem.getDefinitionCode());
                if (processDefinition == null) {
                    logger.error("The dependent task's workflow is not exist, dependentItem: {}", dependentItem);
                    throw new RuntimeException(
                            "The dependent task's workflow is not exist, dependentItem: " + dependentItem);
                }
                if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                    logger.info(
                            "Add dependent task: projectName: {}, workflowName: {}, taskName: ALL, dependentKey: {}",
                            project.getName(), processDefinition.getName(), dependentItem.getKey());

                } else {
                    TaskDefinition taskDefinition = taskDefinitionMap.get(dependentItem.getDepTaskCode());
                    if (taskDefinition == null) {
                        logger.error("The dependent task's taskDefinition is not exist, dependentItem: {}",
                                dependentItem);
                        throw new RuntimeException(
                                "The dependent task's taskDefinition is not exist, dependentItem: " + dependentItem);
                    }
                    logger.info("Add dependent task: projectName: {}, workflowName: {}, taskName: {}, dependentKey: {}",
                            project.getName(), processDefinition.getName(), taskDefinition.getName(),
                            dependentItem.getKey());
                }
            }
            this.dependentTaskList.add(new DependentExecute(taskModel.getDependItemList(), taskModel.getRelation()));
        }
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(TaskExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean killTask() {
        this.taskInstance.setState(TaskExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    /**
     * judge all dependent tasks finish
     *
     * @return whether all dependent tasks finish
     */
    private boolean allDependentTaskFinish() {
        boolean finish = true;
        for (DependentExecute dependentExecute : dependentTaskList) {
            for (Map.Entry<String, DependResult> entry : dependentExecute.getDependResultMap().entrySet()) {
                if (!dependResultMap.containsKey(entry.getKey())) {
                    dependResultMap.put(entry.getKey(), entry.getValue());
                    // save depend result to log
                    logger.info("dependent item complete, dependentKey: {}, result: {}, dependentDate: {}",
                            entry.getKey(), entry.getValue(), dependentDate);
                }
            }
            if (!dependentExecute.finish(dependentDate, testFlag)) {
                finish = false;
            }
        }
        return finish;
    }

    /**
     * get dependent result
     *
     * @return DependResult
     */
    private DependResult getTaskDependResult() {
        List<DependResult> dependResultList = new ArrayList<>();
        for (DependentExecute dependentExecute : dependentTaskList) {
            DependResult dependResult = dependentExecute.getModelDependResult(dependentDate, testFlag);
            dependResultList.add(dependResult);
        }
        result = DependentUtils.getDependResultForRelation(this.dependentParameters.getRelation(), dependResultList);
        logger.info("Dependent task completed, dependent result: {}", result);
        return result;
    }

    /**
     *
     */
    private void endTask() {
        TaskExecutionStatus status;
        status = (result == DependResult.SUCCESS) ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
    }

    @Override
    public String getType() {
        return TASK_TYPE_DEPENDENT;
    }
}
