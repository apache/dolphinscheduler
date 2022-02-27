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

package org.apache.dolphinscheduler.plugin.task.api.enums.dp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DqFailureStrategyTest {

    @Test
    public void testGetCode() {
        assertEquals(0, DqFailureStrategy.ALERT.getCode());
        assertEquals(1, DqFailureStrategy.BLOCK.getCode());
    }

    @Test
    public void testGetDescription() {
        assertEquals("alert", DqFailureStrategy.ALERT.getDescription());
        assertEquals("block", DqFailureStrategy.BLOCK.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(DqFailureStrategy.ALERT, DqFailureStrategy.of(0));
        assertEquals(DqFailureStrategy.BLOCK, DqFailureStrategy.of(1));
    }
}
