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

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class RetryerUtilsTest {

    @Test
    public void testDefaultRetryer() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer();
        Assert.assertNotNull(retryer);
        try {
            boolean result = retryer.call(() -> true);
            Assert.assertTrue(result);
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
        Retryer<Boolean> retryer1 = RetryerUtils.getDefaultRetryer(true);
        Assert.assertEquals(retryer, retryer1);
    }

    @Test
    public void testDefaultRetryerResultCheck() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer();
        Assert.assertNotNull(retryer);
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = retryer.call(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                });
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            retryer.call(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            });
            Assert.fail("Retry times not reached");
        } catch (RetryException e) {
            Assert.assertEquals(3, e.getNumberOfFailedAttempts());
            Assert.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testDefaultRetryerResultNoCheck() {
        Retryer<Boolean> retryer = RetryerUtils.getDefaultRetryer(false);
        Assert.assertNotNull(retryer);
        try {
            for (int execTarget = 1; execTarget <= 5; execTarget++) {
                int[] execTime = {0};
                boolean result = retryer.call(() -> {
                    execTime[0]++;
                    return execTime[0] > 1;
                });
                Assert.assertEquals(1, execTime[0]);
                Assert.assertFalse(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
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
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            RetryerUtils.retryCall(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            });
            Assert.fail("Recall times not reached");
        } catch (RetryException e) {
            Assert.assertEquals(3, e.getNumberOfFailedAttempts());
            Assert.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assert.fail("Retry call failed " + e.getMessage());
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
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            RetryerUtils.retryCall(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            }, true);
            Assert.fail("Recall times not reached");
        } catch (RetryException e) {
            Assert.assertEquals(3, e.getNumberOfFailedAttempts());
            Assert.assertEquals(3, execTime[0]);
        } catch (ExecutionException e) {
            Assert.fail("Retry call failed " + e.getMessage());
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
                Assert.assertEquals(1, execTime[0]);
                Assert.assertFalse(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
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
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (ExecutionException | RetryException e) {
            Assert.fail("Retry call failed " + e.getMessage());
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
            Assert.fail("Recall times not reached");
        } catch (RetryException e) {
            Assert.assertEquals(3, e.getNumberOfFailedAttempts());
            Assert.assertEquals(3, execTime[0]);
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(3, Integer.parseInt(e.getCause().getMessage()));
        } catch (ExecutionException e) {
            Assert.fail("Retry call failed " + e.getMessage());
        }
    }

    @Test
    public void testRetryException() {
        testRetryExceptionWithPara(true);
        testRetryExceptionWithPara(false);
    }

    @Test
    public void testRetrySilent() {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCallSilent(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                });
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            boolean result = RetryerUtils.retryCallSilent(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            });
            Assert.assertFalse(result);
        } catch (Exception e) {
            Assert.fail("Unexpected exception  " + e.getMessage());
        }
    }

    @Test
    public void testRetrySilentWithPara() {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCallSilent(() -> {
                    execTime[0]++;
                    return execTime[0] == finalExecTarget;
                }, true);
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            boolean result = RetryerUtils.retryCallSilent(() -> {
                execTime[0]++;
                return execTime[0] == 4;
            }, true);
            Assert.assertFalse(result);
        } catch (Exception e) {
            Assert.fail("Unexpected exception  " + e.getMessage());
        }
    }
    @Test
    public void testRetrySilentNoCheckResult(){
        try {
            for (int execTarget = 1; execTarget <= 5; execTarget++) {
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCallSilent(() -> {
                    execTime[0]++;
                    return execTime[0] > 1;
                }, false);
                Assert.assertEquals(1, execTime[0]);
                Assert.assertFalse(result);
            }
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
    }
    private void testRetrySilentExceptionWithPara(boolean checkResult) {
        try {
            for (int execTarget = 1; execTarget <= 3; execTarget++) {
                int finalExecTarget = execTarget;
                int[] execTime = {0};
                boolean result = RetryerUtils.retryCallSilent(() -> {
                    execTime[0]++;
                    if (execTime[0] != finalExecTarget) {
                        throw new IllegalArgumentException(String.valueOf(execTime[0]));
                    }
                    return true;
                }, checkResult);
                Assert.assertEquals(finalExecTarget, execTime[0]);
                Assert.assertTrue(result);
            }
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
        int[] execTime = {0};
        try {
            boolean result = RetryerUtils.retryCallSilent(() -> {
                execTime[0]++;
                if (execTime[0] != 4) {
                    throw new IllegalArgumentException(String.valueOf(execTime[0]));
                }
                return true;
            }, checkResult);
            Assert.assertFalse(result);
        } catch (Exception e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
    }
    @Test
    public void testRetrySilentException() {
        testRetrySilentExceptionWithPara(true);
        testRetrySilentExceptionWithPara(false);
    }
}
