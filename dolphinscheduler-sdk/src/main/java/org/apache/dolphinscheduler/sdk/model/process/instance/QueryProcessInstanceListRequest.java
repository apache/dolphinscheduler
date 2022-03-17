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

package org.apache.dolphinscheduler.sdk.model.process.instance;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

import java.util.Map;

public class QueryProcessInstanceListRequest extends TeaModel {
    @NameInMap(value = "query")
    @Validation(required = true)
    public QueryProcessInstanceListRequest.Query query;

    @NameInMap(value = "path")
    @Validation(required = true)
    public QueryProcessInstanceListRequest.Path path;

    public static class Query extends TeaModel {
        @NameInMap(value = "processDefineCode")
        public long processDefineCode;
        @NameInMap(value = "searchVal")
        public String searchVal;
        @NameInMap(value = "executorName")
        public String executorName;
        @NameInMap(value = "stateType")
        public String stateType;
        @NameInMap(value = "host")
        public String host;
        @NameInMap(value = "startDate")
        public String startDate;
        @NameInMap(value = "endDate")
        public String endDate;
        @NameInMap(value = "pageNo")
        @Validation(required = true)
        public int pageNo;
        @NameInMap(value = "pageSize")
        @Validation(required = true)
        public int pageSize;


        public static QueryProcessInstanceListRequest.Query build(Map<String, ?> map) throws Exception {
            QueryProcessInstanceListRequest.Query self = new QueryProcessInstanceListRequest.Query();
            return TeaModel.build(map, self);
        }
    }

    public static class Path extends TeaModel {

        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public long projectCode;

        public static QueryProcessInstanceListRequest.Path build(Map<String, ?> map) throws Exception {
            QueryProcessInstanceListRequest.Path self = new QueryProcessInstanceListRequest.Path();
            return TeaModel.build(map, self);
        }
    }
}
