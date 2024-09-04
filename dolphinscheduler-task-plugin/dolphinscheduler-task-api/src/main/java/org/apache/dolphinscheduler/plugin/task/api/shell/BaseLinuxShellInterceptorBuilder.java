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
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.AbstractCommandExecutorConstants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseLinuxShellInterceptorBuilder<T extends BaseLinuxShellInterceptorBuilder<T, Y>, Y extends BaseShellInterceptor>
        extends
            BaseShellInterceptorBuilder<T, Y> {

    protected void generateShellScript() throws IOException {
        List<String> finalScripts = new ArrayList<>();
        // add shell header
        finalScripts.add(shellHeader());
        finalScripts.add("BASEDIR=$(cd `dirname $0`; pwd)");
        finalScripts.add("cd $BASEDIR");
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
        log.info(
                "Final Shell file is: \n****************************** Script Content *****************************************************************\n"
                        +
                        "{}" +
                        "\n****************************** Script Content *****************************************************************\n",
                finalScript);
    }

    protected List<String> generateBootstrapCommand() {
        if (sudoEnable) {
            return bootstrapCommandInSudoMode();
        }
        return bootstrapCommandInNormalMode();
    }

    protected abstract String shellHeader();

    protected abstract String shellInterpreter();

    protected abstract String shellExtension();

    private List<String> systemEnvScript() {
        if (CollectionUtils.isEmpty(systemEnvs)) {
            return Collections.emptyList();
        }
        return systemEnvs
                .stream()
                .map(systemEnv -> "source " + systemEnv).collect(Collectors.toList());
    }

    private List<String> customEnvScript() {
        if (CollectionUtils.isEmpty(customEnvScripts)) {
            return Collections.emptyList();
        }
        return customEnvScripts;
    }

    private List<String> k8sConfig() throws IOException {
        if (StringUtils.isEmpty(k8sConfigYaml)) {
            return Collections.emptyList();
        }
        Path kubeConfigPath = Paths.get(FileUtils.getKubeConfigPath(shellDirectory));
        FileUtils.createFileWith755(kubeConfigPath);
        Files.write(kubeConfigPath, k8sConfigYaml.getBytes(), StandardOpenOption.APPEND);
        log.info("Created kubernetes configuration file: {}.", kubeConfigPath);
        return Collections.singletonList("export KUBECONFIG=" + kubeConfigPath);
    }

    private String shellBody() {
        if (CollectionUtils.isEmpty(scripts)) {
            return StringUtils.EMPTY;
        }
        String scriptBody = scripts
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
        scriptBody = scriptBody.replaceAll("\\r\\n", System.lineSeparator());
        return ParameterUtils.convertParameterPlaceholders(scriptBody, propertyMap);
    }

    private Path shellAbsolutePath() {
        return Paths.get(shellDirectory, shellName + shellExtension());
    }

    private List<String> bootstrapCommandInSudoMode() {
        if (PropertyUtils.getBoolean(AbstractCommandExecutorConstants.TASK_RESOURCE_LIMIT_STATE, false)) {
            return bootstrapCommandInResourceLimitMode();
        }
        List<String> bootstrapCommand = new ArrayList<>();
        bootstrapCommand.add("sudo");
        if (StringUtils.isNotBlank(runUser)) {
            bootstrapCommand.add("-u");
            bootstrapCommand.add(runUser);
        }
        bootstrapCommand.add("-i");
        bootstrapCommand.add(shellAbsolutePath().toString());
        return bootstrapCommand;
    }

    private List<String> bootstrapCommandInNormalMode() {
        List<String> bootstrapCommand = new ArrayList<>();
        bootstrapCommand.add(shellInterpreter());
        bootstrapCommand.add(shellAbsolutePath().toString());
        return bootstrapCommand;
    }

    private List<String> bootstrapCommandInResourceLimitMode() {
        List<String> bootstrapCommand = new ArrayList<>();
        bootstrapCommand.add("sudo");
        bootstrapCommand.add("systemd-run");
        bootstrapCommand.add("-q");
        bootstrapCommand.add("--scope");

        if (cpuQuota == -1) {
            bootstrapCommand.add("-p");
            bootstrapCommand.add("CPUQuota=");
        } else {
            bootstrapCommand.add("-p");
            bootstrapCommand.add(String.format("CPUQuota=%s%%", cpuQuota));
        }

        // use `man systemd.resource-control` to find available parameter
        if (memoryQuota == -1) {
            bootstrapCommand.add("-p");
            bootstrapCommand.add(String.format("MemoryLimit=%s", "infinity"));
        } else {
            bootstrapCommand.add("-p");
            bootstrapCommand.add(String.format("MemoryLimit=%sM", memoryQuota));
        }

        bootstrapCommand.add(String.format("--uid=%s", runUser));
        return bootstrapCommand;
    }
}
