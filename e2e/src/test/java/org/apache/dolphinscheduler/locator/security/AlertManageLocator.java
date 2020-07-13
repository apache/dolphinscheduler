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

public class AlertManageLocator {
    //create alert locator
    public static final By CLICK_ALERT_MANAGE = By.xpath("//div[4]/div/a/div/a/span");
    public static final By CLICK_CREATE_ALERT = By.xpath("//div[1]/div[2]/div/div[2]/div[2]/div/div[1]/button/span");
    public static final By INPUT_ALERT_NAME = By.xpath("//div[2]/div/div[1]/div[2]/div/input");
    public static final By CLICK_ALERT_TYPE = By.xpath("//div[2]/div/div[2]/div/div[2]/div[2]/div/div[1]/div/input");
    public static final By SELECT_ALERT_EMAIL = By.xpath("//div[2]/div/div[2]/div/div[2]/div[2]/div/div[2]/div/div/div/ul/li[1]/span");
    public static final By INPUT_ALERT_DESCRIPTION = By.xpath("//textarea");
    public static final By SUBMIT_ALERT = By.xpath("//div[3]/button[2]/span");

    //delete alert locator
    public static final By DELETE_ALERT_BUTTON = By.xpath("//span/button");
    public static final By CONFIRM_DELETE_ALERT_BUTTON = By.xpath("//div[2]/div/button[2]/span");
}

