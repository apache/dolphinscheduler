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
package org.apache.dolphinscheduler.locator.security;

import org.openqa.selenium.By;

public class TokenManageLocator {
    //create token
    public static final By CLICK_TOKEN_MANAGE = By.xpath("//div[7]/div/a/div/a/span");

    public static final By CLICK_CREATE_TOKEN = By.xpath("//div[2]/div/div[2]/div[2]/div/div[1]/button/span");

    public static final By SELECT_USER = By.xpath("//div[2]/div[2]/div/div/div/span/i");

    public static final By CLICK_GENERATE_TOKEN_BUTTON = By.xpath("//div[3]/div[2]/button/span");

    public static final By CLICK_SUBMIT_BUTTON = By.xpath("//div[3]/button[2]/span");

    //edit token
    public static final By CLICK_EDIT_BUTTON = By.xpath("//div[3]/div[1]/div/table/tr[2]/td[7]/button/i");

    //delete token
    public static final By CLICK_DELETE_BUTTON = By.xpath("//div[3]/div[1]/div/table/tr[2]/td[7]/span/button");

    public static final By CLICK_CONFIRM_DELETE_BUTTON = By.xpath("//div[2]/div/button[2]/span");

}
