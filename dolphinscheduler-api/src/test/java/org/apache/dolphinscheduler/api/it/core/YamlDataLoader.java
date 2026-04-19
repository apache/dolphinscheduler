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

package org.apache.dolphinscheduler.api.it.core;

import org.apache.dolphinscheduler.common.utils.EncryptionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class YamlDataLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    private static final TypeReference<LinkedHashMap<String, List<Map<String, Object>>>> DOC_TYPE =
            new TypeReference<LinkedHashMap<String, List<Map<String, Object>>>>() {
            };

    private static final Pattern AUTO_MD5 = Pattern.compile("^<auto-md5:(.+)>$");
    private static final String NOW_MARKER = "<now>";

    private final TableEntityRegistry registry;

    public YamlDataLoader(TableEntityRegistry registry) {
        this.registry = registry;
    }

    public void load(List<String> classpathLocations) {
        for (String loc : classpathLocations) {
            Resource resource = RESOLVER.getResource(loc);
            if (!resource.exists()) {
                throw new IllegalArgumentException("YAML fixture not found: " + loc);
            }
            try (InputStream in = resource.getInputStream()) {
                loadFromInputStream(in, loc);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + loc, e);
            }
        }
    }

    public void loadFromString(String yaml) {
        try {
            loadFromInputStream(
                    new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                    "<inline>");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse inline YAML", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadFromInputStream(InputStream in, String source) throws IOException {
        LinkedHashMap<String, List<Map<String, Object>>> doc = YAML_MAPPER.readValue(in, DOC_TYPE);

        if (doc == null || doc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<Map<String, Object>>> e : doc.entrySet()) {
            String tableName = e.getKey();
            List<Map<String, Object>> rows = e.getValue();
            Class<?> entityClass;
            try {
                entityClass = registry.entityClassFor(tableName);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Unknown table '" + tableName + "' in " + source, ex);
            }
            BaseMapper mapper = registry.mapperFor(entityClass);

            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> rawRow = rows.get(i);
                preprocessMarkers(rawRow);
                Object entity = convertRow(entityClass, rawRow, source, tableName, i);
                try {
                    mapper.insert(entity);
                } catch (Exception ex) {
                    throw new IllegalStateException(
                            "Failed inserting row " + tableName + "[" + i + "] from " + source, ex);
                }
            }
        }
    }

    private Object convertRow(Class<?> entityClass, Map<String, Object> raw,
                              String source, String tableName, int index) {
        try {
            return YAML_MAPPER.convertValue(raw, entityClass);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed converting row " + tableName + "[" + index + "] from " + source
                            + ": " + ex.getMessage(),
                    ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void preprocessMarkers(Map<String, Object> row) {
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            Object replaced = processValue(v);
            if (replaced != v) {
                e.setValue(replaced);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object processValue(Object v) {
        if (v instanceof String) {
            String s = (String) v;
            if (NOW_MARKER.equals(s)) {
                return LocalDateTime.now().toString();
            }
            Matcher m = AUTO_MD5.matcher(s);
            if (m.matches()) {
                return EncryptionUtils.getMd5(m.group(1));
            }
            return v;
        }
        if (v instanceof Map) {
            preprocessMarkers((Map<String, Object>) v);
            return v;
        }
        if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, processValue(list.get(i)));
            }
            return v;
        }
        return v;
    }
}
