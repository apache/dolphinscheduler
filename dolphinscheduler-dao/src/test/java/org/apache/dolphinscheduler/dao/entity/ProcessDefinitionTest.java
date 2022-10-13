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

package org.apache.dolphinscheduler.dao.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProcessDefinitionTest {

    /**
     * task instance sub process
     */
    @Test
    public void getGlobalParamMapTest() {
        ProcessDefinition taskInstance = new ProcessDefinition();

        //sub process
        taskInstance.setGlobalParams("[{\"prop\":\"selenium_global_parameters_1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"selenium_global_parameters_value_1\"}]");

        taskInstance.getGlobalParamMap();
        Assertions.assertEquals("{selenium_global_parameters_1=selenium_global_parameters_value_1}",taskInstance.getGlobalParamMap().toString());

    }
}
