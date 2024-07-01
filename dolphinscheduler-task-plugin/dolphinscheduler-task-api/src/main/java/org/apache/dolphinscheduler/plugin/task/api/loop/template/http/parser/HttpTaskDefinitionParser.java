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

package org.apache.dolphinscheduler.plugin.task.api.loop.template.http.parser;

import org.apache.dolphinscheduler.common.utils.ClassFilterConstructor;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.LoopTaskYamlDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.TaskDefinitionParser;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.HttpLoopTaskDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskCancelTaskMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskQueryStatusMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskSubmitTaskMethodDefinition;

import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import lombok.NonNull;

import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Preconditions;

public class HttpTaskDefinitionParser implements TaskDefinitionParser<HttpLoopTaskDefinition> {

    @Override
    public @NonNull HttpLoopTaskDefinition parse(@NonNull String yamlConfigFile) {
        LoopTaskYamlDefinition loopTaskYamlDefinition;
        try {
            loopTaskYamlDefinition = parseYamlConfigFile(yamlConfigFile);
        } catch (IOException ex) {
            throw new IllegalArgumentException(String.format("Parse yaml file: %s error", yamlConfigFile), ex);
        }
        validateYamlDefinition(loopTaskYamlDefinition);

        LoopTaskYamlDefinition.LoopTaskServiceYamlDefinition service = loopTaskYamlDefinition.getService();
        LoopTaskYamlDefinition.LoopTaskAPIYamlDefinition api = service.getApi();
        HttpLoopTaskSubmitTaskMethodDefinition submitTaskMethod =
                new SubmitTemplateMethodTransformer().transform(api.getSubmit());
        HttpLoopTaskQueryStatusMethodDefinition queryTaskStateMethod =
                new QueryStateTemplateMethodTransformer().transform(api.getQueryState());
        HttpLoopTaskCancelTaskMethodDefinition cancelTaskMethod =
                new CancelTemplateMethodTransformer().transform(api.getCancel());
        return new HttpLoopTaskDefinition(service.getName(), submitTaskMethod, queryTaskStateMethod, cancelTaskMethod);
    }

    protected @NonNull LoopTaskYamlDefinition parseYamlConfigFile(@NonNull String yamlConfigFile) throws IOException {
        try (FileReader fileReader = new FileReader(yamlConfigFile)) {
            return new Yaml(new ClassFilterConstructor(new Class[]{
                    LoopTaskYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskServiceYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskAPIYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskSubmitMethodYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskQueryStateYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskCancelYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskMethodYamlDefinition.class,
                    LoopTaskYamlDefinition.LoopTaskQueryStateYamlDefinition.class,
                    Map.class,
                    String.class
            }))
                    .loadAs(fileReader, LoopTaskYamlDefinition.class);
        }
    }

    protected void validateYamlDefinition(@NonNull LoopTaskYamlDefinition loopTaskYamlDefinition) {
        LoopTaskYamlDefinition.LoopTaskServiceYamlDefinition service = loopTaskYamlDefinition.getService();
        Preconditions.checkNotNull(service, "service is null");
        Preconditions.checkNotNull(service.getName(), "service name is null");
        if (!StringUtils.equalsIgnoreCase(service.getType(), "http")) {
            throw new IllegalArgumentException(String.format("service type: %s is invalidated", service.getType()));
        }
    }
}
