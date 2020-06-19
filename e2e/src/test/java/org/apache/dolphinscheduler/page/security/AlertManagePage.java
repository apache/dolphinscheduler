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
import org.apache.dolphinscheduler.data.security.AlertManageData;
import org.apache.dolphinscheduler.locator.security.AlertManageLocator;
import org.openqa.selenium.WebDriver;

public class AlertManagePage extends PageCommon {
    /**
     * Unique constructor
     * @param driver driver
     */
    public AlertManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * createTenant
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createAlert() throws InterruptedException {
        // click  alert manage
        clickElement(AlertManageLocator.CLICK_ALERT_MANAGE);
        Thread.sleep(1000);

        // click  create alert button
        clickElement(AlertManageLocator.CLICK_CREATE_ALERT);
        Thread.sleep(1000);

        // input alert data
        sendInput(AlertManageLocator.INPUT_ALERT_NAME, AlertManageData.ALERT_NAME);

        clickElement(AlertManageLocator.CLICK_ALERT_TYPE);

        clickElement(AlertManageLocator.SELECT_ALERT_EMAIL);

        sendInput(AlertManageLocator.INPUT_ALERT_DESCRIPTION, AlertManageData.DESCRIPTION);

        // click  button
        clickButton(AlertManageLocator.SUBMIT_ALERT);

        // Whether to enter the specified page after submit
        return ifTitleContains(AlertManageData.ALERT_MANAGE);
    }

    public boolean deleteAlert() throws InterruptedException {

        // click  user manage
        clickElement(AlertManageLocator.CLICK_ALERT_MANAGE);

        // click  delete user button
        clickButton(AlertManageLocator.DELETE_ALERT_BUTTON);

        // click confirm delete button
        clickButton(AlertManageLocator.CONFIRM_DELETE_ALERT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(AlertManageData.ALERT_MANAGE);
    }
}
