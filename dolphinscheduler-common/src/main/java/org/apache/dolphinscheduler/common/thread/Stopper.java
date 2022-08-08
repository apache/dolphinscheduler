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

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.experimental.UtilityClass;

/**
 * If the process closes, a signal is placed as true, and all threads get this flag to stop working.
 */
@UtilityClass
public class Stopper {

    private static final AtomicBoolean stoppedSignal = new AtomicBoolean(false);

    /**
     * Return the flag if the Server is stopped.
     *
     * @return True, if the server is stopped; False, the server is still running.
     */
    public static boolean isStopped() {
        return stoppedSignal.get();
    }

    /**
     * Return the flag if the Server is stopped.
     *
     * @return True, if the server is running, False, the server is stopped.
     */
    public static boolean isRunning() {
        return !stoppedSignal.get();
    }

    /**
     * Stop the server
     *
     * @return True, if the server stopped success. False, if the server is already stopped.
     */
    public static boolean stop() {
        return stoppedSignal.compareAndSet(false, true);
    }
}
