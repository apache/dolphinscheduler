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

package org.apache.dolphinscheduler.test.core.js;

import org.apache.dolphinscheduler.test.core.Browser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class JavascriptInterface {
    private Browser browser = null;

    public JavascriptInterface(Browser browser) {
        this.browser = browser;
    }

    public Object execjs(String script, Object... args) {
        WebDriver driver = browser.getDriver();

        if (!(driver instanceof JavascriptExecutor)) {
            throw new RuntimeException("driver " + driver + " can not execute javascript");
        }
        return ((JavascriptExecutor) driver).executeScript(script, args);

    }
}
