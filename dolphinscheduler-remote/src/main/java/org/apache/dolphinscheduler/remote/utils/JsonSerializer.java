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

package org.apache.dolphinscheduler.remote.utils;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * json serialize or deserialize
 */
@Slf4j
public class JsonSerializer {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .addModule(new SimpleModule()
                    .addSerializer(LocalDateTime.class, new JSONUtils.LocalDateTimeSerializer())
                    .addDeserializer(LocalDateTime.class, new JSONUtils.LocalDateTimeDeserializer()))
            .defaultTimeZone(TimeZone.getDefault())
            .defaultDateFormat(new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS))
            .build();

    private JsonSerializer() {

    }

    /**
     * serialize to byte
     *
     * @param obj object
     * @param <T> object type
     * @return byte array
     */
    public static <T> byte[] serialize(T obj) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("serializeToString exception!", e);
        }

        return json.getBytes(Constants.UTF8);
    }

    /**
     * serialize to string
     *
     * @param obj object
     * @param <T> object type
     * @return string
     */
    public static <T> String serializeToString(T obj) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("serializeToString exception!", e);
        }

        return json;
    }

    /**
     * deserialize
     *
     * @param src byte array
     * @param clazz class
     * @param <T> deserialize type
     * @return deserialize type
     */
    public static <T> T deserialize(byte[] src, Class<T> clazz) {

        String json = new String(src, StandardCharsets.UTF_8);
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("deserialize exception!", e);
            return null;
        }

    }

}
