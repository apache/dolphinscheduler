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

package org.apache.dolphinscheduler.common.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListenerEventTypeTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, ListenerEventType.SERVER_DOWN.getCode());
        Assertions.assertEquals(1, ListenerEventType.PROCESS_DEFINITION_CREATED.getCode());
    }

    @Test
    public void testGetDesp() {
        Assertions.assertEquals("PROCESS_DEFINITION_UPDATED", ListenerEventType.PROCESS_DEFINITION_UPDATED.getDescp());
        Assertions.assertEquals("PROCESS_DEFINITION_DELETED", ListenerEventType.PROCESS_DEFINITION_DELETED.getDescp());
    }

    @Test
    public void testGetListenerEventTypeByCode() {
        Assertions.assertEquals(ListenerEventType.PROCESS_START, ListenerEventType.of(4));
        Assertions.assertNotEquals(ListenerEventType.PROCESS_END, ListenerEventType.of(6));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ListenerEventType.of(-1));
    }
}
