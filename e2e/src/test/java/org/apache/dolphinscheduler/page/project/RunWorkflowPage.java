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
package org.apache.dolphinscheduler.page.project;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.data.project.RunWorkflowData;
import org.apache.dolphinscheduler.data.project.WorkflowDefineData;
import org.apache.dolphinscheduler.locator.project.RunWorkflowLocator;
import org.apache.dolphinscheduler.locator.project.WorkflowDefineLocator;
import org.openqa.selenium.WebDriver;

public class RunWorkflowPage extends PageCommon {
    RunWorkflowData runWorkflowData = new RunWorkflowData();
    WorkflowDefineData workflowDefineData = new WorkflowDefineData();

    public RunWorkflowPage(WebDriver driver) {
        super(driver);
    }

    public boolean runWorkflow() throws InterruptedException {
        // Determine whether the workflow status is online
        ifTextExists(WorkflowDefineLocator.WORKFLOW_STATE, runWorkflowData.getRunWorkflowData("online"));

        // click run workflow button
        System.out.println("Click run workflow button");
        clickButton(RunWorkflowLocator.CLICK_RUN_WORKFLOW_BUTTON);

        clickElement(RunWorkflowLocator.SELECT_FAILURE_STRATEGY_END);
        clickElement(RunWorkflowLocator.SELECT_FAILURE_STRATEGY_CONTINUE);
        clickElement(RunWorkflowLocator.CLICK_NOTICE_STRATEGY);
        clickElement(RunWorkflowLocator.SELECT_NOTICE_STRATEGY);
        clickElement(RunWorkflowLocator.CLICK_PROCESS_PRIORITY);
        clickElement(RunWorkflowLocator.SELECT_PROCESS_PRIORITY_HIGHEST);
        clickElement(RunWorkflowLocator.CLICK_WORKER_GROUP);
        clickElement(RunWorkflowLocator.SELECT_WORKER_GROUP);
        clickElement(RunWorkflowLocator.CLICK_NOTICE_GROUP);
        clickElement(RunWorkflowLocator.SELECT_NOTICE_GROUP);
        sendInput(RunWorkflowLocator.INPUT_RECIPIENT, runWorkflowData.getRunWorkflowData("recipient"));
        sendInput(RunWorkflowLocator.INPUT_Cc, runWorkflowData.getRunWorkflowData("Cc"));
        clickButton(RunWorkflowLocator.CLICK_RUNNING_BUTTON);

        return ifTitleContains(workflowDefineData.getWorkflowDefineData("workflowDefineTitle"));
    }
}
