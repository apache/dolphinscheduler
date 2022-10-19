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

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RetryUtils {

    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy(3, 1000L);

    /**
     * Retry to execute the given function with the default retry policy.
     */
    public static <T> T retryFunction(@NonNull Supplier<T> supplier) {
        return retryFunction(supplier, DEFAULT_RETRY_POLICY);
    }

    /**
     * Retry to execute the given function with the given retry policy, the retry policy is used to defined retryTimes and retryInterval.
     * This method will sleep for retryInterval when execute given supplier failure.
     */
    public static <T> T retryFunction(@NonNull Supplier<T> supplier, @NonNull RetryPolicy retryPolicy) {
        int retryCount = 0;
        long retryInterval = 0L;
        while (true) {
            try {
                return supplier.get();
            } catch (Exception ex) {
                if (retryCount == retryPolicy.getMaxRetryTimes()) {
                    throw ex;
                }
                retryCount++;
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("The current thread is interrupted, will stop retry", e);
                }
            }
        }
    }

    @Data
    public static final class RetryPolicy {

        /**
         * The max retry times
         */
        private final int maxRetryTimes;
        /**
         * The retry interval, if the give function is failed, will sleep the retry interval milliseconds and retry again.
         */
        private final long retryInterval;

    }
}
