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
    TenantManageData tenantManageData = new TenantManageData();

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
        return ifTitleContains(tenantManageData.getTenantData("tenantTitle"));
    }

    /**
     * createTenant
     *
     * @return Whether to enter the specified page after create tenant
     */
    public boolean createTenant() throws InterruptedException {
        clickElement(TenantManageLocator.TENANT_MANAGE);

        //create tenant
        clickButton(TenantManageLocator.CREATE_TENANT_BUTTON);

        // tenant data
        sendInput(TenantManageLocator.TENANT_INPUT_CODE, tenantManageData.getTenantData("tenantCode"));
        sendInput(TenantManageLocator.QUEUE, tenantManageData.getTenantData("queue"));
        sendInput(TenantManageLocator.DESCRIPTION, tenantManageData.getTenantData("description"));

        // click  button
        clickButton(TenantManageLocator.SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTextExists(TenantManageLocator.TENANT_CODE_FIRST, tenantManageData.getTenantData("tenantCode"));
    }

    public boolean deleteTenant() throws InterruptedException {
        clickElement(TenantManageLocator.TENANT_MANAGE);

        // click delete button
        clickButton(TenantManageLocator.DELETE_TENANT_BUTTON);

        //click confirm delete button
        clickButton(TenantManageLocator.CONFIRM_DELETE_TENANT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(tenantManageData.getTenantData("tenantTitle"));
    }
}
