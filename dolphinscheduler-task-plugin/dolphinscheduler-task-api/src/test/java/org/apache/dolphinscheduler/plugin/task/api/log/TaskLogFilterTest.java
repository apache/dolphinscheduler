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

package org.apache.dolphinscheduler.plugin.task.api.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

class TaskLogFilterTest {

    @Test
    void shouldAcceptLogWhenConfiguredMdcKeyExists() {
        TaskLogFilter filter = new TaskLogFilter();
        filter.setKey("taskOutputLogFullPath");

        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap())
                .thenReturn(Collections.singletonMap("taskOutputLogFullPath", "/tmp/task-output.log"));

        assertEquals(FilterReply.ACCEPT, filter.decide(event));
    }

    @Test
    void shouldDenyLogWhenConfiguredMdcKeyMissing() {
        TaskLogFilter filter = new TaskLogFilter();
        filter.setKey("taskOutputLogFullPath");

        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(Collections.emptyMap());

        assertEquals(FilterReply.DENY, filter.decide(event));
    }
}
