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

package org.apache.dolphinscheduler.plugin.task.sqoop;

import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceHdfsParameter;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceHiveParameter;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetHdfsParameter;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetHiveParameter;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetMysqlParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * sqoop parameter entity test
 */
public class SqoopParameterEntityTest {

    @Test
    public void testEntity() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Class> classList = new ArrayList<>();
        classList.add(SourceMysqlParameter.class);
        classList.add(SourceHiveParameter.class);
        classList.add(SourceHdfsParameter.class);
        classList.add(SqoopParameters.class);
        classList.add(TargetMysqlParameter.class);
        classList.add(TargetHiveParameter.class);
        classList.add(TargetHdfsParameter.class);
        EntityTestUtils.run(classList);
    }
}
