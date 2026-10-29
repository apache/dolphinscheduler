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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseShellInterceptor implements IShellInterceptor {

    /**
     * The value of a {@code -D} option which carries a credential, the command is logged with the
     * value masked instead of leaking the secret of the task into the log file.
     */
    private static final Pattern SENSITIVE_OPTION_PATTERN = Pattern.compile(
            "(-D[^\\s=]*(?i:password|passwd|secret|token|credential)[^\\s=]*=)((?:'[^']*'|\"[^\"]*\"|\\S)+)");

    protected final String workingDirectory;
    protected final List<String> executeCommands;

    protected BaseShellInterceptor(List<String> executeCommands, String workingDirectory) {
        this.executeCommands = executeCommands;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Process execute() throws IOException {
        // init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(workingDirectory));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);
        processBuilder.command(executeCommands);
        log.info("Executing shell command : {}", maskSensitiveOptions(String.join(" ", executeCommands)));
        return processBuilder.start();
    }

    /**
     * Mask the value of the options which carry a credential, so that the command can be logged
     * without writing the secret into the log file.
     */
    static String maskSensitiveOptions(String command) {
        return SENSITIVE_OPTION_PATTERN.matcher(command).replaceAll("$1******");
    }

}
