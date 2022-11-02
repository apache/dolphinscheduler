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

package org.apache.dolphinscheduler.plugin.task.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractTaskTest {

    @Test
    public void testFindFlinkJobId() {
        String jobId = "cca7bc1061d61cf15238e92312c2fc20";
        Pattern FLINK_APPLICATION_REGEX = Pattern.compile(TaskConstants.FLINK_APPLICATION_REGEX);
        Matcher matcher = FLINK_APPLICATION_REGEX.matcher("Job has been submitted with JobID " + jobId);
        String str = null;
        if (matcher.find()) {
            str = matcher.group();
        }
        Assertions.assertNotNull(str);
        Assertions.assertEquals(jobId, str.substring(6));
    }

}
