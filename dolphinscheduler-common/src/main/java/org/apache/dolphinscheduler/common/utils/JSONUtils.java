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
package org.apache.dolphinscheduler.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * json utils
 */
public class JSONUtils {

  private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

  /**
   * can use static singleton, inject: just make sure to reuse!
   */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private JSONUtils() {
    //Feature that determines whether encountering of unknown properties, false means not analyzer unknown properties
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setTimeZone(TimeZone.getDefault());
  }

  /**
   * json representation of object
   * @param object object
   * @return object to json string
   */
  public static String toJson(Object object) {
    try{
      return JSON.toJSONString(object,false);
    } catch (Exception e) {
      logger.error("object to json exception!",e);
    }

    return null;
  }


  /**
   *
   * This method deserializes the specified Json into an object of the specified class. It is not
   * suitable to use if the specified class is a generic type since it will not have the generic
   * type information because of the Type Erasure feature of Java. Therefore, this method should not
   * be used if the desired type is a generic type. Note that this method works fine if the any of
   * the fields of the specified object are generics, just the object itself should not be a
   * generic type.
   *
   * @param json the string from which the object is to be deserialized
   * @param clazz the class of T
   * @param <T> T
   * @return an object of type T from the string
   * classOfT
   */
  public static <T> T parseObject(String json, Class<T> clazz) {
    if (StringUtils.isEmpty(json)) {
      return null;
    }

    try {
      return JSON.parseObject(json, clazz);
    } catch (Exception e) {
      logger.error("parse object exception!",e);
    }
    return null;
  }


  /**
   * json to list
   *
   * @param json json string
   * @param clazz class
   * @param <T> T
   * @return list
   */
  public static <T> List<T> toList(String json, Class<T> clazz) {
    if (StringUtils.isEmpty(json)) {
      return new ArrayList<>();
    }
    try {
      return JSONArray.parseArray(json, clazz);
    } catch (Exception e) {
      logger.error("JSONArray.parseArray exception!",e);
    }

    return new ArrayList<>();
  }



  /**
   * check json object valid
   *
   * @param json json
   * @return true if valid
   */
  public static boolean checkJsonValid(String json) {

    if (StringUtils.isEmpty(json)) {
      return false;
    }

    try {
      objectMapper.readTree(json);
      return true;
    } catch (IOException e) {
      logger.error("check json object valid exception!",e);
    }

    return false;
  }


  /**
   * Method for finding a JSON Object field with specified name in this
   * node or its child nodes, and returning value it has.
   * If no matching field is found in this node or its descendants, returns null.
   *
   * @param jsonNode json node
   * @param fieldName Name of field to look for
   *
   * @return Value of first matching node found, if any; null if none
   */
  public static String findValue(JsonNode jsonNode, String fieldName) {
    JsonNode node = jsonNode.findValue(fieldName);

    if (node == null) {
      return null;
    }

    return node.toString();
  }


  /**
   * json to map
   *
   * {@link #toMap(String, Class, Class)}
   *
   * @param json json
   * @return json to map
   */
  public static Map<String, String> toMap(String json) {
    if (StringUtils.isEmpty(json)) {
      return null;
    }

    try {
      return JSON.parseObject(json, new TypeReference<HashMap<String, String>>(){});
    } catch (Exception e) {
      logger.error("json to map exception!",e);
    }

    return null;
  }

  /**
   *
   * json to map
   *
   * @param json json
   * @param classK classK
   * @param classV classV
   * @param <K> K
   * @param <V> V
   * @return to map
   */
  public static <K, V> Map<K, V> toMap(String json, Class<K> classK, Class<V> classV) {
    if (StringUtils.isEmpty(json)) {
      return null;
    }

    try {
      return JSON.parseObject(json, new TypeReference<HashMap<K, V>>() {});
    } catch (Exception e) {
      logger.error("json to map exception!",e);
    }

    return null;
  }

  /**
   * object to json string
   * @param object object
   * @return json string
   */
  public static String toJsonString(Object object) {
    try{
      return JSON.toJSONString(object,false);
    } catch (Exception e) {
      throw new RuntimeException("Object json deserialization exception.", e);
    }
  }

  public static JSONObject parseObject(String text) {
    try{
      return JSON.parseObject(text);
    } catch (Exception e) {
      throw new RuntimeException("String json deserialization exception.", e);
    }
  }

  public static JSONArray parseArray(String text) {
    try{
      return JSON.parseArray(text);
    } catch (Exception e) {
      throw new RuntimeException("Json deserialization exception.", e);
    }
  }



  /**
   * json serializer
   */
  public static class JsonDataSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeRawValue(value);
    }

  }

  /**
   * json data deserializer
   */
  public static class JsonDataDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      return node.toString();
    }

  }
}
