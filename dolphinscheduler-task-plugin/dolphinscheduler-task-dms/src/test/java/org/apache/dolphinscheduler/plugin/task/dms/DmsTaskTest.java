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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.databasemigrationservice.model.InvalidResourceStateException;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class,
    PropertyUtils.class,
    DmsHook.class
})
@PowerMockIgnore({"javax.*"})
public class DmsTaskTest {

    @Mock
    DmsHook dmsHook;

    DmsTask dmsTask;

    @Before
    public void before() throws Exception {
        whenNew(DmsHook.class).withAnyArguments().thenReturn(dmsHook);
        DmsParameters dmsParameters = new DmsParameters();
        dmsTask = initTask(dmsParameters);
        dmsTask.initDmsHook();
        MemberModifier.field(DmsTask.class, "dmsHook").set(dmsTask, dmsHook);
    }

    @Test
    public void testCreateTaskJson() {
        String jsonData = "{\n" +
            "  \"ReplicationTaskIdentifier\":\"task6\",\n" +
            "  \"SourceEndpointArn\":\"arn:aws:dms:ap-southeast-1:511640773671:endpoint:Z7SUEAL273SCT7OCPYNF5YNDHJDDFRATGNQISOQ\",\n" +
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
        dmsTask.initDmsHook();
        dmsTask.convertJsonParameters();
        DmsParameters dmsParametersNew = dmsTask.getParameters();
        Assert.assertEquals("task6", dmsParametersNew.getReplicationTaskIdentifier());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:511640773671:endpoint:Z7SUEAL273SCT7OCPYNF5YNDHJDDFRATGNQISOQ", dmsParametersNew.getSourceEndpointArn());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:511640773671:endpoint:aws-mysql57-target", dmsParametersNew.getTargetEndpointArn());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:511640773671:rep:dms2c2g", dmsParametersNew.getReplicationInstanceArn());
        Assert.assertEquals("full-load", dmsParametersNew.getMigrationType());
        Assert.assertEquals("file://table-mapping.json", dmsParametersNew.getTableMappings());
        Assert.assertEquals("file://ReplicationTaskSettings.json", dmsParametersNew.getReplicationTaskSettings());
        Assert.assertEquals("key1", dmsParametersNew.getTags().get(0).getKey());
        Assert.assertEquals("value1", dmsParametersNew.getTags().get(0).getValue());
    }

    @Test
    public void testCheckCreateReplicationTask() throws Exception {
        DmsParameters dmsParameters = dmsTask.getParameters();

        dmsParameters.setIsRestartTask(true);
        Assert.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.checkCreateReplicationTask());

        dmsParameters.setIsRestartTask(false);
        when(dmsHook.createReplicationTask()).thenReturn(true);
        Assert.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.checkCreateReplicationTask());

        when(dmsHook.createReplicationTask()).thenReturn(false);
        dmsTask.checkCreateReplicationTask();
        Assert.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.checkCreateReplicationTask());
    }

    @Test
    public void testStartReplicationTask() {
        when(dmsHook.startReplicationTask()).thenReturn(true);
        Assert.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.startReplicationTask());

        when(dmsHook.startReplicationTask()).thenReturn(false);
        Assert.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }

    @Test
    public void testStartReplicationTaskRestartTestConnection() {

        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);
        when(dmsHook.testConnectionEndpoint()).thenReturn(true);
        when(dmsHook.startReplicationTask())
            .thenThrow(new InvalidResourceStateException("Test connection"))
            .thenReturn(true);
        Assert.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, dmsTask.startReplicationTask());


        when(dmsHook.startReplicationTask())
            .thenThrow(new InvalidResourceStateException("Test connection"))
            .thenReturn(false);
        Assert.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }


    @Test
    public void testStartReplicationTaskRestartOtherException() {

        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);
        when(dmsHook.testConnectionEndpoint()).thenReturn(true);
        when(dmsHook.startReplicationTask()).thenThrow(new InvalidResourceStateException("other error"));
        Assert.assertEquals(TaskConstants.EXIT_CODE_FAILURE, dmsTask.startReplicationTask());
    }

    @Test
    public void testIsStopTaskWhenCdc() {
        DmsParameters parameters = dmsTask.getParameters();
        parameters.setIsRestartTask(false);

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(dmsTask.dmsHook.describeReplicationTasks()).thenReturn(replicationTask);

        when(replicationTask.getMigrationType()).thenReturn("cdc");
        parameters.setCdcStopPosition("now");
        Assert.assertFalse(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load-and-cdc");
        parameters.setCdcStopPosition("now");
        Assert.assertFalse(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load-and-cdc");
        parameters.setCdcStopPosition(null);
        Assert.assertTrue(dmsTask.isStopTaskWhenCdc());

        when(replicationTask.getMigrationType()).thenReturn("full-load");
        parameters.setCdcStopPosition(null);
        Assert.assertFalse(dmsTask.isStopTaskWhenCdc());
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
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/dolphinscheduler_dms_%s");
        return taskExecutionContext;
    }
}


