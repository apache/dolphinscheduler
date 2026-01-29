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

package org.apache.dolphinscheduler.plugin.task.dms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.databasemigrationservice.model.InvalidResourceStateException;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;

@ExtendWith(MockitoExtension.class)
public class DmsTaskTest {

    @Mock
    DmsHook dmsHook;

    DmsTask dmsTask;

    @BeforeEach
    public void before() throws Exception {
        DmsParameters dmsParameters = new DmsParameters();
        dmsTask = initTask(dmsParameters);
        Field testAField = dmsTask.getClass().getDeclaredField("dmsHook");
        testAField.set(dmsTask, dmsHook);
    }

    @Test
    public void testCreateTaskJson() {
        String jsonData = "{\n" +
                "  \"ReplicationTaskIdentifier\":\"task6\",\n" +
                "  \"SourceEndpointArn\":\"arn:aws:dms:ap-southeast-1:511640773671:endpoint:Z7SUEAL273SCT7OCPYNF5YNDHJDDFRATGNQISOQ\",\n"
                +
                "  \"TargetEndpointArn\":\"arn:aws:dms:ap-southeast-1:511640773671:endpoint:aws-mysql57-target\",\n" +
                "  \"ReplicationInstanceArn\":\"arn:aws:dms:ap-southeast-1:511640773671:rep:dms2c2g\",\n" +
                "  \"MigrationType\":\"full-load\",\n" +
                "  \"TableMappings\":\"file://table-mapping.json\",\n" +
                "  \"ReplicationTaskSettings\":\"file://ReplicationTaskSettings.json\",\n" +
                "  \"Tags\":[\n" +
                "    {\n" +
                "      \"Key\":\"key1\",\n" +
                "      \"Value\":\"value1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        DmsParameters dmsParameters = new DmsParameters();
        dmsParameters.setIsJsonFormat(true);
        dmsParameters.setJsonData(jsonData);

        DmsTask dmsTask = initTask(dmsParameters);
        dmsTask.convertJsonParameters();
        DmsParameters dmsParametersNew = dmsTask.getParameters();
        Assertions.assertEquals("task6", dmsParametersNew.getReplicationTaskIdentifier());
        Assertions.assertEquals(
                "arn:aws:dms:ap-southeast-1:511640773671:endpoint:Z7SUEAL273SCT7OCPYNF5YNDHJDDFRATGNQISOQ",
                dmsParametersNew.getSourceEndpointArn());
        Assertions.assertEquals("arn:aws:dms:ap-southeast-1:511640773671:endpoint:aws-mysql57-target",
                dmsParametersNew.getTargetEndpointArn());
        Assertions.assertEquals("arn:aws:dms:ap-southeast-1:511640773671:rep:dms2c2g",
                dmsParametersNew.getReplicationInstanceArn());
        Assertions.assertEquals("full-load", dmsParametersNew.getMigrationType());
        Assertions.assertEquals("file://table-mapping.json", dmsParametersNew.getTableMappings());
        Assertions.assertEquals("file://ReplicationTaskSettings.json", dmsParametersNew.getReplicationTaskSettings());
        Assertions.assertEquals("key1", dmsParametersNew.getTags().get(0).getKey());
        Assertions.assertEquals("value1", dmsParametersNew.getTags().get(0).getValue());
    }

    @Test
    public void testCheckCreateReplicationTask() throws Exception {

        DmsParameters dmsParameters = dmsTask.getParameters();

        dmsParameters.setIsRestartTask(true);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.checkCreateReplicationTask());

        dmsParameters.setIsRestartTask(false);
        when(dmsHook.createReplicationTask()).thenReturn(true);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.checkCreateReplicationTask());

        when(dmsHook.createReplicationTask()).thenReturn(false);
        dmsTask.checkCreateReplicationTask();
        Assertions.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.checkCreateReplicationTask());
    }

    @Test
    public void testStartReplicationTask() {
        when(dmsHook.startReplicationTask()).thenReturn(true);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.startReplicationTask());

        when(dmsHook.startReplicationTask()).thenReturn(false);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }

    @Test
    public void testStartReplicationTaskRestartTestConnection() {

        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);
        when(dmsHook.testConnectionEndpoint()).thenReturn(true);
        when(dmsHook.startReplicationTask())
                .thenThrow(new InvalidResourceStateException("Test connection"))
                .thenReturn(true);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.startReplicationTask());

        when(dmsHook.startReplicationTask())
                .thenThrow(new InvalidResourceStateException("Test connection"))
                .thenReturn(false);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }

    @Test
    public void testStartReplicationTaskRestartOtherException() {

        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);
        when(dmsHook.startReplicationTask()).thenThrow(new InvalidResourceStateException("other error"));
        Assertions.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }

    @Test
    public void testIsStopTaskWhenCdc() {
        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(dmsTask.dmsHook.describeReplicationTasks()).thenReturn(replicationTask);

        when(replicationTask.getMigrationType()).thenReturn("cdc");
        parameters.setCdcStopPosition("now");
        Assertions.assertFalse(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load-and-cdc");
        parameters.setCdcStopPosition("now");
        Assertions.assertFalse(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load-and-cdc");
        parameters.setCdcStopPosition(null);
        Assertions.assertTrue(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load");
        parameters.setCdcStopPosition(null);
        Assertions.assertFalse(dmsTask.isStopTaskWhenCdc());
    }

    private DmsTask initTask(DmsParameters dmsParameters) {
        TaskExecutionContext taskExecutionContext = createContext(dmsParameters);
        DmsTask dmsTask = new DmsTask(taskExecutionContext);
        dmsTask.init();
        return dmsTask;
    }

    public TaskExecutionContext createContext(DmsParameters dmsParameters) {
        String parameters = JSONUtils.toJsonString(dmsParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        return taskExecutionContext;
    }
}
