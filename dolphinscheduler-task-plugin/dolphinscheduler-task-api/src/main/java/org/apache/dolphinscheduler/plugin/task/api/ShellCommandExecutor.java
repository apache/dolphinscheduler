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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ShellUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;

/**
 * shell command executor
 */
public class ShellCommandExecutor extends AbstractCommandExecutor {

    /**
     * For Unix-like, using bash
     */
    private static final String SH = "bash";

    /**
     * For Windows, using cmd.exe
     */
    private static final String CMD = "cmd.exe";

    /**
     * constructor
     *
     * @param logHandler logHandler
     * @param taskRequest taskRequest
     * @param logger logger
     */
    public ShellCommandExecutor(Consumer<LinkedBlockingQueue<String>> logHandler,
                                TaskExecutionContext taskRequest,
                                Logger logger) {
        super(logHandler, taskRequest, logger);
    }

    public ShellCommandExecutor(LinkedBlockingQueue<String> logBuffer) {
        super(logBuffer);
    }

    @Override
    protected String buildCommandFilePath() {
        // command file
        return String.format("%s/%s.%s", taskRequest.getExecutePath(), taskRequest.getTaskAppId(),
                SystemUtils.IS_OS_WINDOWS ? "bat" : "command");
    }

    /**
     * create command file if not exists
     *
     * @param execCommand exec command
     * @param commandFile command file
     * @throws IOException io exception
     */
    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        // create if non existence
        logger.info("Begin to create command file:{}", commandFile);

        Path commandFilePath = Paths.get(commandFile);
        if (Files.exists(commandFilePath)) {
            logger.warn("The command file: {} is already exist, will not create a again", commandFile);
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (SystemUtils.IS_OS_WINDOWS) {
            sb.append("@echo off").append(System.lineSeparator());
            sb.append("cd /d %~dp0").append(System.lineSeparator());
            if (CollectionUtils.isNotEmpty(ShellUtils.ENV_SOURCE_LIST)) {
                for (String envSourceFile : ShellUtils.ENV_SOURCE_LIST) {
                    sb.append("call ").append(envSourceFile).append("\n");
                }
            }
            if (StringUtils.isNotBlank(taskRequest.getEnvironmentConfig())) {
                sb.append(taskRequest.getEnvironmentConfig()).append(System.lineSeparator());
            }
        } else {
            sb.append("#!/bin/bash").append(System.lineSeparator());
            sb.append("BASEDIR=$(cd `dirname $0`; pwd)").append(System.lineSeparator());
            sb.append("cd $BASEDIR").append(System.lineSeparator());
            if (CollectionUtils.isNotEmpty(ShellUtils.ENV_SOURCE_LIST)) {
                for (String envSourceFile : ShellUtils.ENV_SOURCE_LIST) {
                    sb.append("source ").append(envSourceFile).append("\n");
                }
            }
            if (StringUtils.isNotBlank(taskRequest.getEnvironmentConfig())) {
                sb.append(taskRequest.getEnvironmentConfig()).append(System.lineSeparator());
            }
            if (Objects.nonNull(taskRequest.getK8sTaskExecutionContext())) {
                String configYaml = taskRequest.getK8sTaskExecutionContext().getConfigYaml();
                Path kubeConfigPath = Paths.get(org.apache.dolphinscheduler.common.utils.FileUtils
                        .getKubeConfigPath(taskRequest.getExecutePath()));
                FileUtils.createFileWith755(kubeConfigPath);
                Files.write(kubeConfigPath, configYaml.getBytes(), StandardOpenOption.APPEND);
                sb.append("export KUBECONFIG=" + kubeConfigPath).append(System.lineSeparator());
                logger.info("Create kubernetes configuration file: {}.", kubeConfigPath);
            }
        }
        sb.append(execCommand);
        String commandContent = sb.toString();

        FileUtils.createFileWith755(commandFilePath);
        Files.write(commandFilePath, commandContent.getBytes(), StandardOpenOption.APPEND);

        logger.info("Success create command file, command: {}", commandContent);
    }

    @Override
    protected String commandInterpreter() {
        return SystemUtils.IS_OS_WINDOWS ? CMD : SH;
    }

}
