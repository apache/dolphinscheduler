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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.databasemigrationservice.AWSDatabaseMigrationService;
import com.amazonaws.services.databasemigrationservice.model.CreateReplicationTaskResult;
import com.amazonaws.services.databasemigrationservice.model.DescribeReplicationTasksResult;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTaskStats;
import com.amazonaws.services.databasemigrationservice.model.StartReplicationTaskResult;

@RunWith(MockitoJUnitRunner.class)
public class DmsHookTest {

    final String replicationTaskArn = "arn:aws:dms:ap-southeast-1:123456789012:task:task";
    AWSDatabaseMigrationService client;

    @Before
    public void before() {
        client = mock(AWSDatabaseMigrationService.class);
    }

    @Test(timeout = 60000)
    public void testCreateReplicationTask() throws Exception {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);

            DmsHook dmsHook = spy(new DmsHook());
            CreateReplicationTaskResult createReplicationTaskResult = mock(CreateReplicationTaskResult.class);
            when(client.createReplicationTask(any())).thenReturn(createReplicationTaskResult);

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            final String taskIdentifier = "task";
            when(replicationTask.getReplicationTaskArn()).thenReturn(replicationTaskArn);
            when(replicationTask.getReplicationTaskIdentifier()).thenReturn(taskIdentifier);
            when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.READY);
            when(createReplicationTaskResult.getReplicationTask()).thenReturn(replicationTask);

            doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
            Assert.assertTrue(dmsHook.createReplicationTask());
            Assert.assertEquals(replicationTaskArn, dmsHook.getReplicationTaskArn());
            Assert.assertEquals(taskIdentifier, dmsHook.getReplicationTaskIdentifier());
        }
    }

    @Test(timeout = 60000)
    public void testStartReplicationTask() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);

            DmsHook dmsHook = spy(new DmsHook());
            StartReplicationTaskResult startReplicationTaskResult = mock(StartReplicationTaskResult.class);
            when(client.startReplicationTask(any())).thenReturn(startReplicationTaskResult);

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            when(replicationTask.getReplicationTaskArn()).thenReturn(replicationTaskArn);
            when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.RUNNING);
            when(startReplicationTaskResult.getReplicationTask()).thenReturn(replicationTask);

            doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
            Assert.assertTrue(dmsHook.startReplicationTask());
            Assert.assertEquals(replicationTaskArn, dmsHook.getReplicationTaskArn());
        }
    }

    @Test(timeout = 60000)
    public void testCheckFinishedReplicationTask() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);
            DmsHook dmsHook = spy(new DmsHook());

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.STOPPED);

            doReturn(replicationTask).when(dmsHook).describeReplicationTasks();

            when(replicationTask.getStopReason()).thenReturn("*_FINISHED");
            Assert.assertTrue(dmsHook.checkFinishedReplicationTask());

            when(replicationTask.getStopReason()).thenReturn("*_ERROR");
            Assert.assertFalse(dmsHook.checkFinishedReplicationTask());
        }
    }

    @Test(timeout = 60000)
    public void testDeleteReplicationTask() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);

            DmsHook dmsHook = spy(new DmsHook());

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            when(replicationTask.getStatus()).thenReturn(DmsHook.STATUS.DELETE);
            doReturn(replicationTask).when(dmsHook).describeReplicationTasks();
            Assert.assertTrue(dmsHook.deleteReplicationTask());
        }

    }

    @Test
    public void testTestConnectionEndpoint() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);
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

    }

    @Test
    public void testDescribeReplicationTasks() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);
            DmsHook dmsHook = spy(new DmsHook());

            dmsHook.setReplicationInstanceArn("arn:aws:dms:ap-southeast-1:123456789012:task:task_exist");

            DescribeReplicationTasksResult describeReplicationTasksResult = mock(DescribeReplicationTasksResult.class);
            when(client.describeReplicationTasks(any())).thenReturn(describeReplicationTasksResult);

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            when(replicationTask.getReplicationTaskArn())
                    .thenReturn("arn:aws:dms:ap-southeast-1:123456789012:task:task");
            when(replicationTask.getReplicationTaskIdentifier()).thenReturn("task");

            final String sourceArn = "arn:aws:dms:ap-southeast-1:123456789012:endpoint:source";
            final String targetArn = "arn:aws:dms:ap-southeast-1:123456789012:endpoint:target";

            when(replicationTask.getSourceEndpointArn()).thenReturn(sourceArn);
            when(replicationTask.getTargetEndpointArn()).thenReturn(targetArn);

            when(describeReplicationTasksResult.getReplicationTasks()).thenReturn(Arrays.asList(replicationTask));

            ReplicationTask replicationTaskOut = dmsHook.describeReplicationTasks();
            Assert.assertNotEquals(dmsHook.getReplicationInstanceArn(), replicationTaskOut.getReplicationTaskArn());
            Assert.assertEquals("task", replicationTaskOut.getReplicationTaskIdentifier());
            Assert.assertEquals(sourceArn, replicationTaskOut.getSourceEndpointArn());
            Assert.assertEquals(targetArn, replicationTaskOut.getTargetEndpointArn());
        }

    }

    @Test(timeout = 60000)
    public void testAwaitReplicationTaskStatus() {
        try (MockedStatic<DmsHook> mockHook = Mockito.mockStatic(DmsHook.class)) {
            mockHook.when(DmsHook::createClient).thenReturn(client);
            DmsHook dmsHook = spy(new DmsHook());

            ReplicationTask replicationTask = mock(ReplicationTask.class);
            doReturn(replicationTask).when(dmsHook).describeReplicationTasks();

            ReplicationTaskStats taskStats = mock(ReplicationTaskStats.class);
            when(replicationTask.getReplicationTaskStats()).thenReturn(taskStats);
            when(taskStats.getFullLoadProgressPercent()).thenReturn(100);

            when(replicationTask.getStatus()).thenReturn(
                    DmsHook.STATUS.STOPPED);
            Assert.assertTrue(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED));

            when(replicationTask.getStatus()).thenReturn(
                    DmsHook.STATUS.RUNNING,
                    DmsHook.STATUS.STOPPED);
            Assert.assertTrue(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED));

            when(replicationTask.getStatus()).thenReturn(
                    DmsHook.STATUS.RUNNING,
                    DmsHook.STATUS.STOPPED);
            Assert.assertFalse(dmsHook.awaitReplicationTaskStatus(DmsHook.STATUS.STOPPED, DmsHook.STATUS.RUNNING));
        }
    }
}
