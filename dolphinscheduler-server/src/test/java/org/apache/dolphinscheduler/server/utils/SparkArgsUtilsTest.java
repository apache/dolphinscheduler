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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test SparkArgsUtils
 */
public class SparkArgsUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(SparkArgsUtilsTest.class);

    public String mode = "cluster";
    public String mainClass = "com.test";
    public ResourceInfo mainJar = null;
    public String mainArgs = "partitions=2";
    public String driverMemory = "2G";
    public String executorMemory = "4G";
    public ProgramType programType = ProgramType.JAVA;
    public int driverCores = 2;
    public int executorCores = 6;
    public String sparkVersion = "SPARK1";
    public int numExecutors = 4;
    public String appName = "spark test";
    public String queue = "queue1";

    @Before
    public void setUp() {
        ResourceInfo main = new ResourceInfo();
        main.setRes("testspark-1.0.0-SNAPSHOT.jar");
        mainJar = main;
    }

    /**
     * Test buildArgs
     */
    @Test
    public void testBuildArgs() {
        //Define params
        SparkParameters param = new SparkParameters();
        param.setDeployMode(mode);
        param.setMainClass(mainClass);
        param.setDriverCores(driverCores);
        param.setDriverMemory(driverMemory);
        param.setExecutorCores(executorCores);
        param.setExecutorMemory(executorMemory);
        param.setMainJar(mainJar);
        param.setNumExecutors(numExecutors);
        param.setProgramType(programType);
        param.setSparkVersion(sparkVersion);
        param.setMainArgs(mainArgs);
        param.setAppName(appName);
        param.setQueue(queue);

        //Invoke buildArgs
        List<String> result = SparkArgsUtils.buildArgs(param);
        for (String s : result) {
            logger.info(s);
        }

        //Expected values and order
        assertEquals(22, result.size());

        assertEquals("--master", result.get(0));
        assertEquals("yarn", result.get(1));

        assertEquals("--deploy-mode", result.get(2));
        assertEquals(mode, result.get(3));

        assertEquals("--class", result.get(4));
        assertEquals(mainClass, result.get(5));

        assertEquals("--driver-cores", result.get(6));
        assertSame(driverCores, Integer.valueOf(result.get(7)));

        assertEquals("--driver-memory", result.get(8));
        assertEquals(driverMemory, result.get(9));

        assertEquals("--num-executors", result.get(10));
        assertSame(numExecutors, Integer.valueOf(result.get(11)));

        assertEquals("--executor-cores", result.get(12));
        assertSame(executorCores, Integer.valueOf(result.get(13)));

        assertEquals("--executor-memory", result.get(14));
        assertEquals(executorMemory, result.get(15));

        assertEquals("--name", result.get(16));
        assertEquals(ArgsUtils.escape(appName), result.get(17));

        assertEquals("--queue", result.get(18));
        assertEquals(queue, result.get(19));

        assertEquals(mainJar.getRes(), result.get(20));
        assertEquals(mainArgs, result.get(21));

        //Others param without --queue
        SparkParameters param1 = new SparkParameters();
        param1.setOthers("--files xxx/hive-site.xml");
        param1.setQueue(queue);
        result = SparkArgsUtils.buildArgs(param1);
        assertEquals(7, result.size());
    }

}