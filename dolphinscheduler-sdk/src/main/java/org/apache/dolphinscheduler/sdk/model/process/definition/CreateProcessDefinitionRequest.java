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

package org.apache.dolphinscheduler.sdk.model.process.definition;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.Validation;

public class CreateProcessDefinitionRequest extends TeaModel {

    @NameInMap(value = "body")
    @Validation(required = true)
    public Body body;

    @NameInMap(value = "path")
    @Validation(required = true)
    public Path path;

    public static class Body extends TeaModel {

        /**
         * 流程定义节点坐标位置信息(json格式)
         */
        @NameInMap(value = "locations")
        @Validation(required = true)
        public String locations;
        /**
         * 流程定义名称
         */
        @NameInMap(value = "name")
        @Validation(required = true)
        public String name;

        @NameInMap(value = "taskDefinitionJson")
        @Validation(required = true)
        public String taskDefinitionJson;

        @NameInMap(value = "taskRelationJson")
        @Validation(required = true)
        public String taskRelationJson;

        @NameInMap(value = "tenantCode")
        @Validation(required = true)
        public String tenantCode;

        @NameInMap(value = "description")
        @Validation(required = true)
        public String description;

        /**
         * executionType,可用值:PARALLEL,SERIAL_WAIT,SERIAL_DISCARD,SERIAL_PRIORITY
         */
        @NameInMap(value = "executionType")
        @Validation(required = true)
        public String executionType;

        @NameInMap(value = "globalParams")
        @Validation(required = true)
        public String globalParams;

        @NameInMap(value = "timeout")
        @Validation(required = true)
        public Integer timeout;


        public static Body build(java.util.Map<String, ?> map) throws Exception {
            Body self = new Body();
            return TeaModel.build(map, self);
        }
    }

    public static class Path extends TeaModel {
        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public long projectCode;

        public static Path build(java.util.Map<String, ?> map) throws Exception {
            Path self = new Path();
            return TeaModel.build(map, self);
        }
    }

    public static CreateProcessDefinitionRequest build(java.util.Map<String, ?> map) throws Exception {
        CreateProcessDefinitionRequest self = new CreateProcessDefinitionRequest();
        return TeaModel.build(map, self);
    }
}
