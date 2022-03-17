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

package org.apache.dolphinscheduler.sdk.model.user;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

public class CreateUserRequest extends TeaModel {

    @NameInMap(value = "query")
    @Validation(required = true)
    public Query query = new CreateUserRequest.Query();

    public static class Query extends TeaModel {
        @NameInMap(value = "email")
        @Validation(required = true)
        public String email;

        @NameInMap(value = "tenantId")
        @Validation(required = true)
        public Integer tenantId;

        @NameInMap(value = "userName")
        @Validation(required = true)
        public String userName;

        @NameInMap(value = "userPassword")
        @Validation(required = true)
        public String userPassword;

        @NameInMap(value = "phone")
        public String phone;

        @NameInMap(value = "queue")
        public String queue;

        @NameInMap(value = "state")
        @Validation(required = true)
        public Integer state;
    }
}
