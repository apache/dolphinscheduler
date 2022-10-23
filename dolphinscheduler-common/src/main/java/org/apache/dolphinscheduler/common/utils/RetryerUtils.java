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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.constants.Constants;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

public class RetryerUtils {

    private static Retryer<Boolean> defaultRetryerResultCheck;
    private static Retryer<Boolean> defaultRetryerResultNoCheck;

    private RetryerUtils() {
        throw new UnsupportedOperationException("Construct RetryerUtils");
    }

    private static Retryer<Boolean> getDefaultRetryerResultNoCheck() {
        if (defaultRetryerResultNoCheck == null) {
            defaultRetryerResultNoCheck = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return defaultRetryerResultNoCheck;
    }

    /**
     * Gets default retryer.
     * the retryer will retry 3 times if exceptions throw
     * and wait 1 second between each retry
     *
     * @param checkResult true means the callable must return true before retrying
     *                    false means that retry callable only throw exceptions
     * @return the default retryer
     */
    public static Retryer<Boolean> getDefaultRetryer(boolean checkResult) {
        return checkResult ? getDefaultRetryer() : getDefaultRetryerResultNoCheck();
    }

    /**
     * Gets default retryer.
     * the retryer will retry 3 times if exceptions throw
     * and wait 1 second between each retry
     *
     * @return the default retryer
     */
    public static Retryer<Boolean> getDefaultRetryer() {
        if (defaultRetryerResultCheck == null) {
            defaultRetryerResultCheck = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfResult(Boolean.FALSE::equals)
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return defaultRetryerResultCheck;
    }

    /**
     * Use RETRYER to invoke the Callable
     *
     * @param callable    the callable
     * @param checkResult true means that retry callable before returning true
     *                    false means that retry callable only throw exceptions
     * @return the final result of callable
     * @throws ExecutionException the execution exception
     * @throws RetryException     the retry exception
     */
    public static Boolean retryCall(final Callable<Boolean> callable,
                                    boolean checkResult) throws ExecutionException, RetryException {
        return getDefaultRetryer(checkResult).call(callable);
    }

    /**
     * Use RETRYER to invoke the Callable before returning true
     *
     * @param callable the callable
     * @return the boolean
     * @throws ExecutionException the execution exception
     * @throws RetryException     the retry exception
     */
    public static Boolean retryCall(final Callable<Boolean> callable) throws ExecutionException, RetryException {
        return retryCall(callable, true);
    }
}
