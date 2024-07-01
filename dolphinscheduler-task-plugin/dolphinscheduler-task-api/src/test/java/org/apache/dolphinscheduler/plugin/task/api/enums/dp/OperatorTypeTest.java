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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OperatorTypeTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, OperatorType.EQ.getCode());
        Assertions.assertEquals(1, OperatorType.LT.getCode());
        Assertions.assertEquals(2, OperatorType.LE.getCode());
        Assertions.assertEquals(3, OperatorType.GT.getCode());
        Assertions.assertEquals(4, OperatorType.GE.getCode());
        Assertions.assertEquals(5, OperatorType.NE.getCode());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("equal", OperatorType.EQ.getDescription());
        Assertions.assertEquals("little than", OperatorType.LT.getDescription());
        Assertions.assertEquals("little and equal", OperatorType.LE.getDescription());
        Assertions.assertEquals("great than", OperatorType.GT.getDescription());
        Assertions.assertEquals("great and equal", OperatorType.GE.getDescription());
        Assertions.assertEquals("not equal", OperatorType.NE.getDescription());
    }

    @Test
    public void testOf() {
        Assertions.assertEquals(OperatorType.EQ, OperatorType.of(0));
        Assertions.assertEquals(OperatorType.LT, OperatorType.of(1));
        Assertions.assertEquals(OperatorType.LE, OperatorType.of(2));
        Assertions.assertEquals(OperatorType.GT, OperatorType.of(3));
        Assertions.assertEquals(OperatorType.GE, OperatorType.of(4));
        Assertions.assertEquals(OperatorType.NE, OperatorType.of(5));
    }
}
