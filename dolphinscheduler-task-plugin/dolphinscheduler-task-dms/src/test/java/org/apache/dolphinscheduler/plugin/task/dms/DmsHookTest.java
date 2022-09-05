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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.databasemigrationservice.AWSDatabaseMigrationService;
import com.amazonaws.services.databasemigrationservice.model.CreateReplicationTaskResult;
import com.amazonaws.services.databasemigrationservice.model.DescribeReplicationTasksResult;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTaskStats;
import com.amazonaws.services.databasemigrationservice.model.StartReplicationTaskResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class,
    DmsHook.class
})
@PowerMockIgnore({"javax.*"})
public class DmsHookTest {

    AWSDatabaseMigrationService client;

    @Before
    public void before() {
        mockStatic(DmsHook.class);
        client = mock(AWSDatabaseMigrationService.class);
        when(DmsHook.createClient()).thenAnswer(invocation -> client);
    }

    @Test(timeout = 60000)
    public void testCreateReplicationTask() throws Exception {

        DmsHook dmsHook = spy(new DmsHook());
        CreateReplicationTaskResult createReplicationTaskResult = mock(CreateReplicationTaskResult.class);
        when(client.createReplicationTask(any())).thenReturn(createReplicationTaskResult);

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(replicationTask.getReplicationTaskArn()).thenReturn("arn:aws:dms:ap-southeast-1:123456789012:task:task");
        when(replicationTask.getReplicationTaskIdentifier()).thenReturn("task");
        when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.READY);
        when(createReplicationTaskResult.getReplicationTask()).thenReturn(replicationTask);

        doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
        Assert.assertTrue(dmsHook.createReplicationTask());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:123456789012:task:task", dmsHook.getReplicationTaskArn());
        Assert.assertEquals("task", dmsHook.getReplicationTaskIdentifier());
    }

    @Test(timeout = 60000)
    public void testStartReplicationTask() {
        DmsHook dmsHook = spy(new DmsHook());
        StartReplicationTaskResult startReplicationTaskResult = mock(StartReplicationTaskResult.class);
        when(client.startReplicationTask(any())).thenReturn(startReplicationTaskResult);

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(replicationTask.getReplicationTaskArn()).thenReturn("arn:aws:dms:ap-southeast-1:123456789012:task:task");
        when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.RUNNING);
        when(startReplicationTaskResult.getReplicationTask()).thenReturn(replicationTask);

        doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
        Assert.assertTrue(dmsHook.startReplicationTask());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:123456789012:task:task", dmsHook.getReplicationTaskArn());
    }

    @Test(timeout = 60000)
    public void testCheckFinishedReplicationTask() {
        DmsHook dmsHook = spy(new DmsHook());

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.STOPPED);

        doReturn(replicationTask).when(dmsHook).describeReplicationTasks();

        when(replicationTask.getStopReason()).thenReturn("*_FINISHED");
        Assert.assertTrue(dmsHook.checkFinishedReplicationTask());

        when(replicationTask.getStopReason()).thenReturn("*_ERROR");
        Assert.assertFalse(dmsHook.checkFinishedReplicationTask());
    }

    @Test(timeout = 60000)
    public void testDeleteReplicationTask() {
        DmsHook dmsHook = spy(new DmsHook());

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.DELETE);
        doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
        Assert.assertTrue(dmsHook.deleteReplicationTask());

    }

    @Test
    public void testTestConnectionEndpoint() {
        DmsHook dmsHook = spy(new DmsHook());

        String replicationInstanceArn = "replicationInstanceArn";
        String trueSourceEndpointArn = "trueSourceEndpointArn";
        String trueTargetEndpointArn = "trueTargetEndpointArn";
        String falseSourceEndpointArn = "falseSourceEndpointArn";
        String falseTargetEndpointArn = "falseTargetEndpointArn";

        doReturn(true).when(dmsHook).testConnection(replicationInstanceArn, trueSourceEndpointArn);
        doReturn(true).when(dmsHook).testConnection(replicationInstanceArn, trueTargetEndpointArn);
        doReturn(false).when(dmsHook).testConnection(replicationInstanceArn, falseSourceEndpointArn);
        doReturn(false).when(dmsHook).testConnection(replicationInstanceArn, falseTargetEndpointArn);


        dmsHook.setReplicationInstanceArn(replicationInstanceArn);

        dmsHook.setSourceEndpointArn(trueSourceEndpointArn);
        dmsHook.setTargetEndpointArn(trueTargetEndpointArn);
        Assert.assertTrue(dmsHook.testConnectionEndpoint());

        dmsHook.setSourceEndpointArn(falseSourceEndpointArn);
        dmsHook.setTargetEndpointArn(falseTargetEndpointArn);
        Assert.assertFalse(dmsHook.testConnectionEndpoint());

        dmsHook.setSourceEndpointArn(trueSourceEndpointArn);
        dmsHook.setTargetEndpointArn(falseTargetEndpointArn);
        Assert.assertFalse(dmsHook.testConnectionEndpoint());

        dmsHook.setSourceEndpointArn(falseSourceEndpointArn);
        dmsHook.setTargetEndpointArn(trueTargetEndpointArn);
        Assert.assertFalse(dmsHook.testConnectionEndpoint());

    }


    @Test
    public void testDescribeReplicationTasks() {

        DmsHook dmsHook = new DmsHook();
        dmsHook.setReplicationInstanceArn("arn:aws:dms:ap-southeast-1:123456789012:task:task_exist");

        DescribeReplicationTasksResult describeReplicationTasksResult = mock(DescribeReplicationTasksResult.class);
        when(client.describeReplicationTasks(any())).thenReturn(describeReplicationTasksResult);

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        when(replicationTask.getReplicationTaskArn()).thenReturn("arn:aws:dms:ap-southeast-1:123456789012:task:task");
        when(replicationTask.getReplicationTaskIdentifier()).thenReturn("task");
        when(replicationTask.getSourceEndpointArn()).thenReturn("arn:aws:dms:ap-southeast-1:123456789012:endpoint:source");
        when(replicationTask.getTargetEndpointArn()).thenReturn("arn:aws:dms:ap-southeast-1:123456789012:endpoint:target");

        when(describeReplicationTasksResult.getReplicationTasks()).thenReturn(Arrays.asList(replicationTask));

        ReplicationTask replicationTaskOut = dmsHook.describeReplicationTasks();
        Assert.assertNotEquals(dmsHook.getReplicationInstanceArn(), replicationTaskOut.getReplicationTaskArn());
        Assert.assertEquals("task", replicationTaskOut.getReplicationTaskIdentifier());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:123456789012:endpoint:source", replicationTaskOut.getSourceEndpointArn());
        Assert.assertEquals("arn:aws:dms:ap-southeast-1:123456789012:endpoint:target", replicationTaskOut.getTargetEndpointArn());

    }


    @Test(timeout = 60000)
    public void testAwaitReplicationTaskStatus() {

        DmsHook dmsHook = spy(new DmsHook());

        ReplicationTask replicationTask = mock(ReplicationTask.class);
        doReturn(replicationTask).when(dmsHook).describeReplicationTasks();

        ReplicationTaskStats taskStats = mock(ReplicationTaskStats.class);
        when(replicationTask.getReplicationTaskStats()).thenReturn(taskStats);
        when(taskStats.getFullLoadProgressPercent()).thenReturn(100);

        when(replicationTask.getStatus()).thenReturn(
            DmsHook.STATUS.STOPPED
        );
        Assert.assertTrue(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED));

        when(replicationTask.getStatus()).thenReturn(
            DmsHook.STATUS.RUNNING,
            DmsHook.STATUS.STOPPED
        );
        Assert.assertTrue(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED));

        when(replicationTask.getStatus()).thenReturn(
            DmsHook.STATUS.RUNNING,
            DmsHook.STATUS.STOPPED
        );
        Assert.assertFalse(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED, DmsHook.STATUS.RUNNING));
    }

    @Test
    public void testReplaceFileParameters() throws IOException {
        String path = this.getClass().getResource("table_mapping.json").getPath();

        String jsonData = loadJson("table_mapping.json");

        DmsHook dmsHook = new DmsHook();

        String pathParameter = "file://" + path;
        Assert.assertEquals(jsonData, dmsHook.replaceFileParameters(pathParameter));

//        String pathParameter2 = "file://" + "not_exist.json";
//
//        try {
//            Assert.assertEquals(pathParameter2, dmsHook.replaceFileParameters(pathParameter2));
//        }catch (Exception e) {
//            Assert.assertTrue(e instanceof IOException);
//        }

        String pathParameter3 = "{}";
        Assert.assertEquals(pathParameter3, dmsHook.replaceFileParameters(pathParameter3));

    }

//    this.getClass().getResourceAsStream("SagemakerRequestJson.json"))

    private String loadJson(String fileName) {
        String jsonData;
        try (InputStream i = this.getClass().getResourceAsStream(fileName)) {
            assert i != null;
            jsonData = IOUtils.toString(i, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jsonData;
    }

}


