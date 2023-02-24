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

package org.apache.dolphinscheduler.plugin.task.chunjun;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChunJunParametersTest {

    private ChunJunParameters chunJunParameters = new ChunJunParameters();

    @BeforeEach
    public void setUp() {
        chunJunParameters.setCustomConfig(0);
        chunJunParameters.setDataSource(1);
        chunJunParameters.setDataTarget(1);
        chunJunParameters.setDsType("MYSQL");
        chunJunParameters.setDtType("MYSQL");
        chunJunParameters.setJobSpeedByte(1);
        chunJunParameters.setJobSpeedRecord(1);
        chunJunParameters.setJson("json");
    }

    @Test
    public void testToString() {

        String expected = "ChunJunParameters"
                + "{"
                + "customConfig=0, "
                + "json='json', "
                + "dsType='MYSQL', "
                + "dataSource=1, "
                + "dtType='MYSQL', "
                + "dataTarget=1, "
                + "sql='null', "
                + "targetTable='null', "
                + "preStatements=null, "
                + "postStatements=null, "
                + "jobSpeedByte=1, "
                + "jobSpeedRecord=1, "
                + "others=xx, "
                + "deployMode=local"
                + "}";

        Assertions.assertNotEquals(expected, chunJunParameters.toString());
    }

    @Test
    public void testCheckParameters() {
        Assertions.assertFalse(chunJunParameters.checkParameters());
    }

    @Test
    public void testGetResourceFilesList() {
        Assertions.assertNotNull(chunJunParameters.getResourceFilesList());
    }

    @Test
    public void testGetResources() {
        Assertions.assertNotNull(chunJunParameters.getResources());
    }
}
