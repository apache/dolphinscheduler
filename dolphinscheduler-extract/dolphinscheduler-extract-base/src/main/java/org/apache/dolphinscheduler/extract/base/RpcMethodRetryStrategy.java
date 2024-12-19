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

package org.apache.dolphinscheduler.extract.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ConnectException;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcMethodRetryStrategy {

    /**
     * The maximum number of retries. Default is 3, which means that the method is retried at most 3 times, including the first call.
     */
    int maxRetryTimes() default 3;

    /**
     * The interval between retries, in milliseconds. If the value is less than or equal to 0, no interval is set.
     */
    long retryInterval() default 0;

    /**
     * Which exception to retry.
     * <p> Default is {@link ConnectException}.
     */
    Class<? extends Throwable>[] retryFor() default {ConnectException.class};

}
