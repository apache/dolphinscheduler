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

package org.apache.dolphinscheduler.sdk.model.tenant;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

public class CreateTenantRequest extends TeaModel {
    @NameInMap(value = "query")
    @Validation(required = true)
    public CreateTenantRequest.Query query = new CreateTenantRequest.Query();

    public static class Query extends TeaModel {
        @NameInMap(value = "queueId")
        @Validation(required = true)
        public Integer queueId;

        @NameInMap(value = "tenantCode")
        @Validation(required = true)
        public String tenantCode;

        @NameInMap(value = "description")
        @Validation(required = true)
        public String description;
    }
}
