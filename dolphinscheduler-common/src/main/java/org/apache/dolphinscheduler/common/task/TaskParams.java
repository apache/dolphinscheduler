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

package org.apache.dolphinscheduler.common.task;

import java.util.Map;

public class TaskParams {

    private String rawScript;
    private Map<String, String>[] localParams;

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }

    public void setLocalParams(Map<String, String>[] localParams) {
        this.localParams = localParams;
    }

    public String getRawScript() {
        return rawScript;
    }

    public void setLocalParamValue(String prop, Object value) {
        if (localParams == null || value == null) {
            return;
        }
        for (int i = 0; i < localParams.length; i++) {
            if (localParams[i].get("prop").equals(prop)) {
                localParams[i].put("value", (String)value);
            }
        }
    }

    public void setLocalParamValue(Map<String, Object> propToValue) {
        if (localParams == null || propToValue == null) {
            return;
        }
        for (int i = 0; i < localParams.length; i++) {
            String prop = localParams[i].get("prop");
            if (propToValue.containsKey(prop)) {
                localParams[i].put("value",(String)propToValue.get(prop));
            }
        }
    }

    public String getLocalParamValue(String prop) {
        if (localParams == null) {
            return null;
        }
        for (int i = 0; i < localParams.length; i++) {
            String tmpProp = localParams[i].get("prop");
            if (tmpProp.equals(prop)) {
                return localParams[i].get("value");
            }
        }
        return null;
    }
    
    public Map<String, String>[] getLocalParams() {
        return localParams;
    }
} 