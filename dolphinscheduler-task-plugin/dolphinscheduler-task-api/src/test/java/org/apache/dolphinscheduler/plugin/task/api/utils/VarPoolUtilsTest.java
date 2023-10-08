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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VarPoolUtilsTest {

    @Test
    void findVar() {
        HashMap<String, String> tcs = new HashMap<>();
        tcs.put("${(set_val=123)dsVal}", "set_val=123");
        tcs.put("1970-01-01 ${(set_val=123)dsVal}", "set_val=123");
        tcs.put("1970-01-01 ${(set_val=123)dsVal}123", null);
        tcs.put("${(set_val=123}dsVal", null);
        tcs.put("#{(set_val=123)dsVal}", "set_val=123");
        tcs.put("1970-01-01 #{(set_val=123)dsVal}", "set_val=123");
        tcs.put("1970-01-01 #{(set_val=123)dsVal}123", null);
        tcs.put("#{(set_val=123)dsVal}123", null);
        tcs.put("#{(set_val=123dsVal}", null);

        tcs.put("${(set_val=123)dsVal}${(set_val=456)dsVal}", "set_val=123)dsVal}${(set_val=456");
        tcs.put("1970-01-01$#{(set_val=123)dsVal}", "set_val=123");
        tcs.put("1970-01-01{(set_val=123)dsVal}123", null);
        tcs.put("1970-01-01$#{(${(set_val=123)})dsVal}", "${(set_val=123)}");
        tcs.put("1970-01-01$#{(${(set_val=123\\)})dsVal}", "${(set_val=123\\)}");

        for (String tc : tcs.keySet()) {
            Assertions.assertEquals(tcs.get(tc), VarPoolUtils.findVarPool(tc));
        }
    }
}
