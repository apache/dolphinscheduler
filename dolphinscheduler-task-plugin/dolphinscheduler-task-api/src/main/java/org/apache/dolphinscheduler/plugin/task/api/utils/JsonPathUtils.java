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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import java.util.Optional;
import java.util.Set;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minidev.json.JSONArray;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.internal.DefaultsImpl;

@UtilityClass
public class JsonPathUtils {

    static {
        Set<Option> options = DefaultsImpl.INSTANCE.options();
        options.add(Option.DEFAULT_PATH_LEAF_TO_NULL);
    }

    /**
     * @param jsonString
     * @param jsonPath
     * @param <T>
     * @return
     */
    public static <T> Optional<T> read(@NonNull String jsonString, @NonNull String jsonPath) {
        return Optional.ofNullable(JsonPath.read(jsonString, jsonPath));
    }

    public static boolean exist(@NonNull String jsonString, @NonNull String jsonPath) {
        JSONArray jsonArray = JsonPath.read(jsonString, jsonPath);
        return !jsonArray.isEmpty();
    }

}
