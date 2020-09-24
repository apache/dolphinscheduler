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
import org.apache.dolphinscheduler.data.project.TimingData;
import org.apache.dolphinscheduler.locator.project.TimingLocator;
import org.apache.dolphinscheduler.locator.project.WorkflowDefineLocator;
import org.openqa.selenium.WebDriver;

public class TimingPage extends PageCommon {
    TimingData timingData = new TimingData();

    public TimingPage(WebDriver driver) {
        super(driver);
    }

    /**
     * create timing
     */
    public boolean createTiming() throws InterruptedException {
        flushPage();
        // Determine whether the workflow status is online
        ifTextExists(WorkflowDefineLocator.WORKFLOW_STATE, timingData.getTimingData("online"));

        // click timing button
        System.out.println("Click timing button");
        clickButton(TimingLocator.CLICK_TIMING_BUTTON);
        System.out.println("Click execution timing button");
        clickButton(TimingLocator.CLICK_EXECUTION_TIMING_BUTTON);

        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_END);
        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_CONTINUE);
        clickElement(TimingLocator.CLICK_NOTICE_STRATEGY);
        clickElement(TimingLocator.SELECT_NOTICE_STRATEGY);
        clickElement(TimingLocator.CLICK_PROCESS_PRIORITY);
        clickElement(TimingLocator.SELECT_PROCESS_PRIORITY);
        clickElement(TimingLocator.CLICK_WORKER_GROUP);
        clickElement(TimingLocator.SELECT_WORKER_GROUP);
        clickElement(TimingLocator.CLICK_NOTICE_GROUP);
        clickElement(TimingLocator.SELECT_NOTICE_GROUP);
        sendInput(TimingLocator.INPUT_RECIPIENT, timingData.getTimingData("recipient"));
        sendInput(TimingLocator.INPUT_Cc, timingData.getTimingData("Cc"));
        clickButton(TimingLocator.CLICK_CREATE_BUTTON);

        return ifTextExists(TimingLocator.TIMING_STATE, timingData.getTimingData("offline"));
    }

    /**
     * edit timing
     */
    public boolean editTiming() throws InterruptedException {
        // click timing button
        System.out.println("Click timing  management button");
        clickButton(TimingLocator.CLICK_TIMING_MANAGEMENT_BUTTON);

        // Determine whether the workflow name exists
        ifTextExists(TimingLocator.WORKFLOW_NAME, timingData.getTimingData("offline"));

        System.out.println("Click edit timing button");
        clickButton(TimingLocator.CLICK_EDIT_TIMING_BUTTON);
        System.out.println("Click execution timing button");
        clickButton(TimingLocator.CLICK_EXECUTION_TIMING_BUTTON);

        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_END);
        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_CONTINUE);
        clickElement(TimingLocator.CLICK_NOTICE_STRATEGY);
        clickElement(TimingLocator.SELECT_NOTICE_STRATEGY);
        clickElement(TimingLocator.CLICK_PROCESS_PRIORITY);
        clickElement(TimingLocator.SELECT_PROCESS_PRIORITY);
        clickElement(TimingLocator.CLICK_WORKER_GROUP);
        clickElement(TimingLocator.SELECT_WORKER_GROUP);
        clickElement(TimingLocator.CLICK_NOTICE_GROUP);
        clickElement(TimingLocator.SELECT_NOTICE_GROUP);
        sendInput(TimingLocator.INPUT_RECIPIENT, timingData.getTimingData("editRecipient"));
        sendInput(TimingLocator.INPUT_Cc, timingData.getTimingData("editCc"));
        clickButton(TimingLocator.CLICK_CREATE_BUTTON);

        return ifTitleContains(timingData.getTimingData("timingTitle"));
    }


    /**
     * online timing
     */
    public boolean onlineTiming() throws InterruptedException {
        flushPage();
        // Determine whether the timing is offline
        ifTextExists(TimingLocator.TIMING_MANAGEMENT_TIMING_STATE, timingData.getTimingData("offline"));

        // click online timing button
        System.out.println("Click online timing  button");
        clickElement(TimingLocator.CLICK_ONLINE_TIMING_BUTTON);

        return ifTextExists(TimingLocator.TIMING_MANAGEMENT_TIMING_STATE, timingData.getTimingData("online"));
    }


    /**
     * offline timing
     */
    public boolean offlineTiming() throws InterruptedException {
        flushPage();
        // Determine whether the timing is online
        ifTextExists(TimingLocator.TIMING_MANAGEMENT_TIMING_STATE, timingData.getTimingData("online"));

        // click offline timing button
        System.out.println("Click offline timing  button");
        clickElement(TimingLocator.CLICK_OFFLINE_TIMING_BUTTON);

        return ifTextExists(TimingLocator.TIMING_MANAGEMENT_TIMING_STATE, timingData.getTimingData("offline"));
    }



    /**
     * delete timing
     */
    public boolean deleteTiming() throws InterruptedException {
        // Determine whether the timing is offline
        ifTextExists(TimingLocator.TIMING_MANAGEMENT_TIMING_STATE, timingData.getTimingData("offline"));

        // click offline timing button
        System.out.println("Click delete timing  button");
        clickButton(TimingLocator.CLICK_DELETE_TIMING_BUTTON);
        clickButton(TimingLocator.CLICK_CONFIRM_DELETE_TIMING_BUTTON);

        return ifTextExists(TimingLocator.TIMING_STATE, "-");
    }
}
