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
package org.apache.dolphinscheduler.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * StreamGobbler
 */
public class AsyncStreamThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(AsyncStreamThread.class);

    private InputStream inputStream;

    public AsyncStreamThread(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = null;
        BufferedReader inputBufferReader = null;

        try {
            inputStreamReader = new InputStreamReader(inputStream);
            inputBufferReader = new BufferedReader(inputStreamReader);
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
        } finally {
            try {
                if (inputBufferReader!=null){
                    inputBufferReader.close();
                }
                if (inputStreamReader!=null){
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                logger.error("I/O error occurs {}", e.getMessage());
            }
        }
    }

}
