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

package org.apache.dolphinscheduler;

import org.apache.dolphinscheduler.poc.YarnClientMoc;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YarnClientAspectMocTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final PrintStream standardOut = System.out;
    ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();

    @BeforeEach
    public void beforeEveryTest() {
        System.setOut(new PrintStream(stdoutStream));
    }

    @AfterEach
    public void afterEveryTest() throws IOException {
        System.setOut(standardOut);
        stdoutStream.close();
    }

    @Test
    void testMoc() {
        YarnClientMoc moc = new YarnClientMoc();
        try {
            ApplicationSubmissionContext appContext = ApplicationSubmissionContext.newInstance(
                    ApplicationId.newInstance(System.currentTimeMillis(), 1236), "appName",
                    "queue", Priority.UNDEFINED,
                    null, false,
                    false, 10, null,
                    "type");
            moc.createAppId();
            ApplicationId applicationId = moc.submitApplication(appContext);
            String stdoutContent = stdoutStream.toString();
            Assertions.assertTrue(stdoutContent.contains("YarnClientAspectMoc[submitApplication]"),
                    "trigger YarnClientAspectMoc.submitApplication failed");
            Assertions.assertTrue(stdoutContent.contains("YarnClientAspectMoc[createAppId]:"),
                    "trigger YarnClientAspectMoc.createAppId failed");
        } catch (YarnException | IOException e) {
            logger.error("test YarnClientAspectMoc failed", e);
        }
    }
}
