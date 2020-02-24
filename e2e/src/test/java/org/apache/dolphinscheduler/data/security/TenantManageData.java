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

/**
 * Tenant page object: data
 */
package org.apache.dolphinscheduler.data.security;

import org.apache.dolphinscheduler.data.LoginData;

public class TenantManageData {
    /**
     * Tenant URL
     */
    public static final String TENANAT_URL =  LoginData.URL + "/ui/#/security/tenant";

    /**
     * Tenant Code
     */
    public static final String TENANAT_CODE = "dolphinscheduler_tenant_code2";

    /**
     * Tenant Name
     */
    public static final String TENANAT_NAME = "dolphinscheduler_tenant_Name";

    /**
     * Queue
     */
    public static final String QUEUE = "default";

    /**
     * Description
     */
    public static final String DESCRIPTION = "creat tenant test";

    public static final String TENANAT_MANAGE = "租户管理 - DolphinScheduler";



}
