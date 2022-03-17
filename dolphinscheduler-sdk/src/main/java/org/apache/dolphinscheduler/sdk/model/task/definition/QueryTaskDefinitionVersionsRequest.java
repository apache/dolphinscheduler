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

public class QueryTaskDefinitionVersionsRequest extends TeaModel {
    @NameInMap(value = "query")
    @Validation(required = true)
    public Query query;
    @NameInMap(value = "path")
    @Validation(required = true)
    public Path path;

    public static GenTaskCodeListRequest build(java.util.Map<String, ?> map) throws Exception {
        GenTaskCodeListRequest self = new GenTaskCodeListRequest();
        return TeaModel.build(map, self);
    }

    public static class Query extends TeaModel {

        @NameInMap(value = "pageNo")
        @Validation(required = true)
        public Integer pageNo;

        @NameInMap(value = "pageSize")
        @Validation(required = true)
        public Integer pageSize;

        public static Query build(java.util.Map<String, ?> map) throws Exception {
            Query self = new Query();
            return TeaModel.build(map, self);
        }
    }

    public static class Path extends TeaModel {

        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public String projectCode;

        @NameInMap(value = "code")
        @Validation(required = true)
        public String code;

        public static Path build(java.util.Map<String, ?> map) throws Exception {
            Path self = new Path();
            return TeaModel.build(map, self);
        }
    }


}
