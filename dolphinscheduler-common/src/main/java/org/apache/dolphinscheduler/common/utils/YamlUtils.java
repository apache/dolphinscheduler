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

package org.apache.dolphinscheduler.common.utils;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * YAML Utilities
 */
@Slf4j
public class YamlUtils {

    // YAML parser
    private static final ObjectMapper objectMapper = YAMLMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build();

    // ensure Singleton Pattern of `YamlUtils`
    private YamlUtils() {
        throw new UnsupportedOperationException("Construct YamlUtils");
    }

    /**
     * parse the YAML String
     *
     * @param yamlString YAML string to load
     * @param typeReference the type reference specifying the type of the object to parse into
     * @param <T> the type of the object
     * @return an object of type T parsed from the YAML file, or null if parsing fails
     */
    public static <T> T load(String yamlString, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(yamlString, typeReference);
        } catch (Exception exception) {
            log.error("failed to parse YAML String ({}):" + "\n" +
                    "```yaml" + "\n" +
                    "{}" + "\n" +
                    "```" + "\n" +
                    "\n",
                    exception.getMessage(), yamlString);
            return null;
        }
    }

    /**
     * Loads and parses a YAML file into an object of the specified class.
     *
     * @param file YAML file to load
     * @param typeReference the type reference specifying the type of the object to parse into
     * @param <T> the type of the object
     * @return an object of type T parsed from the YAML file, or null if parsing fails
     */
    public static <T> T load(File file, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(file, typeReference);
        } catch (Exception exception) {
            log.error("failed to parse YAML file `{}`: {}", file.getName(), exception.getMessage());
            return null;
        }
    }
}
