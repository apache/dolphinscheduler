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

package org.apache.dolphinscheduler.plugin.alert.script;

import java.util.HashMap;
import java.util.Map;

/**
 * ScriptType
 */
public enum ScriptType {


    SHELL(0, "SHELL"),
    ;

    ScriptType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    private static final Map<Integer, ScriptType> SCRIPT_TYPE_MAP = new HashMap<>();

    static {
        for (ScriptType scriptType : ScriptType.values()) {
            SCRIPT_TYPE_MAP.put(scriptType.code, scriptType);
        }
    }

    public static ScriptType of(Integer code) {
        if (SCRIPT_TYPE_MAP.containsKey(code)) {
            return SCRIPT_TYPE_MAP.get(code);
        }
        throw new IllegalArgumentException("invalid code : " + code);
    }
}
