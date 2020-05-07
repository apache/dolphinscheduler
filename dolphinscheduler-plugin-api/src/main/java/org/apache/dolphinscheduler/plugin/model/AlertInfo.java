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
package org.apache.dolphinscheduler.plugin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * AlertInfo
 */
public class AlertInfo {

    private Map<String, Object> alertProps;

    private AlertData alertData;

    public AlertInfo() {
        this.alertProps = new HashMap<>();
    }

    public Map<String, Object> getAlertProps() {
        return alertProps;
    }

    public AlertInfo setAlertProps(Map<String, Object> alertProps) {
        this.alertProps = alertProps;
        return this;
    }

    public AlertInfo addProp(String key, Object value) {
        this.alertProps.put(key, value);
        return this;
    }

    public Object getProp(String key) {
        return this.alertProps.get(key);
    }

    public AlertData getAlertData() {
        return alertData;
    }

    public AlertInfo setAlertData(AlertData alertData) {
        this.alertData = alertData;
        return this;
    }
}
