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

package org.apache.dolphinscheduler.plugin.task.grpc;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test GrpcTask
 */
@RunWith(JUnit4.class)
@ExtendWith(MockitoExtension.class)
public class GrpcTaskTest {


    @Test
    public void testHandleStatusCodeDefaultOK() throws Exception {

    }

    @Test
    public void testHandleStatusCodeCustom() throws Exception {

    }

    @Test
    public void testAddDefaultOutput() throws Exception {

    }

    private GrpcTask generateGrpcTask() {
        return genrateGrpcTaskWithJSONDefinition();
    }

    private GrpcTask genrateGrpcTaskWithJSONDefinition() {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        return new GrpcTask(taskExecutionContext);
    }
}
