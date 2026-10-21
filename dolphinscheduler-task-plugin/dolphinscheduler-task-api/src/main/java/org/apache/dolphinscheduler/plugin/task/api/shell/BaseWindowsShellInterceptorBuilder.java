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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseWindowsShellInterceptorBuilder<T extends BaseWindowsShellInterceptorBuilder<T, Y>, Y extends BaseShellInterceptor>
        extends
            BaseShellInterceptorBuilder<T, Y> {

    protected void generateShellScript() throws IOException {
        List<String> finalScripts = new ArrayList<>();
        // add shell header
        finalScripts.add(shellHeader());
        finalScripts.add("cd /d %~dp0");
        // add system env
        finalScripts.addAll(systemEnvScript());
        // add custom env
        finalScripts.addAll(customEnvScript());
        // add k8s config
        finalScripts.addAll(k8sConfig());
        // add shell body
        finalScripts.add(shellBody());
        // create shell file
        String finalScript = finalScripts.stream().collect(Collectors.joining(System.lineSeparator()));
        Path shellAbsolutePath = shellAbsolutePath();
        FileUtils.createFileWith755(shellAbsolutePath);
        Files.write(shellAbsolutePath, finalScript.getBytes(), StandardOpenOption.APPEND);
        log.info("Final Shell file is : \n{}", finalScript);
    }

    private String shellBody() {
        if (CollectionUtils.isEmpty(scripts)) {
            return StringUtils.EMPTY;
        }
        String scriptBody = scripts
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
        return ParameterUtils.convertParameterPlaceholders(scriptBody, propertyMap);
    }

    private Collection<String> k8sConfig() {
        log.warn("k8s config is not supported in windows");
        return Collections.emptyList();
    }

    protected List<String> generateBootstrapCommand() {
        if (sudoEnable) {
            log.warn("sudo is not supported in windows");
        }
        // todo: support tenant in widnows
        List<String> bootstrapCommand = new ArrayList<>();
        bootstrapCommand.add(shellInterpreter());
        bootstrapCommand.add(shellAbsolutePath().toString());
        return bootstrapCommand;
    }

    protected abstract String shellHeader();

    protected abstract String shellInterpreter();

    protected abstract String shellExtension();

    private List<String> systemEnvScript() {
        if (CollectionUtils.isEmpty(systemEnvs)) {
            return Collections.emptyList();
        }
        return systemEnvs.stream()
                .map(systemEnv -> "call " + systemEnv)
                .collect(Collectors.toList());
    }

    private List<String> customEnvScript() {
        if (CollectionUtils.isEmpty(customEnvScripts)) {
            return Collections.emptyList();
        }
        return customEnvScripts;
    }

    private Path shellAbsolutePath() {
        return Paths.get(shellDirectory, shellName + shellExtension());
    }
}
