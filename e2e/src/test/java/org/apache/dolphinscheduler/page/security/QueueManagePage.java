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
package org.apache.dolphinscheduler.page.security;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.data.security.QueueManageData;
import org.apache.dolphinscheduler.locator.security.QueueManageLocator;
import org.openqa.selenium.WebDriver;

public class QueueManagePage extends PageCommon {
    /**
     * Unique constructor
     * @param driver driver
     */
    public QueueManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * create queue
     *
     * @return Whether to enter the specified page after create queue
     */
    public boolean createQueue() throws InterruptedException {
        // click queue manage
        clickElement(QueueManageLocator.CLICK_QUEUE_MANAGE);
        Thread.sleep(1000);

        // click  create queue button
        clickElement(QueueManageLocator.CLICK_CREATE_QUEUE);
        Thread.sleep(1000);

        // input queue data
        sendInput(QueueManageLocator.INPUT_QUEUE_NAME, QueueManageData.QUEUE_NAME);

        sendInput(QueueManageLocator.INPUT_QUEUE_VALUE, QueueManageData.QUEUE_VALUE);

        // click  button
        clickButton(QueueManageLocator.SUBMIT_QUEUE);

        // Whether to enter the specified page after submit
        return ifTitleContains(QueueManageData.QUEUE_MANAGE);
    }


    /**
     * edit queue
     *
     * @return Whether to enter the specified page after create queue
     */
    public boolean editQueue() throws InterruptedException {
        // click queue manage
        Thread.sleep(1000);
        clickElement(QueueManageLocator.CLICK_QUEUE_MANAGE);
        Thread.sleep(1000);

        // click  edit queue button
        clickElement(QueueManageLocator.CLICK_EDIT_QUEUE);
        Thread.sleep(1000);

        // input queue data
        sendInput(QueueManageLocator.INPUT_QUEUE_NAME, QueueManageData.EDIT_QUEUE_NAME);
        sendInput(QueueManageLocator.INPUT_QUEUE_VALUE, QueueManageData.EDIT_QUEUE_VALUE);

        // click  button
        clickButton(QueueManageLocator.SUBMIT_QUEUE);

        // Whether to enter the specified page after submit
        return ifTitleContains(QueueManageData.QUEUE_MANAGE);
    }
}
