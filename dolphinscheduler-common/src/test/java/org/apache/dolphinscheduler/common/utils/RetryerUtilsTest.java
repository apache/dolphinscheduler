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

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;

public class RetryerUtilsTest {

    @Test
    public void testDefaultRetryer() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer();
        Assertions.assertNotNull(retryer);
        try {
            boolean result = retryer.call(() -> true);
            Assertions.assertTrue(result);
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
        Retryer<Boolean> retryer1 = RetryerUtils.getDefaultRetryer(true);
        Assertions.assertEquals(retryer, retryer1);
    }

    @Test
    public void testDefaultRetryerResultCheck() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer();
        Assertions.assertNotNull(retryer);
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = retryer.call(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                });
                Assertions.assertEquals(finalExecTarget, execTime[0]);
                Assertions.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            retryer.call(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            });
            Assertions.fail("Retry times not reached");
        } catch (RetryException e) {
            Assertions.assertEquals(3, e.getNumberOfFailedAttempts());
            Assertions.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testDefaultRetryerResultNoCheck() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer(false);
        Assertions.assertNotNull(retryer);
        try {
            for (int execTarget = 1; execTarget <= 5; execTarget++) {
                int[] execTime = {0};
                boolean result = retryer.call(() -> {
                    execTime[0]++;
                    return execTime[0] > 1;
                });
                Assertions.assertEquals(1, execTime[0]);
                Assertions.assertFalse(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testRecallResultCheck() {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCall(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                });
                Assertions.assertEquals(finalExecTarget, execTime[0]);
                Assertions.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            RetryerUtils.retryCall(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            });
            Assertions.fail("Recall times not reached");
        } catch (RetryException e) {
            Assertions.assertEquals(3, e.getNumberOfFailedAttempts());
            Assertions.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testRecallResultCheckWithPara() {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCall(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                }, true);
                Assertions.assertEquals(finalExecTarget, execTime[0]);
                Assertions.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            RetryerUtils.retryCall(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            }, true);
            Assertions.fail("Recall times not reached");
        } catch (RetryException e) {
            Assertions.assertEquals(3, e.getNumberOfFailedAttempts());
            Assertions.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testRecallResultNoCheck() {
        try {
            for (int execTarget = 1; execTarget <= 5; execTarget++) {
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCall(() -> {
                    execTime[0]++;
                    return execTime[0] > 1;
                }, false);
                Assertions.assertEquals(1, execTime[0]);
                Assertions.assertFalse(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    private void testRetryExceptionWithPara(boolean checkResult) {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCall(() -> {
                    execTime[0]++;
                    if (execTime[0] != finalExecTarget) {
                        throw new IllegalArgumentException(String.valueOf(execTime[0]));
                    }
                    return true;
                }, checkResult);
                Assertions.assertEquals(finalExecTarget, execTime[0]);
                Assertions.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            RetryerUtils.retryCall(() -> {
                execTime[0]++;
                if (execTime[0] != 4) {
                    throw new IllegalArgumentException(String.valueOf(execTime[0]));
                }
                return true;
            }, checkResult);
            Assertions.fail("Recall times not reached");
        } catch (RetryException e) {
            Assertions.assertEquals(3, e.getNumberOfFailedAttempts());
            Assertions.assertEquals(3, execTime[0]);
            Assertions.assertNotNull(e.getCause());
            Assertions.assertEquals(3, Integer.parseInt(e.getCause().getMessage()));
        } catch (ExecutionException e) {
            Assertions.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testRetryException() {
        testRetryExceptionWithPara(true);
        testRetryExceptionWithPara(false);
    }
}
