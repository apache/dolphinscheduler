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

package org.apache.dolphinscheduler.sdk.model.task.definition;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

public class QueryTaskDefinitionListPagingRequest extends TeaModel {

    @NameInMap(value = "path")
    @Validation(required = true)
    public QueryTaskDefinitionListPagingRequestPath path;

    @NameInMap(value = "query")
    @Validation(required = true)
    public QueryTaskDefinitionListPagingRequestQuery query;

    public static QueryTaskDefinitionListPagingRequest build(java.util.Map<String, ?> map) throws Exception {
        QueryTaskDefinitionListPagingRequest self = new QueryTaskDefinitionListPagingRequest();
        return TeaModel.build(map, self);
    }

    public static class QueryTaskDefinitionListPagingRequestPath extends TeaModel {
        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public String projectCode;

        public static QueryTaskDefinitionListPagingRequestPath build(java.util.Map<String, ?> map) throws Exception {
            QueryTaskDefinitionListPagingRequestPath self = new QueryTaskDefinitionListPagingRequestPath();
            return TeaModel.build(map, self);
        }
    }

    public static class QueryTaskDefinitionListPagingRequestQuery extends TeaModel {
        @NameInMap(value = "pageNo")
        @Validation(required = true)
        public Integer pageNo;

        @NameInMap(value = "pageSize")
        @Validation(required = true)
        public Integer pageSize;

        @NameInMap(value = "searchVal")
        public String searchVal;

        @NameInMap(value = "taskType")
        public String taskType;

        @NameInMap(value = "userId")
        public String userId;

        public static QueryTaskDefinitionListPagingRequestQuery build(java.util.Map<String, ?> map) throws Exception {
            QueryTaskDefinitionListPagingRequestQuery self = new QueryTaskDefinitionListPagingRequestQuery();
            return TeaModel.build(map, self);
        }
    }
}
