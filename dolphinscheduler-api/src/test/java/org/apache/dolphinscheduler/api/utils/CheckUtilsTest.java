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

package org.apache.dolphinscheduler.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.python.PythonParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.subprocess.SubProcessParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Map;

import org.junit.Test;

public class CheckUtilsTest {

    /**
     * check username
     */
    @Test
    public void testCheckUserName() {
        assertTrue(CheckUtils.checkUserName("test01"));
        assertFalse(CheckUtils.checkUserName(null));
        assertFalse(CheckUtils.checkUserName("test01@abc"));
    }

    /**
     * check email
     */
    @Test
    public void testCheckEmail() {
        assertTrue(CheckUtils.checkEmail("test01@gmail.com"));
        assertFalse(CheckUtils.checkEmail("test01@gmail"));
    }

    /**
     * check desc
     */
    @Test
    public void testCheckDesc() {
        Map<String, Object> objectMap = CheckUtils.checkDesc("I am desc");
        Status status = (Status) objectMap.get(Constants.STATUS);
        assertEquals(status.getCode(), Status.SUCCESS.getCode());
    }

    @Test
    public void testCheckOtherParams() {
        assertFalse(CheckUtils.checkOtherParams(null));
        assertFalse(CheckUtils.checkOtherParams(""));
        assertTrue(CheckUtils.checkOtherParams("xxx"));
        assertFalse(CheckUtils.checkOtherParams("{}"));
        assertFalse(CheckUtils.checkOtherParams("{\"key1\":111}"));
    }

    /**
     * check passwd
     */
    @Test
    public void testCheckPassword() {
        assertFalse(CheckUtils.checkPassword(null));
        assertFalse(CheckUtils.checkPassword("a"));
        assertFalse(CheckUtils.checkPassword("1234567890abcderfasdf2"));
        assertTrue(CheckUtils.checkPassword("123456"));
    }

    /**
     * check phone
     */
    @Test
    public void testCheckPhone() {
        // phone can be null
        assertTrue(CheckUtils.checkPhone(null));
        assertFalse(CheckUtils.checkPhone("14567134578654"));
        assertTrue(CheckUtils.checkPhone("17362537263"));
    }

    @Test
    public void testCheckTaskNodeParameters() {
        TaskNode taskNode = new TaskNode();
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        taskNode.setType("unKnown");
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        taskNode.setParams("unKnown");
        taskNode.setType("unKnown");
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        taskNode.setParams("unKnown");
        taskNode.setType(null);
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        // sub SubProcessParameters
        SubProcessParameters subProcessParameters = new SubProcessParameters();
        taskNode.setParams(JSONUtils.toJsonString(subProcessParameters));
        taskNode.setType(TaskType.SUB_PROCESS.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        subProcessParameters.setProcessDefinitionCode(1234L);
        taskNode.setParams(JSONUtils.toJsonString(subProcessParameters));
        taskNode.setType(TaskType.SUB_PROCESS.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // ShellParameters
        ShellParameters shellParameters = new ShellParameters();
        taskNode.setParams(JSONUtils.toJsonString(shellParameters));
        taskNode.setType(TaskType.SHELL.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        shellParameters.setRawScript("");
        taskNode.setParams(JSONUtils.toJsonString(shellParameters));
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        shellParameters.setRawScript("sss");
        taskNode.setParams(JSONUtils.toJsonString(shellParameters));
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // ProcedureParameters
        ProcedureParameters procedureParameters = new ProcedureParameters();
        taskNode.setParams(JSONUtils.toJsonString(procedureParameters));
        taskNode.setType(TaskType.PROCEDURE.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        procedureParameters.setDatasource(1);
        procedureParameters.setType("xx");
        procedureParameters.setMethod("yy");
        taskNode.setParams(JSONUtils.toJsonString(procedureParameters));
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // SqlParameters
        SqlParameters sqlParameters = new SqlParameters();
        taskNode.setParams(JSONUtils.toJsonString(sqlParameters));
        taskNode.setType(TaskType.SQL.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        sqlParameters.setDatasource(1);
        sqlParameters.setType("xx");
        sqlParameters.setSql("yy");
        taskNode.setParams(JSONUtils.toJsonString(sqlParameters));
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // MapReduceParameters
        MapReduceParameters mapreduceParameters = new MapReduceParameters();
        taskNode.setParams(JSONUtils.toJsonString(mapreduceParameters));
        taskNode.setType(TaskType.MR.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));

        ResourceInfo resourceInfoMapreduce = new ResourceInfo();
        resourceInfoMapreduce.setId(1);
        resourceInfoMapreduce.setRes("");
        mapreduceParameters.setMainJar(resourceInfoMapreduce);
        mapreduceParameters.setProgramType(ProgramType.JAVA);
        taskNode.setParams(JSONUtils.toJsonString(mapreduceParameters));
        taskNode.setType(TaskType.MR.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // SparkParameters
        SparkParameters sparkParameters = new SparkParameters();
        taskNode.setParams(JSONUtils.toJsonString(sparkParameters));
        taskNode.setType(TaskType.SPARK.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        sparkParameters.setMainJar(new ResourceInfo());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setSparkVersion("1.1.1");
        taskNode.setParams(JSONUtils.toJsonString(sparkParameters));
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // PythonParameters
        PythonParameters pythonParameters = new PythonParameters();
        taskNode.setParams(JSONUtils.toJsonString(pythonParameters));
        taskNode.setType(TaskType.PYTHON.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        pythonParameters.setRawScript("ss");
        taskNode.setParams(JSONUtils.toJsonString(pythonParameters));
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // DependentParameters
        DependentParameters dependentParameters = new DependentParameters();
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
        taskNode.setType(TaskType.DEPENDENT.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // FlinkParameters
        FlinkParameters flinkParameters = new FlinkParameters();
        taskNode.setParams(JSONUtils.toJsonString(flinkParameters));
        taskNode.setType(TaskType.FLINK.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        flinkParameters.setMainJar(new ResourceInfo());
        flinkParameters.setProgramType(ProgramType.JAVA);
        taskNode.setParams(JSONUtils.toJsonString(flinkParameters));
        taskNode.setType(TaskType.FLINK.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // HTTP
        HttpParameters httpParameters = new HttpParameters();
        taskNode.setParams(JSONUtils.toJsonString(httpParameters));
        taskNode.setType(TaskType.HTTP.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        httpParameters.setUrl("httpUrl");
        taskNode.setParams(JSONUtils.toJsonString(httpParameters));
        taskNode.setType(TaskType.HTTP.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));

        // DataxParameters
        DataxParameters dataxParameters = new DataxParameters();
        taskNode.setParams(JSONUtils.toJsonString(dataxParameters));
        taskNode.setType(TaskType.DATAX.getDesc());
        assertFalse(CheckUtils.checkTaskNodeParameters(taskNode));
        dataxParameters.setCustomConfig(0);
        dataxParameters.setDataSource(111);
        dataxParameters.setDataTarget(333);
        dataxParameters.setSql(TaskType.SQL.getDesc());
        dataxParameters.setTargetTable("tar");
        taskNode.setParams(JSONUtils.toJsonString(dataxParameters));
        taskNode.setType(TaskType.DATAX.getDesc());
        assertTrue(CheckUtils.checkTaskNodeParameters(taskNode));
    }

}
