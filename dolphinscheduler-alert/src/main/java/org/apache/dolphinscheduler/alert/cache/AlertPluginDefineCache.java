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

package org.apache.dolphinscheduler.alert.cache;

import java.util.HashMap;
import java.util.Map;

public class AlertPluginDefineCache {

    public AlertPluginDefineCache() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * k->pluginDefineId v->pluginDefineName
     */
    private static Map<Integer, String> pluginDefineMap = new HashMap<>();

    public static void addPlugin(int id, String name) {
        pluginDefineMap.put(id, name);
    }

    public static String getNameById(int id) {
        return pluginDefineMap.get(id);
    }
}
