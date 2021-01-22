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
    UserManageData userManageData = new UserManageData();

    public UserManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * create user
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createUser() throws InterruptedException {
        // click  user manage
        clickElement(UserManageLocator.CLICK_USER_MANAGE);
        //determine whether the create user button exists
        ifTextExists(UserManageLocator.CLICK_CREATE_USER_BUTTON, userManageData.getUserData("createUserButton"));

        // click  create user button
        clickButton(UserManageLocator.CLICK_CREATE_USER_BUTTON);

        // input user data
        sendInput(UserManageLocator.INPUT_USERNAME,  userManageData.getUserData("userName"));
        sendInput(UserManageLocator.INPUT_PASSWORD, userManageData.getUserData("password"));
        clickButton(UserManageLocator.CLICK_TENANT);
        clickButton(UserManageLocator.SELECT_TENANT);
        clickButton(UserManageLocator.CLICK_QUEUE);
        clickButton(UserManageLocator.SELECT_QUEUE);
        sendInput(UserManageLocator.INPUT_EMAIL, userManageData.getUserData("email"));
        sendInput(UserManageLocator.INPUT_PHONE, userManageData.getUserData("phone"));
        clickElement(UserManageLocator.SELECT_STOP_STATE);
        clickElement(UserManageLocator.SELECT_ENABLE_STATE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);

        // Whether to enter the specified page after submit
        return ifTextExists(UserManageLocator.USERNAME, userManageData.getUserData("userName"));
    }

    /**
     * edit general user
     */
    public boolean editGeneralUser() throws InterruptedException {
        //edit general user
        // click  user manage
        System.out.println("start edit general user");
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        //determine whether the general user exists
        ifTextExists(UserManageLocator.USERNAME, userManageData.getUserData("userName"));

        // click  edit user button
        clickButton(UserManageLocator.EDIT_GENERAL_USER_BUTTON );

        // input user data
        clearSendInput(UserManageLocator.INPUT_USERNAME, userManageData.getUserData("editUserName"));
        clearSendInput(UserManageLocator.INPUT_PASSWORD, userManageData.getUserData("editPassword"));

        clickButton(UserManageLocator.CLICK_TENANT);
        clickButton(UserManageLocator.SELECT_TENANT);

        clickButton(UserManageLocator.CLICK_QUEUE);
        clickButton(UserManageLocator.SELECT_QUEUE);

        clearSendInput(UserManageLocator.INPUT_EMAIL, userManageData.getUserData("editEmail"));
        clearSendInput(UserManageLocator.INPUT_PHONE, userManageData.getUserData("editPhone"));

        clickElement(UserManageLocator.SELECT_STOP_STATE);
        clickElement(UserManageLocator.SELECT_ENABLE_STATE);

        // click  button
        clickButton(UserManageLocator.SUBMIT);
        System.out.println("end edit general user");

        // Whether to enter the specified page after submit
        return ifTitleContains(userManageData.getUserData("userTitle"));
    }

    /**
     * edit admin user
     */
    public boolean editAdminUser() throws InterruptedException {
        //edit admin user
        // click  user manage
        System.out.println("start edit admin user");
        clickElement(UserManageLocator.CLICK_USER_MANAGE);

        //determine whether the general user edit success
        ifTextExists(UserManageLocator.USER_NAME,userManageData.getUserData("editUserName"));

        // click  edit user button
        clickButton(UserManageLocator.EDIT_ADMIN_USER_BUTTON );

        // select tenant
        clickButton(UserManageLocator.CLICK_TENANT);

        clickButton(UserManageLocator.SELECT_TENANT);

        // click  button
        clickButton(UserManageLocator.SUBMIT);
        System.out.println("end edit admin user");
        // Whether to enter the specified page after submit
        return ifTitleContains(userManageData.getUserData("userTitle"));
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
        return ifTitleContains(userManageData.getUserData("userTitle"));
    }
}
