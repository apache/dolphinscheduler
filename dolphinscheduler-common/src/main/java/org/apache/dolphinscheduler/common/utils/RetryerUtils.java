package org.apache.dolphinscheduler.common.utils;

import com.github.rholder.retry.*;
import org.apache.dolphinscheduler.common.Constants;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The Retryer util.
 */
public class RetryerUtils {
    private static Retryer<Boolean> DEFAULT_RETRYER_RESULT_CHECK;
    private static Retryer<Boolean> DEFAULT_RETRYER_RESULT_NO_CHECK;

    private static Retryer<Boolean> getDefaultRetryerResultNoCheck() {
        if (DEFAULT_RETRYER_RESULT_NO_CHECK == null) {
            DEFAULT_RETRYER_RESULT_NO_CHECK = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return DEFAULT_RETRYER_RESULT_NO_CHECK;
    }

    /**
     * Gets default retryer.
     * the retryer will retry retry 3 times if exceptions throw
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
     * the retryer will retry retry 3 times if exceptions throw
     * and wait 1 second between each retry
     *
     * @return the default retryer
     */
    public static Retryer<Boolean> getDefaultRetryer() {
        if (DEFAULT_RETRYER_RESULT_CHECK == null) {
            DEFAULT_RETRYER_RESULT_CHECK = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfResult(Boolean.FALSE::equals)
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return DEFAULT_RETRYER_RESULT_CHECK;
    }

    /**
     * Use DEFAULT_RETRYER to invoke the Callable
     *
     * @param callable    the callable
     * @param checkResult true means that retry callable before returning true
     *                    false means that retry callable only throw exceptions
     * @return the final result of callable
     * @throws ExecutionException the execution exception
     * @throws RetryException     the retry exception
     */
    public static Boolean retryCall(final Callable<Boolean> callable, boolean checkResult) throws ExecutionException, RetryException {
        return getDefaultRetryer(checkResult).call(callable);
    }

    /**
     * Retry callable before returning true
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
