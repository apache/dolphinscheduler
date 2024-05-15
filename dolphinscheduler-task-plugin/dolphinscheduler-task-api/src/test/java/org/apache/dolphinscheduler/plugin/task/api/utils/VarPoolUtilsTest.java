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

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

class VarPoolUtilsTest {

    @Test
    void mergeVarPool() {
        List<Property> varpool1 = null;
        List<Property> varpool2 = null;
        Truth.assertThat(VarPoolUtils.mergeVarPool(varpool1, varpool2)).isNull();

        // Override the value of the same property
        // Merge the property with different key.
        varpool1 = Lists.newArrayList(new Property("name", Direct.OUT, DataType.VARCHAR, "tom"));
        varpool2 = Lists.newArrayList(
                new Property("name", Direct.OUT, DataType.VARCHAR, "tim"),
                new Property("age", Direct.OUT, DataType.INTEGER, "10"));

        Truth.assertThat(VarPoolUtils.mergeVarPool(varpool1, varpool2))
                .containsExactly(
                        new Property("name", Direct.OUT, DataType.VARCHAR, "tim"),
                        new Property("age", Direct.OUT, DataType.INTEGER, "10"));

    }

}
