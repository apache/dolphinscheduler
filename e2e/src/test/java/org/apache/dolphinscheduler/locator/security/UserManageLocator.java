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

public class UserManageLocator {

    /**
     * create user
     */
    public static final By CLICK_USER_MANAGE = By.xpath("//div[3]/div/a/div/a/span");

    public static final By CLICK_CREATE_USER_BUTTON = By.xpath("//span[contains(.,'创建用户')]");

    public static final By INPUT_USERNAME = By.xpath("//div[2]/div/div/div[2]/div/input");

    public static final By INPUT_PASSWORD = By.xpath("//div[2]/div[2]/div/input");

    public static final By CLICK_TENANT = By.xpath("//div[3]/div[2]/div/div/div/input");

    public static final By SELECT_TENANT = By.xpath("//div[3]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By CLICK_QUEUE = By.xpath("//div[4]/div[2]/div/div/div/input");

    public static final By SELECT_QUEUE = By.xpath("//div[4]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By INPUT_EMAIL = By.xpath("//div[5]/div[2]/div/input");

    public static final By INPUT_PHONE = By.xpath("//div[6]/div[2]/div/input");

    public static final By SELECT_STOP_STATE = By.xpath("//div[7]/div[2]/div/label[2]/span/input");

    public static final By SELECT_ENABLE_STATE = By.xpath("//div[7]/div[2]/div/label[1]/span/input");

    public static final By SUBMIT = By.xpath("//div[3]/button[2]/span");

    public static final By USERNAME = By.xpath("//table/tr[2]/td[2]/span");

    /**
     * edit user
     */
    public static final By USER_NAME = By.xpath("//table/tr[2]/td[2]/span");

    public static final By EDIT_GENERAL_USER_BUTTON = By.xpath("//div[3]/div[1]/div/table/tr[2]/td[11]/button");

    public static final By EDIT_ADMIN_USER_BUTTON = By.xpath("//div[3]/div[1]/div/table/tr[3]/td[11]/button");
    /**
     * delete user
     */
    public static final By DELETE_USER_BUTTON = By.xpath("//table/tr[3]/td[11]/span[2]/button");

    public static final By CONFIRM_DELETE_USER_BUTTON = By.xpath("//tr[3]/td[11]/span[2]/div/div[2]/div/button[2]/span");
}
