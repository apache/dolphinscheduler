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

package org.apache.dolphinscheduler.remote.rpc.config;

import org.apache.dolphinscheduler.remote.rpc.IUserService;
import org.apache.dolphinscheduler.remote.rpc.base.RpcService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;


import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceBean {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBean.class);

    private static Map<String, Object> serviceMap = new HashMap<>();

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private static synchronized void init() {
        Reflections f = new Reflections("org/apache/dolphinscheduler/remote/rpc");


        List<Class<?>> list = new ArrayList<>(f.getTypesAnnotatedWith(RpcService.class));
        list.forEach(rpcClass -> {
            RpcService rpcService = rpcClass.getAnnotation(RpcService.class);
            serviceMap.put(rpcService.value(), rpcClass);
        });
    }

    public static void main(String[] args) {
        init();
    }

    public static Class getServiceClass(String className) {
        if (initialized.get()) {
            return (Class) serviceMap.get(className);
        } else {
            init();
        }
        return (Class) serviceMap.get(className);
    }

}
