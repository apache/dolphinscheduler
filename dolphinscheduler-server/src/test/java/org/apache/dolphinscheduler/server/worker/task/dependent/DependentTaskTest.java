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
package org.apache.dolphinscheduler.server.worker.task.dependent;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependentTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DependentTaskTest.class);


    @Test
    public void testDependInit() throws Exception{

        TaskProps taskProps = new TaskProps();

        String dependString = "{\n" +
                "\"dependTaskList\":[\n" +
                "    {\n" +
                "        \"dependItemList\":[\n" +
                "            {\n" +
                "                    \"definitionId\": 101,\n" +
                "                    \"depTasks\": \"ALL\",\n" +
                "                    \"cycle\": \"day\",\n" +
                "                    \"dateValue\": \"last1Day\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"relation\": \"AND\"\n" +
                "    }\n" +
                "    ],\n" +
                "\"relation\":\"OR\"\n" +
                "}";

        taskProps.setTaskInstId(252612);
        taskProps.setDependence(dependString);
        DependentTask dependentTask = new DependentTask(taskProps, logger);
        dependentTask.init();
        dependentTask.handle();
        Assert.assertEquals(dependentTask.getExitStatusCode(), Constants.EXIT_CODE_FAILURE );
    }



}