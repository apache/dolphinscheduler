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

package org.apache.dolphinscheduler.server.master.integration;

import java.io.InputStream;

import lombok.SneakyThrows;

import org.yaml.snakeyaml.Yaml;

public class YamlFactory {

    @SneakyThrows
    public static WorkflowTestCaseContext load(final String yamlRelativePath) {
        final Yaml yaml = new Yaml();
        try (InputStream fis = YamlFactory.class.getResourceAsStream(yamlRelativePath)) {
            if (fis == null) {
                throw new IllegalArgumentException("Cannot find the file: " + yamlRelativePath + " under classpath");
            }
            return yaml.loadAs(fis, WorkflowTestCaseContext.class);
        }
    }

}
