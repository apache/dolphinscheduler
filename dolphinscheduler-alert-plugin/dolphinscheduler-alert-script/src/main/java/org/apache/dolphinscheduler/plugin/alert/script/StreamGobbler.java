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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StreamGobbler
 */
public class StreamGobbler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    private InputStream inputStream;

    StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader inputBufferReader = new BufferedReader(inputStreamReader);

        try {
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = inputBufferReader.readLine()) != null) {
                output.append(line);
                output.append(System.getProperty("line.separator"));
            }
            if (output.length() > 0) {
                logger.info("out put msg is{}", output);
            }
        } catch (IOException e) {
            logger.error("I/O error occurs {}", e.getMessage());
        }
    }

}
