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

package org.apache.dolphinscheduler.sdk.model.analysis;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

import java.util.Map;

/**
 * @author lijia
 * Created on 2021/11/26
 */
public class QueryTaskStatesRequest extends TeaModel {
    @NameInMap(value = "query")
    @Validation(required = true)
    public Query query;

    public static class Query extends TeaModel {
        @NameInMap(value = "projectCode")
        public long projectCode;
        @NameInMap(value = "startDate")
        public String startDate;
        @NameInMap(value = "endDate")
        public String endDate;


        public static Query build(Map<String, ?> map) throws Exception {
            Query self = new Query();
            return TeaModel.build(map, self);
        }
    }
}
