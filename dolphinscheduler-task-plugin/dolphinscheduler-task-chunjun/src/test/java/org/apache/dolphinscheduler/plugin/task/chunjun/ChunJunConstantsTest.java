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

package org.apache.dolphinscheduler.plugin.task.chunjun;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChunJunConstantsTest {

    private String flinkConfDir;

    private String flinkLibDir;

    private String hadoopConfDir;

    @BeforeEach
    public void setUp() {
        flinkConfDir = "${FLINK_HOME}/conf";
        flinkLibDir = "${FLINK_HOME}/lib";
        hadoopConfDir = "${HADOOP_HOME}/etc/hadoop";
    }

    @Test
    public void testEqualsString() {
        Assertions.assertEquals(ChunJunConstants.FLINK_CONF_DIR, flinkConfDir);
        Assertions.assertEquals(ChunJunConstants.FLINK_LIB_DIR, flinkLibDir);
        Assertions.assertEquals(ChunJunConstants.HADOOP_CONF_DIR, hadoopConfDir);
    }

}
