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

package org.apache.dolphinscheduler.api.test.pages;

import org.apache.dolphinscheduler.api.test.base.IPageAPIFactory;
import org.apache.dolphinscheduler.api.test.pages.login.LoginPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.project.ProjectPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.WorkFlowPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.TenantPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.user.UserPageAPI;
import org.apache.dolphinscheduler.api.test.pages.token.TokenPageAPI;

import io.restassured.specification.RequestSpecification;

public class PageAPIFactory implements IPageAPIFactory {
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public PageAPIFactory(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public TenantPageAPI createTenantPageAPI() {
        return new TenantPageAPI(reqSpec, sessionId);
    }

    @Override
    public LoginPageAPI createLoginPageAPI() {
        return new LoginPageAPI();
    }

    @Override
    public ProjectPageAPI createProjectPageAPI() {
        return new ProjectPageAPI(reqSpec, sessionId);
    }

    @Override
    public WorkFlowPageAPI createWorkFlowPageAPI() {
        return new WorkFlowPageAPI(reqSpec, sessionId);
    }

    @Override
    public TokenPageAPI createTokenPageAPI() {
        return new TokenPageAPI(reqSpec, sessionId);
    }

    @Override
    public UserPageAPI createUserPageAPI() {
        return new UserPageAPI(reqSpec, sessionId);
    }


}
