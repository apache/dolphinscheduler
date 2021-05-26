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

package org.apache.dolphinscheduler.spi.register;

import java.util.HashMap;

public class ListenerManager {

    /**
     * All message subscriptions must be subscribed uniformly at startup.
     * A node path only supports one listener
     */
    private static HashMap<String, SubscribeListener> listeners = new HashMap<>();

    public static boolean checkHasListeners(String path) {
        return null != listeners.get(path);
    }

    public static void addListener(String path, SubscribeListener listener) {
        listeners.put(path, listener);
    }

    public static void removeListener(String path) {
        listeners.remove(path);
    }

    public static void dataChange(String path, DataChangeEvent dataChangeEvent) {
        SubscribeListener notifyListener = listeners.get(path);
        if (null == notifyListener) {
            return;
        }
        notifyListener.notify(dataChangeEvent);
    }

}
