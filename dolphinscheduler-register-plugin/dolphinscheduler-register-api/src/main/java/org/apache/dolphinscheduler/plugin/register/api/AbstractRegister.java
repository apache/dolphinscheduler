package org.apache.dolphinscheduler.plugin.register.api;/*
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

import org.apache.dolphinscheduler.spi.register.Register;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractRegister implements Register {

    public static Map<String, List> childrenDataCache = new HashMap<>();

    public static Map<String, String> dataCache = new HashMap<>();

    public static Map<String, SubscribeListener> subscribeListenerMap = new HashMap<>();


    public String getByCache(String key){
        if(dataCache.get(key).isEmpty()){
            return get(key);
        }
        return dataCache.get(key);
    }

    @Override
    public void register(Map<String, Object> registerData) {
        System.out.println("ssss");
    }

    @Override
    public void subscribe(String key, SubscribeListener subscribeListener) {

    }

    @Override
    public void unsubscribe(String key, SubscribeListener subscribeListener) {

    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void persist(String key, String value) {

    }

    @Override
    public void update(String key, String value) {

    }



    @Override
    public List<String> getChildren(String path) {
        return null;
    }

    @Override
    public String getData(String key) {
        return null;
    }
}
