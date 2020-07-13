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
import org.apache.dolphinscheduler.data.security.UserManageData;
import org.apache.dolphinscheduler.locator.security.UserManageLocator;
import org.openqa.selenium.WebDriver;

public class UserManagePage extends PageCommon {
    public UserManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * createTenant
     *
     * @return Whether to enter the specified page after creat tenant
     */
    public boolean createUser() throws InterruptedException {
        // click  user manage
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        // click  create user button
        clickButton(UserManageLocator.CLICK_CREATE_USER_BUTTON);

        // input user data
        sendInput(UserManageLocator.INPUT_USERNAME, UserManageData.USERNAME);
        sendInput(UserManageLocator.INPUT_PASSWORD, UserManageData.PASSWORD);
        clickButton(UserManageLocator.CLICK_TENANT);
        clickButton(UserManageLocator.SELECT_TENANT);
        clickButton(UserManageLocator.CLICK_QUEUE);
        clickButton(UserManageLocator.SELECT_QUEUE);
        sendInput(UserManageLocator.TENANT_INPUT_EMAIL, UserManageData.EMAIL);
        sendInput(UserManageLocator.TENANT_INPUT_PHONE, UserManageData.PHONE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }

    public boolean deleteUser() throws InterruptedException {

        // click  user manage
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        // click  delete user button
        clickButton(UserManageLocator.DELETE_USER_BUTTON );

        // click confirm delete button
        clickButton(UserManageLocator.CONFIRM_DELETE_USER_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }
}
