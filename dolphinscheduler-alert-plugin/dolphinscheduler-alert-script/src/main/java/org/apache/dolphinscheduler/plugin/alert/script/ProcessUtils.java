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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ProcessUtils
 */
public class ProcessUtils {


    public static Integer executeScript(String... cmd) {

        int exitCode = -1;
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            Process process = processBuilder.start();

            InputStream in = process.getErrorStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();

            StreamGobbler inputStreamGobbler =
                    new StreamGobbler(process.getInputStream());
            StreamGobbler errorStreamGobbler =
                    new StreamGobbler(process.getErrorStream());

            inputStreamGobbler.start();
            errorStreamGobbler.start();
            return process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return exitCode;
    }
}
