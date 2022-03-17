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

package org.apache.dolphinscheduler.sdk.model.task.instance;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

public class QueryTaskListPagingRequest extends TeaModel {

    @NameInMap(value = "path")
    @Validation(required = true)
    public QueryTaskListPagingRequestPath path;

    @NameInMap(value = "query")
    @Validation(required = true)
    public QueryTaskListPagingRequestQuery query;

    public static QueryTaskListPagingRequest build(java.util.Map<String, ?> map) throws Exception {
        QueryTaskListPagingRequest self = new QueryTaskListPagingRequest();
        return TeaModel.build(map, self);
    }

    public static class QueryTaskListPagingRequestPath extends TeaModel {

        @NameInMap("projectCode")
        @Validation(required = true)
        public String projectCode;

        public static QueryTaskListPagingRequestPath build(java.util.Map<String, ?> map) throws Exception {
            QueryTaskListPagingRequestPath self = new QueryTaskListPagingRequestPath();
            return TeaModel.build(map, self);
        }
    }

    public static class QueryTaskListPagingRequestQuery extends TeaModel {
        @NameInMap(value = "pageNo")
        @Validation(required = true)
        public Integer pageNo;
        @NameInMap(value = "pageSize")
        @Validation(required = true)
        public Integer pageSize;
        @NameInMap(value = "endDate")
        public String endDate;
        @NameInMap(value = "executorName")
        public String executorName;
        @NameInMap(value = "host")
        public String host;
        @NameInMap(value = "processInstanceId")
        public String processInstanceId;
        @NameInMap(value = "processInstanceName")
        public String processInstanceName;
        @NameInMap(value = "searchVal")
        public String searchVal;
        @NameInMap(value = "startDate")
        public String startDate;
        @NameInMap(value = "stateType")
        public String stateType;
        @NameInMap(value = "taskName")
        public String taskName;

        public static QueryTaskListPagingRequestQuery build(java.util.Map<String, ?> map) throws Exception {
            QueryTaskListPagingRequestQuery self = new QueryTaskListPagingRequestQuery();
            return TeaModel.build(map, self);
        }
    }
}
