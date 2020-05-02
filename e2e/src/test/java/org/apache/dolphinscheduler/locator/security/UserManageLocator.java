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

    public static final By CLICK_USER_MANAGE = By.xpath("//div[3]/div/a/div/a/span");

    public static final By CLICK_CREATE_USER_BUTTON = By.xpath("//span[contains(.,'创建用户')]");

    public static final By INPUT_USERNAME = By.xpath("//div[2]/div/div/div[2]/div/input");

    public static final By INPUT_PASSWORD = By.xpath("//div[2]/div[2]/div/input");

    public static final By CLICK_TENANT = By.xpath("//div[3]/div[2]/div/div/div/input");

    public static final By SELECT_TENANT = By.xpath("//div[3]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By CLICK_QUEUE = By.xpath("//div[4]/div[2]/div/div/div/input");

    public static final By SELECT_QUEUE = By.xpath("//div[4]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By TENANT_INPUT_EMAIL = By.xpath("//div[5]/div[2]/div/input");

    public static final By TENANT_INPUT_PHONE = By.xpath("//div[6]/div[2]/div/input");

    public static final By SUBMIT = By.xpath("//div[3]/button[2]/span");

    public static final By DELETE_USER_BUTTON = By.xpath("//span[2]/button/i");

    public static final By CONFIRM_DELETE_USER_BUTTON = By.xpath("//div[2]/div/button[2]/span");
}
