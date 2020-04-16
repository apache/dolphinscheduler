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

public class TenantManageLocator{
    public static final By SECURITY_CENTER = By.xpath("//div[2]/div[6]/div/a/span/em");

    public static final By TENANT_MANAGE = By.xpath("//div[2]/div/a/div/a/span");

    public static final By CREATE_TENANT_BUTTON = By.xpath("//button/span");

    public static final By TENANT_INPUT_CODE = By.xpath("//div[2]/div/div/div[2]/div/input");

    public static final By TENANT_INPUT_NAME = By.xpath("//div[2]/div[2]/div/input");

    public static final By QUEUE = By.xpath("//textarea");

    public static final By DESCRIPTION = By.xpath("//textarea");

    public static final By SUBMIT_BUTTON = By.xpath("//div[3]/button[2]/span");

    public static final By DELETE_TENANT_BUTTON = By.xpath("//div[3]/div[1]/div/table/tr[2]/td[8]/span/button");

    public static final By CONFIRM_DELETE_TENANT_BUTTON = By.xpath("//div[2]/div/button[2]/span");
}
