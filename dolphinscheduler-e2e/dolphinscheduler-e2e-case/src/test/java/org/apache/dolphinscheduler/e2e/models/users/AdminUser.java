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

package org.apache.dolphinscheduler.e2e.models.users;

import org.apache.dolphinscheduler.e2e.models.tenant.BootstrapTenant;
import org.apache.dolphinscheduler.e2e.models.tenant.ITenant;

import lombok.Data;

@Data
public class AdminUser implements IUser {

    private String userName;

    private String password;

    private String email;

    private String phone;

    private ITenant tenant;

    @Override
    public String getUserName() {
        return "admin";
    }

    @Override
    public String getPassword() {
        return "dolphinscheduler123";
    }

    @Override
    public String getEmail() {
        return "admin@gmail.com";
    }

    @Override
    public String getPhone() {
        return "15800000000";
    }

    @Override
    public String getTenant() {
        return new BootstrapTenant().getTenantCode();
    }

}
