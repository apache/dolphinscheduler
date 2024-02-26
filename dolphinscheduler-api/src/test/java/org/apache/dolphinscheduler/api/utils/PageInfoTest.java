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

package org.apache.dolphinscheduler.api.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PageInfoTest {

    @Test
    public void testGetTotalPageWhenTotalAndPageSizeArePositive() {
        PageInfo<Object> pageInfo = new PageInfo<>();
        pageInfo.setTotal(100);
        pageInfo.setPageSize(20);
        Assertions.assertEquals(5, pageInfo.getTotalPage());
    }

    @Test
    public void testGetTotalPageWhenTotalIsZero() {
        PageInfo<Object> pageInfo = new PageInfo<>();
        pageInfo.setTotal(0);
        pageInfo.setPageSize(20);
        Assertions.assertEquals(1, pageInfo.getTotalPage());
    }

    @Test
    public void testGetTotalPageWhenPageSizeIsZero() {
        PageInfo<Object> pageInfo = new PageInfo<>();
        pageInfo.setTotal(101);
        pageInfo.setPageSize(0);
        Assertions.assertEquals(11, pageInfo.getTotalPage());
    }
}
