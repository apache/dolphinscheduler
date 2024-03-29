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

import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RetryUtilsTest {

    @Test
    public void retryFunction() {
        Boolean retrySuccess = RetryUtils.retryFunction(() -> true);
        Assertions.assertTrue(retrySuccess);

        Assertions.assertThrows(RuntimeException.class, () -> {
            RetryUtils.retryFunction((Supplier<Boolean>) () -> {
                throw new RuntimeException("Test failed function");
            });
            // make sure RuntimeException thrown
            Assertions.fail();
        });

        long startTime = System.currentTimeMillis();
        Assertions.assertThrows(RuntimeException.class, () -> RetryUtils.retryFunction((Supplier<Boolean>) () -> {
            throw new RuntimeException("Test failed function");
        }, new RetryUtils.RetryPolicy(3, 1000L)));
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        Assertions.assertTrue(elapsedTime >= 3000L && elapsedTime < 4000L);
    }

}
