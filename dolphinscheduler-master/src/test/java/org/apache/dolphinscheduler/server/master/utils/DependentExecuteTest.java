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

package org.apache.dolphinscheduler.server.master.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceSummaryDto;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class DependentExecuteTest {

    @ParameterizedTest
    @CsvSource({"0, WAITING", "101, WAITING", "-1, SUCCESS", "102, SUCCESS"})
    void testFirstRunWithPreviousDayDependencies(long depTaskCode, DependResult expectedResult) {
        WorkflowInstanceDao workflowInstanceDao = mock(WorkflowInstanceDao.class);
        WorkflowInstance workflow = new WorkflowInstance();
        workflow.setId(1);
        workflow.setWorkflowDefinitionCode(11L);
        Date scheduleTime = DateUtils.stringToDate("2026-01-15 12:00:00");
        workflow.setScheduleTime(scheduleTime);
        TaskInstance task = new TaskInstance();
        task.setTaskCode(102L);
        DependentItem item = new DependentItem();
        item.setDefinitionCode(11L);
        item.setDepTaskCode(depTaskCode);
        item.setCycle("day");
        item.setDateValue("last1Days");
        WorkflowInstanceSummaryDto firstInstance = new WorkflowInstanceSummaryDto();
        firstInstance.setId(workflow.getId());
        when(workflowInstanceDao.queryFirstScheduleWorkflowInstance(11L)).thenReturn(firstInstance);

        try (MockedStatic<SpringApplicationContext> context = mockStatic(SpringApplicationContext.class)) {
            context.when(() -> SpringApplicationContext.getBean(WorkflowInstanceDao.class))
                    .thenReturn(workflowInstanceDao);
            DependentExecute execute = new DependentExecute(Collections.singletonList(item), DependentRelation.AND,
                    workflow, task);

            assertThat(execute.getModelDependResult(scheduleTime)).isEqualTo(expectedResult);
            if (expectedResult == DependResult.WAITING) {
                ArgumentCaptor<DateInterval> intervalCaptor = ArgumentCaptor.forClass(DateInterval.class);
                verify(workflowInstanceDao).queryLastRunningWorkflowInterval(eq(11L), intervalCaptor.capture());
                DateInterval interval = intervalCaptor.getValue();
                assertThat(interval.getStartTime()).isEqualTo(DateUtils.stringToDate("2026-01-14 00:00:00"));
                assertThat(interval.getEndTime()).isBefore(DateUtils.stringToDate("2026-01-15 00:00:00"));
                verify(workflowInstanceDao).queryLastSchedulerWorkflowInterval(11L, depTaskCode, interval);
                verify(workflowInstanceDao).queryLastManualWorkflowInterval(11L, depTaskCode, interval);
            } else {
                verify(workflowInstanceDao, never()).queryLastRunningWorkflowInterval(any(), any());
            }
        }
    }
}
