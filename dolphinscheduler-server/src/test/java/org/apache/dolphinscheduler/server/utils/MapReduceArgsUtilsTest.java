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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test MapReduceArgsUtils
 */
public class MapReduceArgsUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(MapReduceArgsUtilsTest.class);

    public String mainClass = "com.examples.WordCount";
    public ResourceInfo mainJar = null;
    public String mainArgs = "/user/joe/wordcount/input /user/joe/wordcount/output -skip /user/joe/wordcount/patterns.txt";
    public ProgramType programType = ProgramType.JAVA;
    public String others = "-files cachefile.txt -libjars mylib.jar -archives myarchive.zip -Dwordcount.case.sensitive=false";
    public String appName = "mapreduce test";
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
        MapReduceParameters param = new MapReduceParameters();
        param.setMainClass(mainClass);
        param.setMainJar(mainJar);
        param.setMainArgs(mainArgs);
        param.setProgramType(programType);
        param.setOthers(others);
        param.setAppName(appName);
        param.setQueue(queue);

        //Invoke buildArgs
        List<String> result = MapReduceArgsUtils.buildArgs(param);
        for (String s : result) {
            logger.info(s);
        }

        //Expected values and order
        assertEquals(7, result.size());

        assertEquals("jar", result.get(0));
        assertEquals(mainJar.getRes(), result.get(1));
        assertEquals(mainClass, result.get(2));
        assertEquals(String.format("-D%s=%s", Constants.MR_NAME, ArgsUtils.escape(appName)), result.get(3));
        assertEquals(String.format("-D%s=%s", Constants.MR_QUEUE, queue), result.get(4));
        assertEquals(others, result.get(5));
        assertEquals(mainArgs, result.get(6));

        //Others param without --queue
        param.setOthers("-files xxx/hive-site.xml");
        param.setQueue(null);
        result = MapReduceArgsUtils.buildArgs(param);
        assertEquals(6, result.size());
    }

}
