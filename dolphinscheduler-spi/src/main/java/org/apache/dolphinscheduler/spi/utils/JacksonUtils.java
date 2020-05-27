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
package org.apache.dolphinscheduler.spi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * json utils
 */
public class JacksonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JacksonUtils() {
        //Feature that determines whether encountering of unknown properties, false means not analyzer unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setTimeZone(TimeZone.getDefault());
    }

    public static JsonNode toJsonNode(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (IOException e) {
            logger.error("object to json exception!",e);
        }
        return null;
    }

    public static String toJsonString(List<LinkedHashMap<String, Object>> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.error("toJsonString exception!",e);
        }
        return null;
    }

    /**
     * json to list
     *
     * @param json json string
     * @param clazz class
     * @param <T> T
     * @return list
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return new ArrayList<>();
        }
        try {
            JavaType t = objectMapper.getTypeFactory().constructParametricType(
                    List.class, clazz);
            return objectMapper.readValue(json, t);
        } catch (Exception e) {
            logger.error("json to list exception!",e);
        }

        return new ArrayList<>();
    }
}
