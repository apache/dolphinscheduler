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

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TeaModel {

    public Map<String, Object> toMap() {
        return changeToMap(this, true);
    }

    public static Map<String, Object> toMap(Object object) {
        return toMap(object, true);
    }

    private static Map<String, Object> toMap(Object object, Boolean exceptStream) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != object && object instanceof Map) {
            return (Map<String, Object>) object;
        }
        if (null == object || !TeaModel.class.isAssignableFrom(object.getClass())) {
            return map;
        }
        map = changeToMap(object, exceptStream);
        return map;
    }

    private Map<String, Object> toMap(Boolean exceptStream) {
        return changeToMap(this, exceptStream);
    }

    private static Map<String, Object> changeToMap(Object object, Boolean exceptStream) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            for (Field field : object.getClass().getFields()) {
                NameInMap anno = field.getAnnotation(NameInMap.class);
                String key;
                if (anno == null) {
                    key = field.getName();
                } else {
                    key = anno.value();
                }
                if (null != field.get(object) && List.class.isAssignableFrom(field.get(object).getClass())) {
                    List<Object> arrayField = (List<Object>) field.get(object);
                    List<Object> fieldList = new ArrayList<Object>();
                    for (int i = 0; i < arrayField.size(); i++) {
                        fieldList.add(parseObject(arrayField.get(i)));
                    }
                    map.put(key, fieldList);
                } else if (null != field.get(object) && TeaModel.class.isAssignableFrom(field.get(object).getClass())) {
                    map.put(key, TeaModel.toMap(field.get(object), exceptStream));
                } else if (null != field.get(object) && Map.class.isAssignableFrom(field.get(object).getClass())) {
                    Map<String, Object> valueMap = (Map<String, Object>) field.get(object);
                    Map<String, Object> result = new HashMap<String, Object>();
                    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                        result.put(entry.getKey(), parseObject(entry.getValue()));
                    }
                    map.put(key, result);
                } else if (exceptStream && null != field.get(object) && InputStream.class.isAssignableFrom(field.get(object).getClass())) {
                    continue;
                } else if (exceptStream && null != field.get(object) && OutputStream.class.isAssignableFrom(field.get(object).getClass())) {
                    continue;
                } else {
                    map.put(key, field.get(object));
                }
            }
        } catch (Exception e) {
            throw new TeaException(e.getMessage(), e);
        }
        return map;
    }


    public static Object parseObject(Object o) {
        if (null == o) {
            return o;
        }
        Class clazz = o.getClass();
        if (List.class.isAssignableFrom(clazz)) {
            List<Object> list = (List<Object>) o;
            List<Object> result = new ArrayList<Object>();
            for (Object object : list) {
                result.add(parseObject(object));
            }
            return result;
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<String, Object> map = (Map<String, Object>) o;
            Map<String, Object> result = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), parseObject(entry.getValue()));
            }
            return result;
        } else if (TeaModel.class.isAssignableFrom(clazz)) {
            return ((TeaModel) o).toMap(false);
        } else {
            return o;
        }
    }

    private static Object buildObject(Object o, Class self, Type subType) {
        Class valueClass = o.getClass();
        if (Map.class.isAssignableFrom(self) && Map.class.isAssignableFrom(valueClass)) {
            Map<String, Object> valueMap = (Map<String, Object>) o;
            Map<String, Object> result = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                if (null == subType || subType instanceof WildcardType) {
                    result.put(entry.getKey(), entry.getValue());
                } else if (subType instanceof Class) {
                    result.put(entry.getKey(), buildObject(entry.getValue(), (Class) subType, null));
                } else {
                    ParameterizedType parameterizedType = (ParameterizedType) subType;
                    Type[] types = parameterizedType.getActualTypeArguments();
                    result.put(entry.getKey(), buildObject(entry.getValue(), (Class) parameterizedType.getRawType(), types[types.length - 1]));
                }
            }
            return result;
        } else if (List.class.isAssignableFrom(self) && List.class.isAssignableFrom(valueClass)) {
            List<Object> valueList = (List<Object>) o;
            List<Object> result = new ArrayList<Object>();
            for (Object object : valueList) {
                if (null == subType || subType instanceof WildcardType) {
                    result.add(object);
                } else if (subType instanceof Class) {
                    result.add(buildObject(object, (Class) subType, null));
                } else {
                    ParameterizedType parameterizedType = (ParameterizedType) subType;
                    Type[] types = parameterizedType.getActualTypeArguments();
                    result.add(buildObject(object, (Class) parameterizedType.getRawType(), types[types.length - 1]));
                }
            }
            return result;
        } else if (TeaModel.class.isAssignableFrom(self) && Map.class.isAssignableFrom(valueClass)) {
            try {
                return TeaModel.toModel((Map<String, Object>) o, (TeaModel) self.newInstance());
            } catch (Exception e) {
                throw new TeaException(e.getMessage(), e);
            }
        } else {
            return o;
        }
    }

    private static Type getType(Field field, int index) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[index];
        return actualTypeArgument;
    }


    @SuppressWarnings("unchecked")
    public static <T extends TeaModel> T toModel(Map<String, ?> map, T model) {
        T result = model;
        for (Field field : result.getClass().getFields()) {
            NameInMap anno = field.getAnnotation(NameInMap.class);
            String key;
            if (anno == null) {
                key = field.getName();
            } else {
                key = anno.value();
            }
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            result = setTeaModelField(result, field, value, false);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends TeaModel> T setTeaModelField(T model, Field field, Object value, boolean userBuild) {
        try {
            Class<?> clazz = field.getType();
            Object resultValue = parseNumber(value, clazz);
            T result = model;
            if (TeaModel.class.isAssignableFrom(clazz)) {
                Object data = clazz.getDeclaredConstructor().newInstance();
                if (userBuild) {
                    field.set(result, TeaModel.build(TeaModel.toMap(resultValue, false), (TeaModel) data));
                } else if (!userBuild && Map.class.isAssignableFrom(resultValue.getClass())) {
                    field.set(result, TeaModel.toModel((Map<String, Object>) resultValue, (TeaModel) data));
                } else {
                    field.set(result, resultValue);
                }
            } else if (Map.class.isAssignableFrom(clazz)) {
                field.set(result, buildObject(resultValue, Map.class, getType(field, 1)));
            } else if (List.class.isAssignableFrom(clazz)) {
                field.set(result, buildObject(resultValue, List.class, getType(field, 0)));
            } else {
                field.set(result, confirmType(clazz, resultValue));
            }
            return result;
        } catch (Exception e) {
            throw new TeaException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends TeaModel> T build(Map<String, ?> map, T model) {
        T result = model;
        for (Field field : model.getClass().getFields()) {
            String key = field.getName();
            Object value = map.get(key);
            if (value == null) {
                NameInMap anno = field.getAnnotation(NameInMap.class);
                if (null == anno) {
                    continue;
                }
                key = anno.value();
                value = map.get(key);
                if (null == value) {
                    continue;
                }
            }
            result = setTeaModelField(result, field, value, true);
        }
        return result;
    }

    private static Object parseNumber(Object value, Class clazz) {
        BigDecimal bigDecimal;
        if (value instanceof Double && (clazz == Long.class || clazz == long.class)) {
            bigDecimal = new BigDecimal(value.toString());
            return bigDecimal.longValue();
        }
        if (value instanceof Double && (clazz == Integer.class || clazz == int.class)) {
            bigDecimal = new BigDecimal(value.toString());
            return bigDecimal.intValue();
        }
        if (value instanceof Double && (clazz == Float.class || clazz == float.class)) {
            bigDecimal = new BigDecimal(value.toString());
            return bigDecimal.floatValue();
        }
        return value;
    }

    public void validate() {
        Field[] fields = this.getClass().getFields();
        Object object;
        Validation validation;
        String pattern;
        int maxLength;
        int minLength;
        double maximum;
        double minimum;
        boolean required;
        try {
            for (int i = 0; i < fields.length; i++) {
                object = fields[i].get(this);
                validation = fields[i].getAnnotation(Validation.class);
                if (null != validation) {
                    required = validation.required();
                } else {
                    required = false;
                }
                if (required && null == object) {
                    throw new ValidateException("Field " + fields[i].getName() + " is required");
                }
                if (null != validation && null != object) {
                    pattern = validation.pattern();
                    maxLength = validation.maxLength();
                    minLength = validation.minLength();
                    maximum = validation.maximum();
                    minimum = validation.minimum();
                    if (!"".equals(pattern) || maxLength > 0 || minLength > 0 || maximum != Double.MAX_VALUE || minimum != Double.MIN_VALUE) {
                        determineType(fields[i].getType(), object, pattern, maxLength, minLength, maximum, minimum, fields[i].getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ValidateException(e.getMessage(), e);
        }
    }

    private void determineType(Class clazz, Object object, String pattern, int maxLength, int minLength, double maximum, double minimum, String fieldName) {
        if (Map.class.isAssignableFrom(clazz)) {
            validateMap(pattern, maxLength, minLength, maximum, minimum, (Map<String, Object>) object, fieldName);
        } else if (TeaModel.class.isAssignableFrom(clazz)) {
            ((TeaModel) object).validate();
        } else if (List.class.isAssignableFrom(clazz)) {
            List<?> list = (List<?>) object;
            for (int j = 0; j < list.size(); j++) {
                determineType(list.get(j).getClass(), list.get(j), pattern, maxLength, minLength, maximum, minimum, fieldName);
            }
        } else if (clazz.isArray()) {
            Object[] objects = (Object[]) object;
            for (int j = 0; j < objects.length; j++) {
                determineType(clazz.getComponentType(), objects[j], pattern, maxLength, minLength, maximum, minimum, fieldName);
            }
        } else if (Number.class.isAssignableFrom(clazz)) {
            double value = Double.valueOf(object.toString());
            if (value > maximum) {
                throw new ValidateException(this.getClass().getName() + "." + fieldName + " exceeds the maximum");
            }
            if (value < minimum) {
                throw new ValidateException(this.getClass().getName() + "." + fieldName + " less than minimum");
            }
        } else {
            String value = String.valueOf(object);
            if (maxLength > 0 && value.length() > maxLength) {
                throw new ValidateException(this.getClass().getName() + "." + fieldName + " exceeds the maximum length");
            }
            if (minLength > 0 && value.length() < minLength) {
                throw new ValidateException(this.getClass().getName() + "." + fieldName + " less than minimum length");
            }
            if (!"".equals(pattern) && !Pattern.matches(pattern, value)) {
                throw new ValidateException(this.getClass().getName() + "." + fieldName + " regular match failed");
            }
        }
    }

    private void validateMap(String pattern, int maxLength, int minLength, double maximum, double minimum, Map<String, Object> map, String fieldName) {
        for (Map.Entry entry : map.entrySet()) {
            if (entry.getValue() != null) {
                determineType(entry.getValue().getClass(), entry.getValue(), pattern, maxLength, minLength, maximum, minimum, fieldName);
            }
        }
    }

    public static Map<String, Object> buildMap(TeaModel teaModel) {
        if (null == teaModel) {
            return null;
        } else {
            return teaModel.toMap();
        }
    }

    public static void validateParams(TeaModel teaModel, String paramName) {
        if (null == teaModel) {
            throw new ValidateException("parameter " + paramName + " is not allowed as null");
        }
        teaModel.validate();
    }

    public static Object confirmType(Class expect, Object object) throws Exception {
        if (String.class.isAssignableFrom(expect)) {
            if (object instanceof Number || object instanceof Boolean) {
                return object.toString();
            }
        } else if (Boolean.class.isAssignableFrom(expect)) {
            if (object instanceof String) {
                return Boolean.parseBoolean(String.valueOf(object));
            } else if (object instanceof Integer) {
                if (object.toString().equals("1")) {
                    return true;
                } else if (object.toString().equals("0")) {
                    return false;
                }
            }
        } else if (Integer.class.isAssignableFrom(expect)) {
            if (object instanceof String) {
                return Integer.parseInt(object.toString());
            }
            // 判断数值大小是否超过期望数据类型的上限，如果不超过则强制转换，如果超过则报错
            if (object instanceof Long && ((Long) object).longValue() <= Integer.MAX_VALUE) {
                return Integer.parseInt(object.toString());
            }
        } else if (Long.class.isAssignableFrom(expect)) {
            if (object instanceof String || object instanceof Integer) {
                return Long.parseLong(object.toString());
            }
        } else if (Float.class.isAssignableFrom(expect)) {
            if (object instanceof String || object instanceof Integer || object instanceof Long) {
                return Float.parseFloat(object.toString());
            }
            // 判断数值大小是否超过期望数据类型的上限，如果不超过则强制转换，如果超过则报错
            if (object instanceof Double && ((Double) object).doubleValue() <= Float.MAX_VALUE) {
                return Float.parseFloat(object.toString());
            }
        } else if (Double.class.isAssignableFrom(expect)) {
            if (object instanceof String || object instanceof Integer || object instanceof Long || object instanceof Float) {
                return Double.parseDouble(object.toString());
            }
        }
        return object;
    }

    public static <T> T toModel(Map<String, ?> map, Type jsonType) {
        // Fix GSON ISSUE on Linux: Invalid time zone indicator ' '
        // refer: https://github.com/google/gson/issues/1297
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.fromJson(gson.toJson(map), jsonType);
    }

    // for task
    public static <T> T toModel(Map<String, ?> map, Class<T> clazz) {
        String jsonStr = JSONUtils.toJsonString(map);
        return JSONUtils.parseObject(jsonStr,clazz);
    }
}

