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
import org.apache.dolphinscheduler.locator.project.RunWorkflowLocator;
import org.apache.dolphinscheduler.locator.project.TimingLocator;
import org.openqa.selenium.WebDriver;

public class TimingPage extends PageCommon {
    public TimingPage(WebDriver driver) {
        super(driver);
    }


    /**
     * create timing
     */
    public boolean createTiming() throws InterruptedException {
        // click timing button
        System.out.println("Click timing button");
        Thread.sleep(1000);
        clickButton(TimingLocator.CLICK_TIMING_BUTTON);
        System.out.println("Click execution timing button");
        clickButton(TimingLocator.CLICK_EXECUTION_TIMING_BUTTON);
        Thread.sleep(1000);

        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_END);
        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_CONTINUE);
        clickElement(TimingLocator.CLICK_NOTICE_STRATEGY);
        clickElement(TimingLocator.SELECT_NOTICE_STRATEGY);
        Thread.sleep(500);
        clickElement(TimingLocator.CLICK_PROCESS_PRIORITY);
        clickElement(TimingLocator.SELECT_PROCESS_PRIORITY);
        clickElement(TimingLocator.CLICK_WORKER_GROUP);
        clickElement(TimingLocator.SELECT_WORKER_GROUP);
        clickElement(TimingLocator.CLICK_NOTICE_GROUP);
        clickElement(TimingLocator.SELECT_NOTICE_GROUP);
        sendInput(TimingLocator.INPUT_RECIPIENT, TimingData.RECIPIENT);
        sendInput(TimingLocator.INPUT_Cc,TimingData.Cc);
        clickButton(TimingLocator.CLICK_CREATE_BUTTON);

        return ifTitleContains(TimingData.WORKFLOW_TITLE);
    }

    /**
     * edit timing
     */
    public boolean editTiming() throws InterruptedException {
        // click timing button
        System.out.println("Click timing  management button");
        Thread.sleep(1000);
        clickButton(TimingLocator.CLICK_TIMING_MANAGEMENT_BUTTON);
        Thread.sleep(1000);
        System.out.println("Click edit timing button");
        clickButton(TimingLocator.CLICK_EDIT_TIMING_BUTTON);
        System.out.println("Click execution timing button");
        clickButton(TimingLocator.CLICK_EXECUTION_TIMING_BUTTON);
        Thread.sleep(1000);

        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_END);
        clickElement(TimingLocator.SELECT_FAILURE_STRATEGY_CONTINUE);
        clickElement(TimingLocator.CLICK_NOTICE_STRATEGY);
        clickElement(TimingLocator.SELECT_NOTICE_STRATEGY);
        Thread.sleep(500);
        clickElement(TimingLocator.CLICK_PROCESS_PRIORITY);
        clickElement(TimingLocator.SELECT_PROCESS_PRIORITY);
        clickElement(TimingLocator.CLICK_WORKER_GROUP);
        clickElement(TimingLocator.SELECT_WORKER_GROUP);
        clickElement(TimingLocator.CLICK_NOTICE_GROUP);
        clickElement(TimingLocator.SELECT_NOTICE_GROUP);
        sendInput(TimingLocator.INPUT_RECIPIENT, TimingData.EDIT_RECIPIENT);
        sendInput(TimingLocator.INPUT_Cc,TimingData.EDIT_Cc);
        clickButton(TimingLocator.CLICK_CREATE_BUTTON);

        return ifTitleContains(TimingData.TIMING_TITLE );
    }


    /**
     * online timing
     */
    public boolean onlineTiming() throws InterruptedException {
        // click online timing button
        System.out.println("Click online timing  button");
        Thread.sleep(500);
        clickButton(TimingLocator.CLICK_ONLINE_TIMING_BUTTON);

        return ifTitleContains(TimingData.TIMING_TITLE );
    }


    /**
     * offline timing
     */
    public boolean offlineTiming() throws InterruptedException {
        // click offline timing button
        System.out.println("Click offline timing  button");
        Thread.sleep(500);
        clickButton(TimingLocator.CLICK_OFFLINE_TIMING_BUTTON);

        return ifTitleContains(TimingData.TIMING_TITLE );
    }



    /**
     * delete timing
     */
    public boolean deleteTiming() throws InterruptedException {
        // click offline timing button
        System.out.println("Click delete timing  button");
        Thread.sleep(500);
        clickButton(TimingLocator.CLICK_DELETE_TIMING_BUTTON);
        clickButton(TimingLocator.CLICK_CONFIRM_DELETE_TIMING_BUTTON);

        return ifTitleContains(TimingData.WORKFLOW_TITLE );
    }
}
