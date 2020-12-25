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
package org.apache.dolphinscheduler.api.enums;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExecuteTypeTest {

    @Test
    public void testGetEnum() {
        assertEquals(ExecuteType.REPEAT_RUNNING, ExecuteType.getEnum(1));
        assertEquals(ExecuteType.RECOVER_SUSPENDED_PROCESS, ExecuteType.getEnum(2));
        assertEquals(ExecuteType.START_FAILURE_TASK_PROCESS, ExecuteType.getEnum(3));
        assertEquals(ExecuteType.STOP, ExecuteType.getEnum(4));
        assertEquals(ExecuteType.PAUSE, ExecuteType.getEnum(5));
    }
}
