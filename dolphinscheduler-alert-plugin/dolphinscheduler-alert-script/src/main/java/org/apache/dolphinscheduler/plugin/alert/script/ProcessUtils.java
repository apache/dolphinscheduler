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

package org.apache.dolphinscheduler.plugin.alert.script;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessUtils
 */
public class ProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * executeScript
     *
     * @param cmd cmd params
     * @return exit code
     */
    static Integer executeScript(String... cmd) {

        int exitCode = -1;

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            Process process = processBuilder.start();
            StreamGobbler inputStreamGobbler = new StreamGobbler(process.getInputStream());
            StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream());

            inputStreamGobbler.start();
            errorStreamGobbler.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("execute alert script error {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        return exitCode;
    }
}
