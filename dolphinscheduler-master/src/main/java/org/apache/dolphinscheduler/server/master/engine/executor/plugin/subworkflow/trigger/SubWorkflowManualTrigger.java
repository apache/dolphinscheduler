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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow.trigger;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowManualTrigger;

import org.springframework.stereotype.Component;

/**
 * Manual trigger of the workflow, used to trigger the workflow and generate the workflow instance in the manual way.
 */
@Component
public class SubWorkflowManualTrigger extends WorkflowManualTrigger {

    @Override
    protected WorkflowInstance constructWorkflowInstance(final WorkflowManualTriggerRequest workflowManualTriggerRequest) {
        final WorkflowInstance workflowInstance = super.constructWorkflowInstance(workflowManualTriggerRequest);
        workflowInstance.setIsSubWorkflow(Flag.YES);
        return workflowInstance;
    }

}
