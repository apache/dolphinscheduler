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

package org.apache.dolphinscheduler.test.endpoint.utils;

import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;

public class JsonUtil {
    /**
     * jsonToObject
     *
     * @param json
     * @param path       json object access path
     * @param objectType
     * @param <T>
     * @return
     */
    public static <T> T jsonToObject(String json, String path, Class<T> objectType) {
        return JsonPath.from(json).getObject(path, objectType);
    }


    public static <T> List<T> jsonToObjectList(String json, String path, Class<T> objectType) {
        return JsonPath.from(json).getList(path, objectType);
    }


    public static Map<String, Object> jsonToMap(String json, String path) {
        return JsonPath.from(json).getObject(path, new TypeRef<Map<String, Object>>() {});
    }





}
