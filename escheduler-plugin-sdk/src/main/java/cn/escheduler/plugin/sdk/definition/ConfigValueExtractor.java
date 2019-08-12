/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.definition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import cn.escheduler.plugin.sdk.credential.ClearCredentialValue;
import cn.escheduler.plugin.sdk.json.ObjectMapperFactory;
import cn.escheduler.plugin.api.ConfigDef;
import cn.escheduler.plugin.api.credential.CredentialValue;
import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.Utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ConfigValueExtractor {
    private static final ConfigValueExtractor EXTRACTOR = new ConfigValueExtractor() {};

    public static ConfigValueExtractor get() {
        return EXTRACTOR;
    }

    public final static Set<Class> BOOLEAN_TYPES = ImmutableSet.<Class>of(Boolean.class, Boolean.TYPE);
    public final static Set<Class> NUMBER_TYPES = ImmutableSet.<Class>of(Byte.class, Byte.TYPE,
            Short.class, Short.TYPE,
            Integer.class, Integer.TYPE,
            Long.class, Long.TYPE,
            Float.class, Float.TYPE,
            Double.class, Double.TYPE);
    public final static Set<Class> CHARACTER_TYPES = ImmutableSet.<Class>of(Character.class, Character.TYPE);

    public static boolean isCredentialValueConfig(Class type) {
        return CredentialValue.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    public List<ErrorMessage> validate(Field field, ConfigDef.Type type, String valueStr, Object contextMsg,
                                       boolean isTrigger) {
        List<ErrorMessage> errors = new ArrayList<>();
        if (!valueStr.isEmpty()) {
            switch (type) {
                case BOOLEAN:
                    if (!BOOLEAN_TYPES.contains(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_001, contextMsg, field.getType()));
                    }
                    break;
                case NUMBER:
                    if (!NUMBER_TYPES.contains(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_002, contextMsg));
                    }
                    try {
                        Class<?> wrapper;
                        if (Primitives.isWrapperType(field.getType())) {
                            wrapper = field.getType();
                        } else {
                            wrapper = Primitives.wrap(field.getType());
                        }
                        wrapper.getMethod("valueOf", String.class).invoke(null, valueStr);
                    } catch (Exception ex) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_013, contextMsg, valueStr));
                    }
                    break;
                case STRING:
                case MODEL:
                    if (!String.class.isAssignableFrom(field.getType()) && !field.getType().isEnum() &&
                            !List.class.isAssignableFrom(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_003, contextMsg, field.getType()));
                    }
                    if (field.getType().isEnum()) {
                        try {
                            Enum.valueOf(((Class<Enum>) field.getType()), valueStr);
                        } catch (IllegalArgumentException ex) {
                            errors.add(new ErrorMessage(DefinitionError.DEF_004, contextMsg, field.getType(), valueStr));
                        }
                    } else if(List.class.isAssignableFrom(field.getType())) {

                        try {
                            List list = ObjectMapperFactory.get().readValue(valueStr, List.class);
                            try {
                                // convert to enum if necessary to validate
                                convertElementsToEnum(field, list);
                            } catch (Exception ex) {
                                errors.add(new ErrorMessage(DefinitionError.DEF_012, contextMsg, getListType(field).getSimpleName(),
                                        ex.toString()));
                            }
                        } catch (Exception ex) {
                            errors.add(new ErrorMessage(DefinitionError.DEF_006, contextMsg, valueStr, ex.toString()));
                        }
                    }
                    break;
                case LIST:
                    if (!List.class.isAssignableFrom(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_005, contextMsg, field.getType()));
                    }
                    try {
                        List list = ObjectMapperFactory.get().readValue(valueStr, List.class);
                        try {
                            // convert to enum if necessary to validate
                            convertElementsToEnum(field, list);
                        } catch (Exception ex) {
                            errors.add(new ErrorMessage(DefinitionError.DEF_012, contextMsg, getListType(field).getSimpleName(),
                                    ex.toString()));
                        }
                    } catch (Exception ex) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_006, contextMsg, valueStr, ex.toString()));
                    }
                    break;
                case MAP:
                    if (!Map.class.isAssignableFrom(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_007, contextMsg, field.getType()));
                    }
                    try {
                        ObjectMapperFactory.get().readValue(valueStr, LinkedHashMap.class);
                    } catch (Exception ex) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_008, contextMsg, valueStr));
                    }
                    break;
                case CHARACTER:
                    if (!CHARACTER_TYPES.contains(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_009, contextMsg));
                    }
                    if (valueStr.length() > 1) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_010, contextMsg, valueStr));
                    }
                    break;
                case TEXT:
                    if (!String.class.isAssignableFrom(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_011, contextMsg, field.getType()));
                    }
                    break;
                case CREDENTIAL:
                    if (!CredentialValue.class.isAssignableFrom(field.getType())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_014, contextMsg, field.getType()));
                    }
                    break;
            }
        }
        return errors;
    }

    public List<ErrorMessage> validate(Field field, ConfigDef def, Object contextMsg) {
        return validate(field, def.type(), def.defaultValue(), contextMsg, false);
    }

    public Object extract(Field field, ConfigDef def, Object contextMsg) {
        return extract(field, def.type(), def.defaultValue(), contextMsg, false);
    }

    @SuppressWarnings("unchecked")
    public Object extract(Field field, ConfigDef.Type type, String valueStr, Object contextMsg, boolean isTrigger) {
        List<ErrorMessage> errors = validate(field, type, valueStr, contextMsg, isTrigger);
        if (errors.isEmpty()) {
            Object value = null;
            if (valueStr == null || valueStr.isEmpty()) {
                value = null;
            } else {
                try {
                    switch (type) {
                        case BOOLEAN:
                            value = Boolean.parseBoolean(valueStr);
                            break;
                        case NUMBER:
                            value = extractAsNumber(field, valueStr);
                            break;
                        case STRING:
                        case MODEL:
                            if(field.getType() == String.class) {
                                value = valueStr;
                            } else if(field.getType().isEnum()){
                                value = Enum.valueOf(((Class<Enum>)field.getType()), valueStr);
                            } else if(List.class.isAssignableFrom(field.getType())) {
                                value = ObjectMapperFactory.get().readValue(valueStr, List.class);
                                // convert to enum if necessary
                                value = convertElementsToEnum(field, (List) value);
                            }
                            break;
                        case LIST:
                            value = extractAsList(field, valueStr);
                            break;
                        case MAP:
                            Map<String, ?> map = ObjectMapperFactory.get().readValue(valueStr, LinkedHashMap.class);
                            List list = new ArrayList();
                            for (Map.Entry<String, ?> entry : map.entrySet()) {
                                list.add(ImmutableMap.of("key", entry.getKey(), "value", entry.getValue()));
                            }
                            value = list;
                            break;
                        case CHARACTER:
                            value = valueStr.charAt(0);
                            break;
                        case TEXT:
                            value = valueStr;
                            break;
                        case CREDENTIAL:
                            value = new ClearCredentialValue(valueStr);
                            break;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(Utils.format("Unexpected exception: {}", ex.toString()), ex);
                }
            }
            return value;
        } else {
            throw new IllegalArgumentException(Utils.format("Invalid configuration value: {}", errors));
        }
    }

    // RUNTIME supports only Numeric types and String at the moment
    private Object extractAsRuntime(Field field, String valueStr) {
        if (field.getType() == Byte.TYPE || field.getType() == Byte.class ||
                field.getType() == Short.TYPE || field.getType() == Short.class ||
                field.getType() == Integer.TYPE || field.getType() == Integer.class ||
                field.getType() == Long.TYPE || field.getType() == Long.class ||
                field.getType() == Float.TYPE || field.getType() == Float.class ||
                field.getType() == Double.TYPE || field.getType() == Double.class) {
            return extractAsNumber(field, valueStr);
        } else if (String.class.isAssignableFrom(field.getType())) {
            return valueStr;
        }

        throw new IllegalArgumentException(Utils.format("Invalid type for RUNTIME type: {}", field.getType()));
    }

    private Object extractAsList(Field field, String valueStr) throws IOException {
        Object value = ObjectMapperFactory.get().readValue(valueStr, List.class);
        // convert to enum if necessary
        value = convertElementsToEnum(field, (List) value);
        return value;
    }

    private Object extractAsNumber(Field field, String valueStr) {
        if (field.getType() == Byte.TYPE || field.getType() == Byte.class) {
            return Byte.parseByte(valueStr);
        } else if (field.getType() == Short.TYPE || field.getType() == Short.class) {
            return Short.parseShort(valueStr);
        } else if (field.getType() == Integer.TYPE || field.getType() == Integer.class) {
            return Integer.parseInt(valueStr);
        } else if (field.getType() == Long.TYPE || field.getType() == Long.class) {
            return Long.parseLong(valueStr);
        } else if (field.getType() == Float.TYPE || field.getType() == Float.class) {
            return Float.parseFloat(valueStr);
        } else if (field.getType() == Double.TYPE || field.getType() == Double.class) {
            return Double.parseDouble(valueStr);
        }

        throw new IllegalArgumentException(Utils.format("Invalid number type: ", field.getType()));
    }

    Class getListType(Field listField) {
        return (Class)((ParameterizedType)listField.getGenericType()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    List convertElementsToEnum(Field listField, List list) {
        Class elementClass = getListType(listField);
        if (elementClass.isEnum()) {
            for (int i = 0; i < list.size(); i++) {
                list.set(i, Enum.valueOf(elementClass, (String) list.get(i)));
            }
        }
        return list;
    }

}
