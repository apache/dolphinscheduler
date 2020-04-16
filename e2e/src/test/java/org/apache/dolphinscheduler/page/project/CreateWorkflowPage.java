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
import org.apache.dolphinscheduler.data.project.CreateWorkflowData;
import org.apache.dolphinscheduler.locator.project.CreateWorkflowLocator;
import org.openqa.selenium.WebDriver;

public class CreateWorkflowPage extends PageCommon {
    public CreateWorkflowPage(WebDriver driver) {
        super(driver);
    }

    /**
     * jump create workflow page
     */

    public boolean jumpWorkflowPage() throws InterruptedException {
        // click project name
        clickElement(CreateWorkflowLocator.CLICK_PROJECT_NAME);
        Thread.sleep(TestConstant.ONE_THOUSAND);

        System.out.println("Click on workflow define to jump to workflow define page");
        // click workflow define
        clickElement(CreateWorkflowLocator.CLICK_WORKFLOW_DEFINE);

        return ifTitleContains(CreateWorkflowData.WORKFLOW_TITLE);
    }

    public boolean createWorkflow() throws InterruptedException {
        System.out.println("Click create workflow button");
        // click create workflow button
        clickElement(CreateWorkflowLocator.CLICK_CREATE_WORKFLOW_BUTTON);

        System.out.println("drag shell task");
        //drag shell_task
        dragAndDrop(CreateWorkflowLocator.MOUSE_DOWN_AT_SHELL,CreateWorkflowLocator.MOUSE_MOVE_SHELL_AT_DAG);

        //input shell task _name
        sendInput(CreateWorkflowLocator.INPUT_SHELL_TASK_NAME , CreateWorkflowData.SHELL_TASK_NAME);

        //click stop run type
        clickElement(CreateWorkflowLocator.CLICK_STOP_RUN_TYPE);

        //click normal run type
        clickElement(CreateWorkflowLocator.CLICK_NORMAL_RUN_TYPE);

        //input shell task description
        sendInput(CreateWorkflowLocator.INPUT_SHELL_TASK_DESCRIPTION , CreateWorkflowData.SHELL_TASK_DESCRIPTION);

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
        sendInput(CreateWorkflowLocator.SELECT_TIMEOUT, CreateWorkflowData.INPUT_TIMEOUT);

        //click codeMirror and input script
        inputCodeMirror(CreateWorkflowLocator.CLICK_CODE_MIRROR, CreateWorkflowLocator.INPUT_SCRIPT, CreateWorkflowData.SHELL_SCRIPT);
        scrollToElementBottom(CreateWorkflowLocator.SCROLL_BOTTOM);

        //click custom parameters
        clickElement(CreateWorkflowLocator.CLICK_CUSTOM_PARAMETERS);

        //input custom parameters
        sendInput(CreateWorkflowLocator.INPUT_CUSTOM_PARAMETERS, CreateWorkflowData.INPUT_CUSTOM_PARAMETERS);

        //input custom parameters value
        sendInput(CreateWorkflowLocator.INPUT_CUSTOM_PARAMETERS_VALUE, CreateWorkflowData.INPUT_CUSTOM_PARAMETERS_VALUE);

        //click add custom parameters
        clickElement(CreateWorkflowLocator.CLICK_ADD_CUSTOM_PARAMETERS);

        scrollToElementBottom(CreateWorkflowLocator.SCROLL_BOTTOM);

        //input add custom parameters
        sendInput(CreateWorkflowLocator.INPUT_ADD_CUSTOM_PARAMETERS, CreateWorkflowData.INPUT_ADD_CUSTOM_PARAMETERS);

        //input add custom parameters value
        sendInput(CreateWorkflowLocator.INPUT_ADD_CUSTOM_PARAMETERS_VALUE, CreateWorkflowData.INPUT_ADD_CUSTOM_PARAMETERS_VALUE);

        //click delete custom parameters
        clickElement(CreateWorkflowLocator.CLICK_DELETE_CUSTOM_PARAMETERS);

        //click submit button
        clickElement(CreateWorkflowLocator.CLICK_SUBMIT_BUTTON);
        Thread.sleep(TestConstant.ONE_THOUSAND);
        System.out.println("Task node set up successfully");
        System.out.println("move to Dag Element ");
        moveToDragElement(CreateWorkflowLocator.MOUSE_MOVE_SHELL_AT_DAG,-300,-100);

        return ifTitleContains(CreateWorkflowData.CREATE_WORKFLOW_TITLE);
    }

    /**
     * save  workflow
     */
    public boolean saveWorkflow() throws InterruptedException {
        System.out.println("start to save workflow ");

        //click save workflow button
        clickElement(CreateWorkflowLocator.CLICK_SAVE_WORKFLOW_BUTTON);

        //input  workflow name
        sendInput(CreateWorkflowLocator.INPUT_WORKFLOW_NAME, CreateWorkflowData.INPUT_WORKFLOW_NAME);

        //input  workflow description
        sendInput(CreateWorkflowLocator.INPUT_WORKFLOW_DESCRIPTION, CreateWorkflowData.INPUT_WORKFLOW_DESCRIPTION);

        //select tenant
        clickElement(CreateWorkflowLocator.CLICK_TENANT);
        clickElement(CreateWorkflowLocator.SELECT_TENANT);

        //click workflow timeout alarm
        clickElement(CreateWorkflowLocator.CLICK_WORKFLOW_TIMEOUT_ALARM);
        clearInput(CreateWorkflowLocator.INPUT_WORKFLOW_TIMEOUT);

        //input workflow timeout
        sendInput(CreateWorkflowLocator.INPUT_WORKFLOW_TIMEOUT, CreateWorkflowData.INPUT_WORKFLOW_TIMEOUT);

        //click workflow  global parameters
        clickElement(CreateWorkflowLocator.CLICK_WORKFLOW_GLOBAL_PARAMETERS);

        //input workflow  global parameters
        sendInput(CreateWorkflowLocator.INPUT_WORKFLOW_GLOBAL_PARAMETERS, CreateWorkflowData.INPUT_WORKFLOW_GLOBAL_PARAMETERS);

        //input workflow  global parameters value
        sendInput(CreateWorkflowLocator.INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES, CreateWorkflowData.INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES);

        //click to add workflow  global parameters
        clickElement(CreateWorkflowLocator.CLICK_ADD_WORKFLOW_GLOBAL_PARAMETERS);

        //input to  add workflow  global parameters
        sendInput(CreateWorkflowLocator.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS, CreateWorkflowData.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS);

        //input to add workflow  global parameters value
        sendInput(CreateWorkflowLocator.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES, CreateWorkflowData.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES);

        //delete workflow  global parameters value
        clickElement(CreateWorkflowLocator.CLICK_DELETE_WORKFLOW_GLOBAL_PARAMETERS);
        Thread.sleep(TestConstant.ONE_THOUSAND);

        //click add button
        clickButton(CreateWorkflowLocator.CLICK_ADD_BUTTON);
        System.out.println("submit workflow");
        return ifTitleContains(CreateWorkflowData.CREATE_WORKFLOW_TITLE);
    }

    public boolean deleteWorkflow() throws InterruptedException {
        //click  delete project
        clickButton(CreateWorkflowLocator.DELETE_WORKFLOW_BOTTOM);

        //click confirm delete project
        clickButton(CreateWorkflowLocator.CONFIRM_DELETE_WORKFLOW_BOTTOM);

        // Whether to enter the specified page after submit
        return ifTitleContains(CreateWorkflowData.WORKFLOW_TITLE);
    }
}
