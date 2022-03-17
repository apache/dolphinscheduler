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

package org.apache.dolphinscheduler.sdk.model.schedule;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

import java.util.Map;

/**
 * @author lijia
 * Created on 2021/11/26
 */
public class UpdateScheduleRequest extends TeaModel {

    @NameInMap(value = "query")
    @Validation(required = true)
    public Query query;

    @NameInMap(value = "path")
    @Validation(required = true)
    public Path path;

    public static class Query extends TeaModel {
        @NameInMap(value = "schedule")
        @Validation(required = true)
        public String schedule;

        @NameInMap(value = "warningType")
        public String warningType;

        @NameInMap(value = "warningGroupId")
        public int warningGroupId;

        @NameInMap(value = "failureStrategy")
        public String failureStrategy;

        @NameInMap(value = "workerGroup")
        public String workerGroup;

        @NameInMap(value = "environmentCode")
        public long environmentCode;

        @NameInMap(value = "processInstancePriority")
        public String processInstancePriority;


        public static Query build(Map<String, ?> map) throws Exception {
            Query self = new Query();
            return TeaModel.build(map, self);
        }
    }

    public static class Path extends TeaModel {
        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public long projectCode;

        @NameInMap(value = "id")
        @Validation(required = true)
        public int id;

        public static Path build(Map<String, ?> map) throws Exception {
            Path self = new Path();
            return TeaModel.build(map, self);
        }
    }
}
