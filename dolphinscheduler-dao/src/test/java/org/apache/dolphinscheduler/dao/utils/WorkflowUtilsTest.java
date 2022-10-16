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

package org.apache.dolphinscheduler.dao.utils;

import java.util.Date;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkflowUtilsTest {

    @Test
    public void testGetWorkflowInstanceDuration() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setState(null);
        Date start = DateUtils.stringToDate("2020-01-20 11:00:00");
        Date end = DateUtils.stringToDate("2020-01-21 12:10:10");
        processInstance.setStartTime(start);
        processInstance.setEndTime(end);

        String noStateDuration = WorkflowUtils.getWorkflowInstanceDuration(processInstance);
        System.currentTimeMillis();
        Assertions.assertNotEquals("1d 1h 10m 10s", noStateDuration);

        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        String notFinishDuration = WorkflowUtils.getWorkflowInstanceDuration(processInstance);
        Assertions.assertNotEquals("1d 1h 10m 10s", notFinishDuration);

        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        String successDuration = WorkflowUtils.getWorkflowInstanceDuration(processInstance);
        Assertions.assertEquals("1d 1h 10m 10s", successDuration);
    }
}