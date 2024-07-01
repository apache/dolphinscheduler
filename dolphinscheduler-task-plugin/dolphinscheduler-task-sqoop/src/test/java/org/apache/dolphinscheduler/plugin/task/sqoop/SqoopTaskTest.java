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

import org.apache.dolphinscheduler.common.log.SensitiveDataConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqoopTaskTest {

    @Test
    public void testSqoopPasswordMask() {
        final String originalScript =
                "sqoop import -D mapred.job.name=sqoop_task -m 1 --connect \"jdbc:mysql://localhost:3306/defuault\" --username root --password \"mypassword\" --table student --target-dir /sqoop_test --as-textfile";

        final String maskScript =
                "sqoop import -D mapred.job.name=sqoop_task -m 1 --connect \"jdbc:mysql://localhost:3306/defuault\" --username root --password \"**********\" --table student --target-dir /sqoop_test --as-textfile";

        SensitiveDataConverter.addMaskPattern(SqoopConstants.SQOOP_PASSWORD_REGEX);
        Assertions.assertEquals(maskScript, SensitiveDataConverter.maskSensitiveData(originalScript));
    }
}
