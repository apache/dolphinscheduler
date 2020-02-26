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
package org.apache.dolphinscheduler.page;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.constant.TestConstant;
import org.apache.dolphinscheduler.data.LoginData;
import org.apache.dolphinscheduler.locator.LoginLocator;
import org.apache.dolphinscheduler.util.RedisUtil;
import org.openqa.selenium.WebDriver;



public class LoginPage extends PageCommon {
    /**
     * Unique constructor
     * @param driver driver
     */
    public LoginPage(WebDriver driver) {
        super(driver);
    }


    /**
     * jump page
     */
    public void jumpPage() {
        System.out.println("jump login page");
        super.jumpPage(LoginData.URL);
    }

    /**
     * login
     *
     * @return Whether to enter the specified page after searching
     */
    public boolean login() throws InterruptedException {
        System.out.println("LoginPage");
        // login data
        sendInput(LoginLocator.LOGIN_INPUT_USER, LoginData.USER);
        sendInput(LoginLocator.LOGIN_INPUT_PASSWORD, LoginData.PASSWORD);

        // click login button
        clickButton(LoginLocator.LOGIN_BUTTON);

        moveToElement(LoginLocator.LOGIN_BUTTON_MOVE);

        // Whether to enter the specified page after login
        return ifTitleContains(LoginData.TENANT);
    }
}
