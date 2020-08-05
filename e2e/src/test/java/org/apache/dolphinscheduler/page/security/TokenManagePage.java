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
import org.apache.dolphinscheduler.data.security.TokenManageData;
import org.apache.dolphinscheduler.locator.security.TokenManageLocator;
import org.openqa.selenium.WebDriver;

public class TokenManagePage extends PageCommon {
    public TokenManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * createTenant
     *
     * @return Whether to enter the specified page after creat tenant
     */
    public boolean createToken() throws InterruptedException {
        //create token
        Thread.sleep(1000);
        clickElement(TokenManageLocator.CLICK_TOKEN_MANAGE);
        Thread.sleep(1000);

        // click  create token button
        clickButton(TokenManageLocator.CLICK_CREATE_TOKEN);
        Thread.sleep(1000);

        //selectDate(TokenManageLocator.js, TokenManageLocator.CLICK_TIME, TokenManageData.DATE);

        clickButton(TokenManageLocator.SELECT_USER);

        clickButton(TokenManageLocator.CLICK_GENERATE_TOKEN_BUTTON);
        Thread.sleep(2500);

        // click  button
        clickButton(TokenManageLocator.CLICK_SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(TokenManageData.TOKEN_MANAGE);
    }


    //edit token
    public boolean editToken() throws InterruptedException {
        // click  token manage
        clickElement(TokenManageLocator.CLICK_TOKEN_MANAGE);
        Thread.sleep(1000);

        // click  create token button
        clickButton(TokenManageLocator.CLICK_EDIT_BUTTON);
        Thread.sleep(1000);

        clickButton(TokenManageLocator.SELECT_USER);

        clickButton(TokenManageLocator.CLICK_GENERATE_TOKEN_BUTTON);
        Thread.sleep(2500);

        // click  button
        clickButton(TokenManageLocator.CLICK_SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(TokenManageData.TOKEN_MANAGE);
    }


    //delete token
    public boolean deleteToken() throws InterruptedException {
        // click  token manage
        clickElement(TokenManageLocator.CLICK_TOKEN_MANAGE);
        Thread.sleep(1000);

        clickButton(TokenManageLocator.CLICK_DELETE_BUTTON);
        clickButton(TokenManageLocator.CLICK_CONFIRM_DELETE_BUTTON);

        return ifTitleContains(TokenManageData.TOKEN_MANAGE);
    }

}
