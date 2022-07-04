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

import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;

import java.util.Set;

public class SeleniumLocalStorage implements SeleniumWebStorage {

    private final WebStorage webStorage;


    public SeleniumLocalStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    @Override
    public String getAt(String key) {
        return this.getLocalStorage().getItem(key);
    }

    @Override
    public void putAt(String key, String value) {
        this.getLocalStorage().setItem(key, value);
    }

    @Override
    public void remove(String key) {
        this.getLocalStorage().removeItem(key);
    }

    @Override
    public Set<String> keySet() {
        return this.getLocalStorage().keySet();
    }

    @Override
    public int size() {
        return this.getLocalStorage().size();
    }

    @Override
    public void clear() {
        this.getLocalStorage().clear();
    }

    private LocalStorage getLocalStorage() {
        return webStorage.getLocalStorage();
    }

}
