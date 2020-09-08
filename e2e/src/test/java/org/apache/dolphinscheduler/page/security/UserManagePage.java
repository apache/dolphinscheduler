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
     * create user
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
        sendInput(UserManageLocator.INPUT_EMAIL, UserManageData.EMAIL);
        sendInput(UserManageLocator.INPUT_PHONE, UserManageData.PHONE);
        clickElement(UserManageLocator.SELECT_STOP_STATE);
        clickElement(UserManageLocator.SELECT_ENABLE_STATE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }

    /**
     * edit general user
     */
    public boolean editGeneralUser() throws InterruptedException {
        //edit general user
        // click  user manage
        System.out.println("start edit general user");
        Thread.sleep(500);
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        // click  edit user button
        clickButton(UserManageLocator.EDIT_GENERAL_USER_BUTTON );

        // input user data
        clearSendInput(UserManageLocator.INPUT_USERNAME, UserManageData.EDIT_USERNAME);
        clearSendInput(UserManageLocator.INPUT_PASSWORD, UserManageData.EDIT_PASSWORD);

        clickButton(UserManageLocator.CLICK_TENANT);
        clickButton(UserManageLocator.SELECT_TENANT);

        clickButton(UserManageLocator.CLICK_QUEUE);
        clickButton(UserManageLocator.SELECT_QUEUE);

        clearSendInput(UserManageLocator.INPUT_EMAIL, UserManageData.EDIT_EMAIL);
        clearSendInput(UserManageLocator.INPUT_PHONE, UserManageData.EDIT_PHONE);

        clickElement(UserManageLocator.SELECT_STOP_STATE);
        clickElement(UserManageLocator.SELECT_ENABLE_STATE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);
        System.out.println("end edit general user");

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }

    /**
     * edit admin user
     */
    public boolean editAdminlUser() throws InterruptedException {
        //edit admin user
        // click  user manage
        System.out.println("start edit admin user");
        Thread.sleep(500);
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        // click  edit user button
        clickButton(UserManageLocator.EDIT_ADMIN_USER_BUTTON );

        // select tenant
        clickButton(UserManageLocator.CLICK_TENANT);

        clickButton(UserManageLocator.SELECT_TENANT);

        // click  button
        clickButton(UserManageLocator.SUBMIT);
        System.out.println("end edit admin user");
        Thread.sleep(500);
        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }


    /**
     * delete user
     */
    public boolean deleteUser() throws InterruptedException {
        System.out.println("jump to user manage");
        // click  user manage
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        // click  delete user button
        System.out.println("click delete user");
        clickButton(UserManageLocator.DELETE_USER_BUTTON );

        // click confirm delete button
        System.out.println("click confirm delete user");
        clickButton(UserManageLocator.CONFIRM_DELETE_USER_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(UserManageData.USER_MANAGE);
    }
}
