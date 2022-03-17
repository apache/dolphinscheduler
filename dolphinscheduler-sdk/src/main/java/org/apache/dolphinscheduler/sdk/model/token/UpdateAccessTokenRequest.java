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

package org.apache.dolphinscheduler.sdk.model.token;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

/**
 * @author ouyangyewei
 * @date 2021-11-23
 **/
public class UpdateAccessTokenRequest extends TeaModel {
    @NameInMap(value = "path")
    @Validation(required = true)
    public UpdateAccessTokenRequest.Path path = new UpdateAccessTokenRequest.Path();

    @NameInMap(value = "query")
    @Validation(required = true)
    public UpdateAccessTokenRequest.Query query = new UpdateAccessTokenRequest.Query();

    public static class Path extends TeaModel {
        @NameInMap(value = "id")
        @Validation(required = true)
        public Integer id;
    }

    public static class Query extends TeaModel {
        @NameInMap(value = "userId")
        @Validation(required = true)
        public Integer userId;

        @NameInMap(value = "token")
        @Validation(required = false)
        public String accessToken;

        @NameInMap(value = "expireTime")
        @Validation(required = true)
        public String expireTime;
    }
}
