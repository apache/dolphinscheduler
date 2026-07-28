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
    public void testCheckParametersWithCustomConfig() {
        DataxParameters withInlineJson = new DataxParameters();
        withInlineJson.setCustomConfig(1);
        withInlineJson.setJson("{\"job\":{}}");
        Assertions.assertTrue(withInlineJson.checkParameters());

        // a blank json field or any semantically empty JSON object is no inline
        // definition: invalid without a resource file, valid with one because the
        // resource then carries the job definition (issue #18389)
        String[] absentJsonVariants = {null, "", "   ", "{}", "{ }", "{\n\n}", " { } "};
        for (String variant : absentJsonVariants) {
            DataxParameters withoutResource = new DataxParameters();
            withoutResource.setCustomConfig(1);
            withoutResource.setJson(variant);
            Assertions.assertTrue(withoutResource.isInlineJsonAbsent(),
                    "expected inline json to be absent for: [" + variant + "]");
            Assertions.assertFalse(withoutResource.checkParameters(),
                    "expected invalid without resource for json: [" + variant + "]");

            DataxParameters withResource = new DataxParameters();
            withResource.setCustomConfig(1);
            withResource.setJson(variant);
            withResource.setResourceList(buildResourceList());
            Assertions.assertTrue(withResource.checkParameters(),
                    "expected valid with resource for json: [" + variant + "]");
        }

        // a non-empty inline definition stays inline even when a resource is attached
        DataxParameters inlineWithResource = new DataxParameters();
        inlineWithResource.setCustomConfig(1);
        inlineWithResource.setJson("{\"job\":{}}");
        inlineWithResource.setResourceList(buildResourceList());
        Assertions.assertFalse(inlineWithResource.isInlineJsonAbsent());
        Assertions.assertTrue(inlineWithResource.checkParameters());

        // malformed json is not treated as absent, downstream validation reports it
        DataxParameters malformed = new DataxParameters();
        malformed.setCustomConfig(1);
        malformed.setJson("{invalid");
        Assertions.assertFalse(malformed.isInlineJsonAbsent());

        DataxParameters withNeither = new DataxParameters();
        withNeither.setCustomConfig(1);
        Assertions.assertFalse(withNeither.checkParameters());
    }

    private List<ResourceInfo> buildResourceList() {
        ResourceInfo resource = new ResourceInfo();
        resource.setResourceName("/datax/job.json");
        List<ResourceInfo> resources = new ArrayList<>();
        resources.add(resource);
        return resources;
    }

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
        dataxParameters.setJobChannel(1);
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
                + "jobChannel=1, "
                + "xms=0, "
                + "xmx=-100, "
                + "batchSize=0, "
                + "resourceList=[{\"id\":null,\"resourceName\":\"/hdfs.keytab\",\"res\":null}]"
                + "}";

        Assertions.assertEquals(expected, dataxParameters.toString());
    }

    @Test
    public void testBatchSize() {
        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setBatchSize(0);
        Assertions.assertEquals(0, dataxParameters.getBatchSize());

        dataxParameters.setBatchSize(2048);
        Assertions.assertEquals(2048, dataxParameters.getBatchSize());

        dataxParameters.setBatchSize(65536);
        Assertions.assertEquals(65536, dataxParameters.getBatchSize());
    }

    public String loadJvmEnvTest(DataxParameters dataXParameters) {
        int xms = dataXParameters.getXms() < 1 ? 1 : dataXParameters.getXms();
        int xmx = dataXParameters.getXmx() < 1 ? 1 : dataXParameters.getXmx();
        return String.format(JVM_PARAM, xms, xmx);
    }
}
