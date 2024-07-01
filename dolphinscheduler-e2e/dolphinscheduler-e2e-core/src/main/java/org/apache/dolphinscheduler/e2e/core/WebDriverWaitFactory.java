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

package org.apache.dolphinscheduler.e2e.core;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverWaitFactory {

    private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(500);

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Create a WebDriverWait instance with default timeout 60s and interval 100ms.
     */
    public static WebDriverWait createWebDriverWait(WebDriver driver) {
        return createWebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    public static WebDriverWait createWebDriverWait(WebDriver driver, Duration timeout) {
        return new WebDriverWait(driver, timeout, DEFAULT_INTERVAL);
    }

    public static WebDriverWait createWebDriverWait(WebDriver driver, Duration timeout, Duration interval) {
        return new WebDriverWait(driver, timeout, interval);
    }

}
