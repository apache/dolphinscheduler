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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProcessUtils {

    public static final class ProcessExecutionResult {

        private final Integer exitCode;
        private final boolean timedOut;

        private ProcessExecutionResult(Integer exitCode, boolean timedOut) {
            this.exitCode = exitCode;
            this.timedOut = timedOut;
        }

        public Integer getExitCode() {
            return exitCode;
        }

        public boolean isTimedOut() {
            return timedOut;
        }

        static ProcessExecutionResult success(int exitCode) {
            return new ProcessExecutionResult(exitCode, false);
        }

        static ProcessExecutionResult timeout() {
            return new ProcessExecutionResult(null, true);
        }

        static ProcessExecutionResult error() {
            return new ProcessExecutionResult(null, false);
        }
    }

    private ProcessUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * executeScript with timeout
     *
    * @param timeoutSeconds timeout in seconds, must be greater than 0
     * @param cmd cmd params
     * @return execution result
     */
    static ProcessExecutionResult executeScript(long timeoutSeconds, String... cmd) {

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        Process process = null;
        StreamGobbler inputStreamGobbler = null;
        StreamGobbler errorStreamGobbler = null;
        try {
            process = processBuilder.start();
            inputStreamGobbler = new StreamGobbler(process.getInputStream());
            errorStreamGobbler = new StreamGobbler(process.getErrorStream());

            inputStreamGobbler.setDaemon(true);
            errorStreamGobbler.setDaemon(true);
            inputStreamGobbler.start();
            errorStreamGobbler.start();

            if (timeoutSeconds > 0) {
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!finished) {
                    log.error("script execution timed out after {} seconds, destroying process", timeoutSeconds);
                    process.destroyForcibly();
                    return ProcessExecutionResult.timeout();
                }
            } else {
                process.waitFor();
            }
            return ProcessExecutionResult.success(process.exitValue());
        } catch (IOException e) {
            log.error("execute alert script error {}", e.getMessage());
            return ProcessExecutionResult.error();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("execute alert script interrupted", e);
            return ProcessExecutionResult.error();
        } finally {
            closeProcessStreams(process);
            joinGobbler(inputStreamGobbler);
            joinGobbler(errorStreamGobbler);
        }
    }

    private static void closeProcessStreams(Process process) {
        if (Objects.isNull(process)) {
            return;
        }
        try {
            process.getOutputStream().close();
        } catch (IOException e) {
            log.warn("Failed to close process output stream", e);
        }
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
        if (gobbler == null) {
            return;
        }
        try {
            gobbler.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for stream gobbler to finish", e);
        }
    }
}
