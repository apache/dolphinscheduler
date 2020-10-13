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
    TokenManageData tokenManageData = new TokenManageData();

    public TokenManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * create token
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createToken() throws InterruptedException {
        //create token
        clickElement(TokenManageLocator.CLICK_TOKEN_MANAGE);

        //determine whether the create token button exists
        ifTextExists(TokenManageLocator.CLICK_CREATE_TOKEN, tokenManageData.getTokenData("createTokenText"));

        // click  create token button
        clickButton(TokenManageLocator.CLICK_CREATE_TOKEN);

        ifTextExists(TokenManageLocator.CREATE_TOKEN_POPUP,tokenManageData.getTokenData("createTokenText"));

        clickButton(TokenManageLocator.SELECT_USER);

        clickButton(TokenManageLocator.CLICK_GENERATE_TOKEN_BUTTON);
        Thread.sleep(2000);

        // click  button
        clickButton(TokenManageLocator.CLICK_SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(tokenManageData.getTokenData("tokenTitle"));
    }

    /**
     * edit token
     *
     * @return Whether to enter the specified page after edit tenant
     */
    public boolean editToken() throws InterruptedException {
        // edit token
        ifTextExists(TokenManageLocator.TOKEN, "1");

        // determine the existence of the editing token
        locateElement(TokenManageLocator.EDIT_TOKEN_BUTTON);

        // click  edit token button
        clickButton(TokenManageLocator.EDIT_TOKEN_BUTTON);

        clickButton(TokenManageLocator.SELECT_USER);

        clickButton(TokenManageLocator.CLICK_GENERATE_TOKEN_BUTTON);
        Thread.sleep(2000);

        // click  button
        clickButton(TokenManageLocator.CLICK_SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(tokenManageData.getTokenData("tokenTitle"));
    }


    //delete token
    public boolean deleteToken() throws InterruptedException {
        // click  token manage
        clickElement(TokenManageLocator.CLICK_TOKEN_MANAGE);

        clickButton(TokenManageLocator.CLICK_DELETE_BUTTON);
        clickButton(TokenManageLocator.CLICK_CONFIRM_DELETE_BUTTON);

        return ifTitleContains(tokenManageData.getTokenData("tokenTitle"));
    }

}
