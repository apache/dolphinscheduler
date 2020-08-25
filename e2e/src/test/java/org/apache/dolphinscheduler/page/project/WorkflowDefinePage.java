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
import org.apache.dolphinscheduler.data.project.ProjectData;
import org.apache.dolphinscheduler.data.project.WorkflowDefineData;
import org.apache.dolphinscheduler.locator.project.ProjectLocator;
import org.apache.dolphinscheduler.locator.project.WorkflowDefineLocator;
import org.openqa.selenium.WebDriver;

public class WorkflowDefinePage extends PageCommon {
    public WorkflowDefinePage(WebDriver driver) {
        super(driver);
    }

    /**
     * jump create workflow page
     */

    public boolean jumpWorkflowPage() throws InterruptedException {
        ifTextExists(ProjectLocator.LIST_PROJECT_NAME, ProjectData.PROJECT_NAME);

        // click project name
        clickElement(WorkflowDefineLocator.CLICK_PROJECT_NAME);

        ifTextExists(WorkflowDefineLocator.CLICK_WORKFLOW_DEFINE,WorkflowDefineData.workflow_define);

        System.out.println("Click on workflow define to jump to workflow define page");
        // click workflow define
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_DEFINE);

        return ifTitleContains(WorkflowDefineData.WORKFLOW_TITLE);
    }

    public boolean createWorkflow() throws InterruptedException {
        System.out.println("Click create workflow button");
        // click create workflow button
        clickElement(WorkflowDefineLocator.CLICK_CREATE_WORKFLOW_BUTTON);

        System.out.println("drag shell task");
        //drag shell_task
        dragAndDrop(WorkflowDefineLocator.MOUSE_DOWN_AT_SHELL, WorkflowDefineLocator.MOUSE_MOVE_SHELL_AT_DAG);

        //input shell task _name
        sendInput(WorkflowDefineLocator.INPUT_SHELL_TASK_NAME , WorkflowDefineData.SHELL_TASK_NAME);

        //click stop run type
        clickElement(WorkflowDefineLocator.CLICK_STOP_RUN_TYPE);

        //click normal run type
        clickElement(WorkflowDefineLocator.CLICK_NORMAL_RUN_TYPE);

        //input shell task description
        sendInput(WorkflowDefineLocator.INPUT_SHELL_TASK_DESCRIPTION , WorkflowDefineData.SHELL_TASK_DESCRIPTION);

        //select task priority
        clickElement(WorkflowDefineLocator.CLICK_TASK_PRIORITY);
        clickElement(WorkflowDefineLocator.SELECT_TASK_PRIORITY);

        //select work group
        clickElement(WorkflowDefineLocator.CLICK_WORK_GROUP);
        clickElement(WorkflowDefineLocator.SELECT_WORK_GROUP);

        //select number of failed retries
        clickElement(WorkflowDefineLocator.SELECT_FAIL_RETRIES_NUMBER);

        //select failed retry interval
        clickElement(WorkflowDefineLocator.SELECT_FAIL_RETRIES_INTERVAL);

        //click timeout alarm
        clickElement(WorkflowDefineLocator.CLICK_TIMEOUT_ALARM);

        //select timeout fail
        clickElement(WorkflowDefineLocator.SELECT_TIMEOUT_FAIL);

        //cancel timeout alarm
        clickElement(WorkflowDefineLocator.CANCEL_TIMEOUT_ALARM);

        //select timeout alarm
        clickElement(WorkflowDefineLocator.SELECT_TIMEOUT_ALARM);

        //clear timeout
        clearInput(WorkflowDefineLocator.SELECT_TIMEOUT);
        clearInput(WorkflowDefineLocator.SELECT_TIMEOUT);

        //input timeout
        sendInput(WorkflowDefineLocator.SELECT_TIMEOUT, WorkflowDefineData.INPUT_TIMEOUT);

        //click codeMirror and input script
        inputCodeMirror(WorkflowDefineLocator.CLICK_CODE_MIRROR, WorkflowDefineLocator.INPUT_SCRIPT, WorkflowDefineData.SHELL_SCRIPT);
        scrollToElementBottom(WorkflowDefineLocator.SCROLL_BOTTOM);

        //click custom parameters
        clickElement(WorkflowDefineLocator.CLICK_CUSTOM_PARAMETERS);

        //input custom parameters
        sendInput(WorkflowDefineLocator.INPUT_CUSTOM_PARAMETERS, WorkflowDefineData.INPUT_CUSTOM_PARAMETERS);

        //input custom parameters value
        sendInput(WorkflowDefineLocator.INPUT_CUSTOM_PARAMETERS_VALUE, WorkflowDefineData.INPUT_CUSTOM_PARAMETERS_VALUE);

        //click add custom parameters
        clickElement(WorkflowDefineLocator.CLICK_ADD_CUSTOM_PARAMETERS);

        scrollToElementBottom(WorkflowDefineLocator.SCROLL_BOTTOM);

        //input add custom parameters
        sendInput(WorkflowDefineLocator.INPUT_ADD_CUSTOM_PARAMETERS, WorkflowDefineData.INPUT_ADD_CUSTOM_PARAMETERS);

        //input add custom parameters value
        sendInput(WorkflowDefineLocator.INPUT_ADD_CUSTOM_PARAMETERS_VALUE, WorkflowDefineData.INPUT_ADD_CUSTOM_PARAMETERS_VALUE);

        //click delete custom parameters
        clickElement(WorkflowDefineLocator.CLICK_DELETE_CUSTOM_PARAMETERS);

        //click submit button
        clickElement(WorkflowDefineLocator.CLICK_SUBMIT_BUTTON);
        System.out.println("Task node set up successfully");
        System.out.println("move to Dag Element ");
        moveToDragElement(WorkflowDefineLocator.MOUSE_MOVE_SHELL_AT_DAG,-300,-100);

        System.out.println("copy task");
        mouseRightClickElement(WorkflowDefineLocator.MOUSE_RIGHT_CLICK);
        clickButton(WorkflowDefineLocator.COPY_TASK);
        clickButton(WorkflowDefineLocator.CLICK_LINE);
        mouseMovePosition(WorkflowDefineLocator.LINE_SOURCES_TASK,WorkflowDefineLocator.LINE_TARGET_TASK);
        return ifTitleContains(WorkflowDefineData.CREATE_WORKFLOW_TITLE);
    }

    /**
     * save  workflow
     */
    public boolean saveWorkflow() throws InterruptedException {
        System.out.println("start to save workflow ");

        //click save workflow button
        clickElement(WorkflowDefineLocator.CLICK_SAVE_WORKFLOW_BUTTON);

        //input  workflow name
        sendInput(WorkflowDefineLocator.INPUT_WORKFLOW_NAME, WorkflowDefineData.INPUT_WORKFLOW_NAME);

        //input  workflow description
        sendInput(WorkflowDefineLocator.INPUT_WORKFLOW_DESCRIPTION, WorkflowDefineData.INPUT_WORKFLOW_DESCRIPTION);

        //select tenant
        clickElement(WorkflowDefineLocator.CLICK_TENANT);
        clickElement(WorkflowDefineLocator.SELECT_TENANT);

        //click workflow timeout alarm
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_TIMEOUT_ALARM);
        clearInput(WorkflowDefineLocator.INPUT_WORKFLOW_TIMEOUT);

        //input workflow timeout
        sendInput(WorkflowDefineLocator.INPUT_WORKFLOW_TIMEOUT, WorkflowDefineData.INPUT_WORKFLOW_TIMEOUT);

        //click workflow  global parameters
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_GLOBAL_PARAMETERS);

        //input workflow  global parameters
        sendInput(WorkflowDefineLocator.INPUT_WORKFLOW_GLOBAL_PARAMETERS, WorkflowDefineData.INPUT_WORKFLOW_GLOBAL_PARAMETERS);

        //input workflow  global parameters value
        sendInput(WorkflowDefineLocator.INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES, WorkflowDefineData.INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES);

        //click to add workflow  global parameters
        clickElement(WorkflowDefineLocator.CLICK_ADD_WORKFLOW_GLOBAL_PARAMETERS);

        //input to  add workflow  global parameters
        sendInput(WorkflowDefineLocator.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS, WorkflowDefineData.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS);

        //input to add workflow  global parameters value
        sendInput(WorkflowDefineLocator.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES, WorkflowDefineData.INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES);

        //delete workflow  global parameters value
        clickElement(WorkflowDefineLocator.CLICK_DELETE_WORKFLOW_GLOBAL_PARAMETERS);

        //click add button
        System.out.println("submit workflow");
        clickButton(WorkflowDefineLocator.CLICK_ADD_BUTTON);

        return ifTitleContains(WorkflowDefineData.CREATE_WORKFLOW_TITLE);
    }

    public boolean onlineWorkflow() throws InterruptedException {
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_DEFINE);

        // Determine whether the workflow status is offline
        ifTextExists(WorkflowDefineLocator.WORKFLOW_STATE,WorkflowDefineData.WORKFLOW_OFFLINE_STATE);

        // click online button
        System.out.println("Click online workflow button");
        clickButton(WorkflowDefineLocator.CLICK_ONLINE_WORKFLOW_BUTTON);

        return ifTitleContains(WorkflowDefineData.WORKFLOW_TITLE);
    }

    public boolean offlineWorkflow() throws InterruptedException {
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_DEFINE);

        // Determine whether the workflow status is online
        ifTextExists(WorkflowDefineLocator.WORKFLOW_STATE,WorkflowDefineData.WORKFLOW_ONLINE_STATE);

        // click offline button
        System.out.println("offline workflow");
        clickButton(WorkflowDefineLocator.CLICK_OFFLINE_WORKFLOW_BUTTON);

        return ifTitleContains(WorkflowDefineData.WORKFLOW_TITLE);
    }


    public boolean deleteWorkflow() throws InterruptedException {
        //click  delete workflow
        clickElement(WorkflowDefineLocator.CLICK_WORKFLOW_DEFINE);

        // Determine whether the workflow status is offline
        ifTextExists(WorkflowDefineLocator.WORKFLOW_STATE,WorkflowDefineData.WORKFLOW_OFFLINE_STATE);

        clickButton(WorkflowDefineLocator.DELETE_WORKFLOW_BOTTOM);

        //click confirm delete project
        clickButton(WorkflowDefineLocator.CONFIRM_DELETE_WORKFLOW_BOTTOM);

        // Whether to enter the specified page after submit
        return ifTitleContains(WorkflowDefineData.WORKFLOW_TITLE);
    }
}
