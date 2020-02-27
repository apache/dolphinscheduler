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
import org.apache.dolphinscheduler.constant.TestConstant;
import org.apache.dolphinscheduler.data.project.CreatWorkflowData;
import org.apache.dolphinscheduler.locator.project.CreateWorkflowLocator;
import org.openqa.selenium.WebDriver;

public class CreateWorkflowPage extends PageCommon {
    public CreateWorkflowPage(WebDriver driver) {
        super(driver);
    }
    /**
     * jump page
     */

    public boolean createWorkflow() throws InterruptedException {
        // click project name
        clickElement(CreateWorkflowLocator.CLICK_PROJECT_NAME);

        // click workflow define
        clickElement(CreateWorkflowLocator.CLICK_WORKFLOW_DEFINE);
        Thread.sleep(TestConstant.ONE_THOUSANG);

        // click create workflow button
        clickElement(CreateWorkflowLocator.CLICK_CREATE_WORKFLOW_BUTTON);
        Thread.sleep(TestConstant.ONE_THOUSANG);

        //drag shell_task
        dragAndDrop(CreateWorkflowLocator.MOUSE_DOWN_AT_SHELL,CreateWorkflowLocator.MOUSE_MOVE_SHELL_AT_DAG);

        //input shell task _name
        sendInput(CreateWorkflowLocator.INPUT_SHELL_TASK_NAME , CreatWorkflowData.SHELL_TASK_NAME);

        //click stop run type
        clickElement(CreateWorkflowLocator.CLICK_STOP_RUN_TYPE);

        //click normal run type
        clickElement(CreateWorkflowLocator.CLICK_NORMAL_RUN_TYPE);

        //input shell task description
        sendInput(CreateWorkflowLocator.INPUT_SHELL_TASK_DESCRIPTION , CreatWorkflowData.SHELL_TASK_DESCRIPTION);

        //select task priority
        clickElement(CreateWorkflowLocator.CLICK_TASK_PRIORITY);
        clickElement(CreateWorkflowLocator.SELECT_TASK_PRIORITY);

        //select work group
        clickElement(CreateWorkflowLocator.CLICK_WORK_GROUP);
        clickElement(CreateWorkflowLocator.SELECT_WORK_GROUP);

        //select number of failed retries
        clickElement(CreateWorkflowLocator.SELECT_FAIL_RETRIES_NUMBER);

        //select failed retry interval
        clickElement(CreateWorkflowLocator.SELECT_FAIL_RETRIES_INTERVAL);


        //click timeout alarm
        clickElement(CreateWorkflowLocator.CLICK_TIMEOUT_ALARM);


        //select timeout fail
        clickElement(CreateWorkflowLocator.SELECT_TIMEOUT_FAIL);


        //cancel timeout alarm
        clickElement(CreateWorkflowLocator.CANCEL_TIMEOUT_ALARM);


        //select timeout alarm
        clickElement(CreateWorkflowLocator.SELECT_TIMEOUT_ALARM);

        //clear timeout
        clearInput(CreateWorkflowLocator.SELECT_TIMEOUT);
        clearInput(CreateWorkflowLocator.SELECT_TIMEOUT);

        //input timeout
        sendInput(CreateWorkflowLocator.SELECT_TIMEOUT,CreatWorkflowData.INPUT_TIMEOUT);

        //click codeMirror and input script
        inputCodeMirror(CreateWorkflowLocator.CLICK_CODE_MIRROR, CreateWorkflowLocator.INPUT_SCRIPT,CreatWorkflowData.SHELL_SCRIPT);
        scrollToElementBottom();
        Thread.sleep(TestConstant.ONE_THOUSANG);

        //click custom parameters
        clickElement(CreateWorkflowLocator.CLICK_CUSTOM_PARAMETERS);

        //input custom parameters
        sendInput(CreateWorkflowLocator.INPUT_CUSTOM_PARAMETERS, CreatWorkflowData.INPUT_CUSTOM_PARAMETERS);

        //input custom parameters value
        sendInput(CreateWorkflowLocator.INPUT_CUSTOM_PARAMETERS_VALUE, CreatWorkflowData.INPUT_CUSTOM_PARAMETERS_VALUE);

        //click add custom parameters
        clickElement(CreateWorkflowLocator.CLICK_ADD_CUSTOM_PARAMETERS);

        scrollToElementBottom();
        Thread.sleep(TestConstant.ONE_THOUSANG);

        //input add custom parameters
        sendInput(CreateWorkflowLocator.INPUT_ADD_CUSTOM_PARAMETERS,CreatWorkflowData.INPUT_ADD_CUSTOM_PARAMETERS);

        //input add custom parameters value
        sendInput(CreateWorkflowLocator.INPUT_ADD_CUSTOM_PARAMETERS_VALUE,CreatWorkflowData.INPUT_ADD_CUSTOM_PARAMETERS_VALUE);

        //click delete custom parameters
        clickElement(CreateWorkflowLocator.CLICK_DELETE_CUSTOM_PARAMETERS);
        Thread.sleep(TestConstant.ONE_THOUSANG);

        //click submit button
        clickElement(CreateWorkflowLocator.CLICK_SUBMIT_BUTTON);
        Thread.sleep(TestConstant.ONE_THOUSANG);

        moveToDragElement(CreateWorkflowLocator.MOUSE_MOVE_SHELL_AT_DAG,-300,-100);

        return ifTitleContains(CreatWorkflowData.WORKFLOW_TITLE);
    }
}
