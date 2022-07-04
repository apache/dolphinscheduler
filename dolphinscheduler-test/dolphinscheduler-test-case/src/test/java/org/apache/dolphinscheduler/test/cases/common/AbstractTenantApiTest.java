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

package org.apache.dolphinscheduler.test.cases.common;

import com.devskiller.jfairy.Fairy;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.TenantEndPoints;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantResponseEntity;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractTenantApiTest extends AbstractApiTest {
    protected final Fairy fairy = Fairy.create();
    protected TenantRequestEntity tenantRequestEntity = null;
    protected TenantResponseEntity tenantResponseEntity = null;
    protected TenantEndPoints tenantEndPoints = null;

    @BeforeAll
    public void initTenantEndPointFactory() {
        tenantEndPoints = endPointFactory.createTenantEndPoints();
    }

}
