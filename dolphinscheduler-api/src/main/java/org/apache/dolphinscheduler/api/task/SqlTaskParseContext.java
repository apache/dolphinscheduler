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

package org.apache.dolphinscheduler.api.task;

import org.apache.dolphinscheduler.dao.entity.DataSource;

import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class SqlTaskParseContext {

    private Map<String, Object> processProperties;

    private Map<String, Object> hints;

    private String sql;

    private String taskName;

    private DataSource dataSource;

    public <T> T hint(String key) {
        Object o = hints.get(key);
        if (Objects.nonNull(o)) {
            return (T) o;
        }
        o = processProperties.get(key);
        if (Objects.nonNull(o)) {
            return (T) o;
        }
        return null;
    }

    public <T> T hintOrDefault(String key, T defaultValue) {
        T hint = hint(key);
        if (Objects.nonNull(hint)) {
            return hint;
        } else {
            return defaultValue;
        }
    }
}
