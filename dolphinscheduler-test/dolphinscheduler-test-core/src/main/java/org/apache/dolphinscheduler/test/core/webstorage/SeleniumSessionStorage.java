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

package org.apache.dolphinscheduler.test.core.webstorage;

import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import java.util.Set;

public class SeleniumSessionStorage implements SeleniumWebStorage{

    private final WebStorage webDriver;

    public SeleniumSessionStorage(WebStorage webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public String getAt(String key) {
        return this.getSessionStorage().getItem(key);
    }

    @Override
    public void putAt(String key, String value) {
        this.getSessionStorage().setItem(key, value);
    }

    @Override
    public void remove(String key) {
        this.getSessionStorage().removeItem(key);
    }

    @Override
    public Set<String> keySet() {
        return this.getSessionStorage().keySet();
    }

    @Override
    public int size() {
        return this.getSessionStorage().size();
    }

    @Override
    public void clear() {
        this.getSessionStorage().clear();
    }

    private SessionStorage getSessionStorage() {
        return this.webDriver.getSessionStorage();
    }
}
