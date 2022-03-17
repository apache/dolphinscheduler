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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class TeaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public String code;
    public String message;
    public Map<String, Object> data;

    public TeaException() {
    }

    public TeaException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeaException(Map<String, ?> map) {
        this.setCode(String.valueOf(map.get("code")));
        this.setMessage(String.valueOf(map.get("message")));
        Object obj = map.get("data");
        if (obj == null) {
            return;
        }
        if (obj instanceof Map) {
            data = (Map<String, Object>) obj;
            return;
        }
        Map<String, Object> hashMap = new HashMap<String, Object>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                hashMap.put(field.getName(), field.get(obj));
            } catch (Exception e) {
                continue;
            }
        }
        this.data = hashMap;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}