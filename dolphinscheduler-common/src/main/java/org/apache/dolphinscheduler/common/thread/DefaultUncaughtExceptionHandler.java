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

package org.apache.dolphinscheduler.common.thread;

import java.util.concurrent.atomic.LongAdder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final DefaultUncaughtExceptionHandler INSTANCE = new DefaultUncaughtExceptionHandler();

    private static final LongAdder uncaughtExceptionCount = new LongAdder();

    private DefaultUncaughtExceptionHandler() {
    }

    public static DefaultUncaughtExceptionHandler getInstance() {
        return INSTANCE;
    }

    public static long getUncaughtExceptionCount() {
        return uncaughtExceptionCount.longValue();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        uncaughtExceptionCount.add(1);
        log.error("Caught an exception in {}.", t, e);
    }
}
