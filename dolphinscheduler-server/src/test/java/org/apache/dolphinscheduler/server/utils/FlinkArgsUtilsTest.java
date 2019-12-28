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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test FlinkArgsUtils
 */
public class FlinkArgsUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(FlinkArgsUtilsTest.class);

    public String mode = "cluster";
    public int slot = 2;
    public String appName = "testFlink";
    public int taskManager = 4;
    public String taskManagerMemory = "2G";
    public String jobManagerMemory = "4G";
    public ProgramType programType = ProgramType.JAVA;
    public String mainClass = "com.test";
    public ResourceInfo mainJar = null;
    public String mainArgs = "testArgs";
    public String queue = "queue1";
    public String others = "--input file:///home";


    @Before
    public void setUp() throws Exception {

        ResourceInfo main = new ResourceInfo();
        main.setRes("testflink-1.0.0-SNAPSHOT.jar");
        mainJar = main;
    }

    /**
     * Test buildArgs
     */
    @Test
    public void testBuildArgs() {

        //Define params
        FlinkParameters param = new FlinkParameters();
        param.setDeployMode(mode);
        param.setMainClass(mainClass);
        param.setAppName(appName);
        param.setSlot(slot);
        param.setTaskManager(taskManager);
        param.setJobManagerMemory(jobManagerMemory);
        param.setTaskManagerMemory(taskManagerMemory);
        param.setMainJar(mainJar);
        param.setProgramType(programType);
        param.setMainArgs(mainArgs);
        param.setQueue(queue);
        param.setOthers(others);

        //Invoke buildArgs
        List<String> result = FlinkArgsUtils.buildArgs(param);
        for (String s : result) {
            logger.info(s);
        }

        //Expected values and order
        assertEquals(result.size(),20);

        assertEquals(result.get(0),"-m");
        assertEquals(result.get(1),"yarn-cluster");

        assertEquals(result.get(2),"-ys");
        assertSame(Integer.valueOf(result.get(3)),slot);

        assertEquals(result.get(4),"-ynm");
        assertEquals(result.get(5),appName);

        assertEquals(result.get(6),"-yn");
        assertSame(Integer.valueOf(result.get(7)),taskManager);

        assertEquals(result.get(8),"-yjm");
        assertEquals(result.get(9),jobManagerMemory);

        assertEquals(result.get(10),"-ytm");
        assertEquals(result.get(11),taskManagerMemory);

        assertEquals(result.get(12),"-d");

        assertEquals(result.get(13),"-c");
        assertEquals(result.get(14),mainClass);

        assertEquals(result.get(15),mainJar.getRes());
        assertEquals(result.get(16),mainArgs);

        assertEquals(result.get(17),"--qu");
        assertEquals(result.get(18),queue);

        assertEquals(result.get(19),others);

        //Others param without --qu
        FlinkParameters param1 = new FlinkParameters();
        param1.setQueue(queue);
        param1.setDeployMode(mode);
        result = FlinkArgsUtils.buildArgs(param1);
        assertEquals(result.size(),5);

    }
}