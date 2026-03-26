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
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProcessUtils {

    static final int EXECUTE_ERROR_EXIT_CODE = 125;
    static final int EXECUTE_TIMEOUT_EXIT_CODE = 124;

    private ProcessUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * executeScript with timeout
     *
     * @param timeoutSeconds timeout in seconds, if <= 0 waits indefinitely
     * @param cmd cmd params
    * @return exit code, 125 if internal error, 124 if timeout
     */
    static Integer executeScript(long timeoutSeconds, String... cmd) {

        int exitCode = EXECUTE_ERROR_EXIT_CODE;

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            Process process = processBuilder.start();
            StreamGobbler inputStreamGobbler = new StreamGobbler(process.getInputStream());
            StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream());

            inputStreamGobbler.start();
            errorStreamGobbler.start();

            if (timeoutSeconds > 0) {
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!finished) {
                    log.error("script execution timed out after {} seconds, destroying process", timeoutSeconds);
                    process.destroyForcibly();
                    closeProcessStreams(process);
                    joinGobbler(inputStreamGobbler);
                    joinGobbler(errorStreamGobbler);
                    return EXECUTE_TIMEOUT_EXIT_CODE;
                }
            } else {
                process.waitFor();
            }
            int processExitCode = process.exitValue();
            joinGobbler(inputStreamGobbler);
            joinGobbler(errorStreamGobbler);
            return processExitCode;
        } catch (InterruptedException e) {
            log.error("execute alert script interrupted {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("execute alert script error {}", e.getMessage());
        }

        return exitCode;
    }

    private static void closeProcessStreams(Process process) {
        try {
            process.getInputStream().close();
        } catch (IOException e) {
            log.warn("Failed to close process input stream after timeout", e);
        }
        try {
            process.getErrorStream().close();
        } catch (IOException e) {
            log.warn("Failed to close process error stream after timeout", e);
        }
    }

    private static void joinGobbler(StreamGobbler gobbler) {
        try {
            gobbler.interrupt();
            gobbler.join(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
