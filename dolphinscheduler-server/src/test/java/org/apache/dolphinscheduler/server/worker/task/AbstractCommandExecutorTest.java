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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class AbstractCommandExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCommandExecutorTest.class);

    private ShellCommandExecutor shellCommandExecutor;

    @Before
    public void before() throws Exception {
        System.setProperty("log4j2.disable.jmx", Boolean.TRUE.toString());
        shellCommandExecutor = new ShellCommandExecutor(null);
    }

    @Test
    public void testSetTaskResultString() {
        shellCommandExecutor.setTaskResultString("shellReturn");
    }

    @Test
    public void testGetTaskResultString() {
        logger.info(shellCommandExecutor.getTaskResultString());
    }
}