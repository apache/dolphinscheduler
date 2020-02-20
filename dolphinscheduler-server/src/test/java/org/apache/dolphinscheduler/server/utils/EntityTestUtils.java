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
package org.apache.dolphinscheduler.server.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EntityTestUtils {
 
    //实体化数据
    private static final Map<String, Object> STATIC_MAP = new HashMap<>();
 
    //忽略的函数方法method
    private static final String NO_NOTICE = "getClass,notify,notifyAll,wait,equals,hashCode,clone";
    
    static {
        STATIC_MAP.put("java.lang.Long", 1L);
        STATIC_MAP.put("java.lang.String", "test");
        STATIC_MAP.put("java.lang.Integer", 1);
        STATIC_MAP.put("int", 1);
        STATIC_MAP.put("long", 1L);
        STATIC_MAP.put("java.util.Date", new Date());
        STATIC_MAP.put("char", '1');
        STATIC_MAP.put("java.util.Map", new HashMap());
        STATIC_MAP.put("java.util.List", new ArrayList<>());
        STATIC_MAP.put("boolean", true);
    }

    public static void run(List<Class> classList)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Class temp : classList) {
            Object tempInstance = new Object();
            //执行构造函数
            Constructor[] constructors = temp.getConstructors();
            for (Constructor constructor : constructors) {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    tempInstance = constructor.newInstance();
                } else {
                    Object[] objects = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        objects[i] = STATIC_MAP.get(parameterTypes[i].getName());
                    }
                    tempInstance = constructor.newInstance(objects);
                }
            }
 
            //执行函数方法
            Method[] methods = temp.getMethods();
            for (final Method method : methods) {
                if (NO_NOTICE.contains(method.getName())) {
                    break;
                }
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 0) {
                    Object[] objects = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        objects[i] = STATIC_MAP.get(parameterTypes[i].getName());
                    }
                    method.invoke(tempInstance, objects);
                } else {
                    method.invoke(tempInstance);
                }
            }
            System.out.println(temp.getName());
        }
    }
}
