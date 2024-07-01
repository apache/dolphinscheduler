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

package org.apache.dolphinscheduler.data.quality.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Config
 */
public class Config {

    private Map<String, Object> configuration = new HashMap<>();

    public Config() {

    }

    public Config(Map<String, Object> configuration) {
        if (configuration != null) {
            this.configuration = configuration;
        }
    }

    public String getString(String key) {
        return configuration.get(key) == null ? null : String.valueOf(configuration.get(key));
    }

    public List<String> getStringList(String key) {
        return (List<String>) configuration.get(key);
    }

    public Integer getInt(String key) {
        return Integer.valueOf(String.valueOf(configuration.get(key)));
    }

    public Boolean getBoolean(String key) {
        return Boolean.valueOf(String.valueOf(configuration.get(key)));
    }

    public Double getDouble(String key) {
        return Double.valueOf(String.valueOf(configuration.get(key)));
    }

    public Long getLong(String key) {
        return Long.valueOf(String.valueOf(configuration.get(key)));
    }

    public Boolean has(String key) {
        return configuration.get(key) != null;
    }

    public Set<Entry<String, Object>> entrySet() {
        return configuration.entrySet();
    }

    public boolean isEmpty() {
        return configuration.size() <= 0;
    }

    public boolean isNotEmpty() {
        return configuration.size() > 0;
    }

    public void put(String key, Object value) {
        this.configuration.put(key, value);
    }

    public void merge(Map<String, Object> configuration) {
        configuration.forEach(this.configuration::putIfAbsent);
    }

    public Map<String, Object> configurationMap() {
        return this.configuration;
    }
}
