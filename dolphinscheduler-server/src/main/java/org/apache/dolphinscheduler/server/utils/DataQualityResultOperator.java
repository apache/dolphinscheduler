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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.dq.CheckType;
import org.apache.dolphinscheduler.common.enums.dq.DqFailureStrategy;
import org.apache.dolphinscheduler.common.enums.dq.DqTaskState;
import org.apache.dolphinscheduler.common.enums.dq.OperatorType;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseEvent;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DataQualityResultOperator
 */
@Component
public class DataQualityResultOperator {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(DataQualityResultOperator.class);

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * alert manager
     */
    private final AlertManager alertManager = new AlertManager();

    public void operateDqExecuteResult(TaskResponseEvent taskResponseEvent, TaskInstance taskInstance) {
        if (TaskType.DATA_QUALITY == TaskType.valueOf(taskInstance.getTaskType())) {

            ProcessInstance processInstance =
                    processService.findProcessInstanceDetailById(
                            Integer.parseInt(String.valueOf(taskInstance.getProcessInstanceId())));

            if (taskResponseEvent.getState().typeIsFailure()
                    || taskResponseEvent.getState().typeIsCancel()) {
                processService.deleteDqExecuteResultByTaskInstanceId(taskInstance.getId());
                processService.deleteTaskStatisticsValueByTaskInstanceId(taskInstance.getId());
                sendDqTaskErrorAlert(taskInstance,processInstance);
                return;
            }

            processService.updateDqExecuteResultUserId(taskInstance.getId());
            DqExecuteResult dqExecuteResult =
                    processService.getDqExecuteResultByTaskInstanceId(taskInstance.getId());
            if (dqExecuteResult != null) {
                //check the result ,if result is failure do some operator by failure strategy
                checkDqExecuteResult(taskResponseEvent, dqExecuteResult, processInstance);
            }
        }
    }

    private void checkDqExecuteResult(TaskResponseEvent taskResponseEvent,
                                      DqExecuteResult dqExecuteResult,
                                      ProcessInstance processInstance) {
        if (isFailure(dqExecuteResult)) {
            DqFailureStrategy dqFailureStrategy = DqFailureStrategy.of(dqExecuteResult.getFailureStrategy());
            if (dqFailureStrategy != null) {
                dqExecuteResult.setState(DqTaskState.FAILURE);
                sendDqTaskResultAlert(dqExecuteResult,processInstance);
                switch (dqFailureStrategy) {
                    case ALERT:
                        logger.info("task is failure, continue and alert");
                        break;
                    case BLOCK:
                        taskResponseEvent.setState(ExecutionStatus.FAILURE);
                        logger.info("task is failure, end and alert");
                        break;
                    default:
                        break;
                }
            }
        } else {
            dqExecuteResult.setState(DqTaskState.SUCCESS);
        }

        processService.updateDqExecuteResultState(dqExecuteResult);
    }

    private boolean isFailure(DqExecuteResult dqExecuteResult) {
        CheckType checkType = dqExecuteResult.getCheckType();

        double statisticsValue = dqExecuteResult.getStatisticsValue();
        double comparisonValue = dqExecuteResult.getComparisonValue();
        double threshold = dqExecuteResult.getThreshold();

        OperatorType operatorType = OperatorType.of(dqExecuteResult.getOperator());

        boolean isFailure = false;
        if (operatorType != null) {
            double srcValue = 0;
            switch (checkType) {
                case COMPARISON_MINUS_STATISTICS:
                    srcValue = comparisonValue - statisticsValue;
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_MINUS_COMPARISON:
                    srcValue = statisticsValue - comparisonValue;
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_COMPARISON_PERCENTAGE:
                    if (comparisonValue > 0) {
                        srcValue = statisticsValue / comparisonValue * 100;
                    }
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE:
                    if (comparisonValue > 0) {
                        srcValue = Math.abs(comparisonValue - statisticsValue) / comparisonValue * 100;
                    }
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                default:
                    break;
            }
        }

        return isFailure;
    }

    private void sendDqTaskResultAlert(DqExecuteResult dqExecuteResult, ProcessInstance processInstance) {
        alertManager.sendDataQualityTaskExecuteResultAlert(dqExecuteResult,processInstance);
    }

    private void sendDqTaskErrorAlert(TaskInstance taskInstance, ProcessInstance processInstance) {
        alertManager.sendTaskErrorAlert(taskInstance,processInstance);
    }

    private boolean getCompareResult(OperatorType operatorType, double srcValue, double targetValue) {
        BigDecimal src = BigDecimal.valueOf(srcValue);
        BigDecimal target = BigDecimal.valueOf(targetValue);
        switch (operatorType) {
            case EQ:
                return src.compareTo(target) == 0;
            case LT:
                return src.compareTo(target) <= -1;
            case LE:
                return src.compareTo(target) == 0 || src.compareTo(target) <= -1;
            case GT:
                return src.compareTo(target) >= 1;
            case GE:
                return src.compareTo(target) == 0 || src.compareTo(target) >= 1;
            case NE:
                return src.compareTo(target) != 0;
            default:
                return true;
        }
    }
}
