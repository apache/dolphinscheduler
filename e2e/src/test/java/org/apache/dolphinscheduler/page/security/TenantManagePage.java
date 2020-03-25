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
import org.apache.dolphinscheduler.data.security.TenantManageData;
import org.apache.dolphinscheduler.locator.security.TenantManageLocator;
import org.openqa.selenium.WebDriver;

public class TenantManagePage extends PageCommon {
    /**
     * Unique constructor
     * @param driver driver
     */
    public TenantManagePage(WebDriver driver) {
        super(driver);
    }

    /**
     * jump security page
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean jumpSecurity() throws InterruptedException {
        clickTopElement(TenantManageLocator.SECURITY_CENTER);
        return ifTitleContains(TenantManageData.TENANT_MANAGE);
    }

    /**
     * createTenant
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createTenant() throws InterruptedException {
        clickButton(TenantManageLocator.TENANT_MANAGE);

        //create tenant
        clickButton(TenantManageLocator.CREATE_TENANT_BUTTON);

        // tenant data
        sendInput(TenantManageLocator.TENANT_INPUT_CODE, TenantManageData.TENANT_CODE);
        sendInput(TenantManageLocator.TENANT_INPUT_NAME, TenantManageData.TENANT_NAME);
        sendInput(TenantManageLocator.QUEUE, TenantManageData.QUEUE);
        sendInput(TenantManageLocator.DESCRIPTION, TenantManageData.DESCRIPTION);

        // click  button
        clickButton(TenantManageLocator.SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(TenantManageData.TENANT_MANAGE);
    }

    public boolean deleteTenant() throws InterruptedException {
        clickButton(TenantManageLocator.TENANT_MANAGE);

        // click delete button
        clickButton(TenantManageLocator.DELETE_TENANT_BUTTON);

        //click confirm delete button
        clickButton(TenantManageLocator.CONFIRM_DELETE_TENANT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(TenantManageData.TENANT_MANAGE);
    }
}
