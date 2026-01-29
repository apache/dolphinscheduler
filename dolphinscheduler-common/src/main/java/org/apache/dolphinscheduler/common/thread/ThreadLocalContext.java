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

/**
 * A utility class to manage timezone context using ThreadLocal.
 * This allows each thread to have its own timezone value, which is useful in
 * multi-threaded environments such as web applications where each request
 * may need to operate in a different timezone.
 *
 * Note: Always call {@link #removeTimezone()} at the end of a request or task
 * to prevent memory leaks and context pollution in thread pool environments.
 */
public class ThreadLocalContext {

    /**
     * ThreadLocal variable to hold the timezone string for the current thread.
     * Each thread will have its own copy of the timezone value.
     */
    private static final ThreadLocal<String> TIMEZONE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Sets the timezone for the current thread.
     *
     * @param timezone the timezone ID (e.g., "UTC", "Asia/Shanghai", "America/New_York")
     */
    public static void setTimezone(String timezone) {
        TIMEZONE_THREAD_LOCAL.set(timezone);
    }

    /**
     * Retrieves the timezone for the current thread.
     *
     * @return the timezone string set for the current thread, or null if not set
     */
    public static String getTimezone() {
        return TIMEZONE_THREAD_LOCAL.get();
    }

    /**
     * Removes the timezone value for the current thread.
     * This method should be called to clean up the thread-local value, especially
     * when using thread pools (e.g., in web servers), to prevent memory leaks
     * and unintended data leakage between requests.
     */
    public static void removeTimezone() {
        TIMEZONE_THREAD_LOCAL.remove();
    }
}
