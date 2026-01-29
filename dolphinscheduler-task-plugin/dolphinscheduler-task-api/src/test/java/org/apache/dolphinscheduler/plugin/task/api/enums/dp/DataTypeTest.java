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

public class DataTypeTest {

    /**
     * 0-string
     * 1-list
     * 2-number
     * 3-sql
     */
    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, DataType.STRING.getCode());
        Assertions.assertEquals(1, DataType.LIST.getCode());
        Assertions.assertEquals(2, DataType.NUMBER.getCode());
        Assertions.assertEquals(3, DataType.LIKE_SQL.getCode());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("string", DataType.STRING.getDescription());
        Assertions.assertEquals("list", DataType.LIST.getDescription());
        Assertions.assertEquals("number", DataType.NUMBER.getDescription());
        Assertions.assertEquals("sql", DataType.LIKE_SQL.getDescription());
    }

    @Test
    public void testOf() {
        Assertions.assertEquals(DataType.STRING, DataType.of(0));
        Assertions.assertEquals(DataType.LIST, DataType.of(1));
        Assertions.assertEquals(DataType.NUMBER, DataType.of(2));
        Assertions.assertEquals(DataType.LIKE_SQL, DataType.of(3));
    }
}
