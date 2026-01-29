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

import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionCreatedListenerEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowDefinitionCreatedListenerEventTest {

    @Test
    public void testBuildProcessDefinitionUpdatedListenerEvent() {
        int id = 1;
        long code = 1L;
        String name = "testName";
        ReleaseState releaseState = ReleaseState.OFFLINE;
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(id);
        workflowDefinition.setCode(code);
        workflowDefinition.setName(name);
        workflowDefinition.setReleaseState(releaseState);
        ProcessDefinitionCreatedListenerEvent event = new ProcessDefinitionCreatedListenerEvent(workflowDefinition);
        Assertions.assertEquals(event.getEventType(), ListenerEventType.PROCESS_DEFINITION_CREATED);
        Assertions.assertEquals(event.getId(), id);
        Assertions.assertEquals(event.getCode(), code);
        Assertions.assertEquals(event.getName(), name);
        Assertions.assertEquals(event.getReleaseState(), releaseState);
        Assertions.assertEquals(String.format("process definition created:%s", name), event.getTitle());
    }
}
