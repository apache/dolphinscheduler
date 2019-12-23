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
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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
    public String queue = "queue1";


    @Before
    public void setUp() throws Exception {

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
        param.setQueue(queue);

        //Invoke buildArgs
        List<String> result = SparkArgsUtils.buildArgs(param);
        for (String s : result) {
            logger.info(s);
        }

        //Expected values and order
        assertEquals(result.size(),20);

        assertEquals(result.get(0),"--master");
        assertEquals(result.get(1),"yarn");

        assertEquals(result.get(2),"--deploy-mode");
        assertEquals(result.get(3),mode);

        assertEquals(result.get(4),"--class");
        assertEquals(result.get(5),mainClass);

        assertEquals(result.get(6),"--driver-cores");
        assertSame(Integer.valueOf(result.get(7)),driverCores);

        assertEquals(result.get(8),"--driver-memory");
        assertEquals(result.get(9),driverMemory);

        assertEquals(result.get(10),"--num-executors");
        assertSame(Integer.valueOf(result.get(11)),numExecutors);

        assertEquals(result.get(12),"--executor-cores");
        assertSame(Integer.valueOf(result.get(13)),executorCores);

        assertEquals(result.get(14),"--executor-memory");
        assertEquals(result.get(15),executorMemory);

        assertEquals(result.get(16),"--queue");
        assertEquals(result.get(17),queue);
        assertEquals(result.get(18),mainJar.getRes());
        assertEquals(result.get(19),mainArgs);

        //Others param without --queue
        SparkParameters param1 = new SparkParameters();
        param1.setOthers("--files xxx/hive-site.xml");
        param1.setQueue(queue);
        result = SparkArgsUtils.buildArgs(param1);
        assertEquals(result.size(),7);
    }
}