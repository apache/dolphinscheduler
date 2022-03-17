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

package org.apache.dolphinscheduler.sdk.tea;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TeaConverter {

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> buildMap(TeaPair... pairs) {
        Map<String, T> map = new HashMap<String, T>();
        for (int i = 0; i < pairs.length; i++) {
            TeaPair pair = pairs[i];
            map.put(pair.key, (T) pair.value);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> merge(Class<T> t, Map<String, ?>... maps) {
        Map<String, T> out = new HashMap<String, T>();
        for (int i = 0; i < maps.length; i++) {
            Map<String, ?> map = maps[i];
            if (null == map) {
                continue;
            }
            Set<? extends Entry<String, ?>> entries = map.entrySet();
            for (Entry<String, ?> entry : entries) {
                if (null != entry.getValue()) {
                    out.put(entry.getKey(), (T) entry.getValue());
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> merge(Class<T> t, Object... maps) {
        Map<String, T> out = new HashMap<String, T>();
        Map<String, ?> map = null;
        for (int i = 0; i < maps.length; i++) {
            if (null == maps[i]) {
                continue;
            }
            if (TeaModel.class.isAssignableFrom(maps[i].getClass())) {
                map = TeaModel.buildMap((TeaModel) maps[i]);
            } else {
                map = (Map<String, T>) maps[i];
            }
            Set<? extends Entry<String, ?>> entries = map.entrySet();
            for (Entry<String, ?> entry : entries) {
                if (null != entry.getValue()) {
                    out.put(entry.getKey(), (T) entry.getValue());
                }
            }
        }
        return out;
    }

    public static Map<String, String> merge(Object... maps) {
        Map<String, String> out = new HashMap<>();
        Map<String, ?> map = null;
        for (int i = 0; i < maps.length; i++) {
            if (null == maps[i]) {
                continue;
            }
            if (TeaModel.class.isAssignableFrom(maps[i].getClass())) {
                map = TeaModel.buildMap((TeaModel) maps[i]);
            } else {
                map = (Map<String, String>) maps[i];
            }
            Set<? extends Entry<String, ?>> entries = map.entrySet();
            for (Entry<String, ?> entry : entries) {
                if (null != entry.getValue()) {
                    out.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return out;
    }
}
