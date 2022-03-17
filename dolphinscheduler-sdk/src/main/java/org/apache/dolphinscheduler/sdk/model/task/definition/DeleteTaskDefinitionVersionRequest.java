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

public class DeleteTaskDefinitionVersionRequest extends TeaModel {
    @NameInMap(value = "path")
    @Validation(required = true)
    public DeleteTaskDefinitionVersionRequestPath path;

    public static DeleteTaskDefinitionVersionRequest build(java.util.Map<String, ?> map) throws Exception {
        DeleteTaskDefinitionVersionRequest self = new DeleteTaskDefinitionVersionRequest();
        return TeaModel.build(map, self);
    }

    public static class DeleteTaskDefinitionVersionRequestPath extends TeaModel {

        @NameInMap(value = "code")
        @Validation(required = true)
        public String code;
        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public String projectCode;
        @NameInMap(value = "version")
        @Validation(required = true)
        public String version;

        public static DeleteTaskDefinitionVersionRequestPath build(java.util.Map<String, ?> map) throws Exception {
            DeleteTaskDefinitionVersionRequestPath self = new DeleteTaskDefinitionVersionRequestPath();
            return TeaModel.build(map, self);
        }
    }
}
