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

import java.io.InputStream;

public class ImportProcessDefinitionRequest extends TeaModel {

    @NameInMap(value = "path")
    @Validation(required = true)
    public Path path;

    @NameInMap(value = "query")
    public Query query;

    @NameInMap(value = "stream")
    public InputStream stream;

    public static class Path extends TeaModel {

        @NameInMap(value = "projectCode")
        @Validation(required = true)
        public long projectCode;

        public static Path build(java.util.Map<String, ?> map) throws Exception {
            Path self = new Path();
            return TeaModel.build(map, self);
        }
    }

    public static class Query extends TeaModel {
        @NameInMap(value = "fileName")
        @Validation(required = true)
        public String fileName;

        public static Query build(java.util.Map<String, ?> map) throws Exception {
            Query self = new Query();
            return TeaModel.build(map, self);
        }
    }

    public static ImportProcessDefinitionRequest build(java.util.Map<String, ?> map) throws Exception {
        ImportProcessDefinitionRequest self = new ImportProcessDefinitionRequest();
        return TeaModel.build(map, self);
    }
}
