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

package org.apache.dolphinscheduler.plugin.task.datax;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataxParametersTest {

    /**
     * jvm parameters
     */
    public static final String JVM_PARAM = " --jvm=\"-Xms%sG -Xmx%sG\" ";

    @Test
    public void testLoadJvmEnv() {

        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setXms(0);
        dataxParameters.setXmx(-100);

        String actual = loadJvmEnvTest(dataxParameters);

        String except = " --jvm=\"-Xms1G -Xmx1G\" ";
        Assertions.assertEquals(except, actual);

        dataxParameters.setXms(13);
        dataxParameters.setXmx(14);
        actual = loadJvmEnvTest(dataxParameters);
        except = " --jvm=\"-Xms13G -Xmx14G\" ";
        Assertions.assertEquals(except, actual);

    }

    @Test
    public void testToString() {

        DataxParameters dataxParameters = new DataxParameters();
        List<ResourceInfo> resourceInfoList = new ArrayList<>();
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setId(2);
        resourceInfo.setResourceName("/hdfs.keytab");
        resourceInfoList.add(resourceInfo);

        dataxParameters.setCustomConfig(0);
        dataxParameters.setXms(0);
        dataxParameters.setXmx(-100);
        dataxParameters.setDataSource(1);
        dataxParameters.setDataTarget(1);
        dataxParameters.setDsType("MYSQL");
        dataxParameters.setDtType("MYSQL");
        dataxParameters.setJobSpeedByte(1);
        dataxParameters.setJobSpeedRecord(1);
        dataxParameters.setJson("json");
        dataxParameters.setResourceList(resourceInfoList);

        String expected = "DataxParameters"
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
                + "xms=0, "
                + "xmx=-100, "
                + "resourceList=[{\"id\":2,\"resourceName\":\"/hdfs.keytab\",\"res\":null}]"
                + "}";

        Assertions.assertEquals(expected, dataxParameters.toString());
    }

    public String loadJvmEnvTest(DataxParameters dataXParameters) {
        int xms = dataXParameters.getXms() < 1 ? 1 : dataXParameters.getXms();
        int xmx = dataXParameters.getXmx() < 1 ? 1 : dataXParameters.getXmx();
        return String.format(JVM_PARAM, xms, xmx);
    }
}
