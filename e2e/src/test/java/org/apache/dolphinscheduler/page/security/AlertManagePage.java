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
    AlertManageData alertManageData = new AlertManageData();

    /**
     * Unique constructor
     * @param driver driver
     */
    public AlertManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * create alert
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createAlert() throws InterruptedException {
        // click  alert manage
        System.out.println("start click alert manage button");
        clickElement(AlertManageLocator.CLICK_ALERT_MANAGE);

        //determine whether the create alert button exists
        ifTextExists(AlertManageLocator.CLICK_CREATE_ALERT,alertManageData.getAlertData("createAlert"));

        // click  create alert button
        System.out.println("start click create alert  button");
        clickElement(AlertManageLocator.CLICK_CREATE_ALERT);
        // input alert data
        System.out.println("start input  alert ");
        sendInput(AlertManageLocator.INPUT_ALERT_NAME, alertManageData.getAlertData("alertName"));

        clickElement(AlertManageLocator.CLICK_ALERT_TYPE);

        clickElement(AlertManageLocator.SELECT_ALERT_EMAIL);

        sendInput(AlertManageLocator.INPUT_ALERT_DESCRIPTION, alertManageData.getAlertData("description"));

        // click  button
        clickButton(AlertManageLocator.SUBMIT_ALERT);

        // Whether to enter the specified page after submit
        return ifTextExists(AlertManageLocator.ALERT_NAME, alertManageData.getAlertData("alertName"));
    }

    public boolean deleteAlert() throws InterruptedException {

        // click  alert manage
        clickElement(AlertManageLocator.CLICK_ALERT_MANAGE);

        ifTextExists(AlertManageLocator.ALERT_NAME, alertManageData.getAlertData("alertName"));

        // click  delete alert button
        clickButton(AlertManageLocator.DELETE_ALERT_BUTTON);

        // click confirm delete button
        clickButton(AlertManageLocator.CONFIRM_DELETE_ALERT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(alertManageData.getAlertData("alertTitle"));
    }
}
