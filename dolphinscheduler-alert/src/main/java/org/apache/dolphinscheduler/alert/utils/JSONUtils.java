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
package org.apache.dolphinscheduler.alert.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * json utils
 */
public class JSONUtils {

  private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

  /**
   * object to json string
   * @param object the object to be converted to json
   * @return json string
   */
  public static String toJsonString(Object object) {
    try{
      return JSON.toJSONString(object,false);
    } catch (Exception e) {
      throw new RuntimeException("Json deserialization exception.", e);
    }
  }

  /**
   * json to list
   *
   * @param json the json
   * @param clazz c
   * @param <T> the generic clazz
   * @return the result list or empty list
   */
  public static <T> List<T> toList(String json, Class<T> clazz) {
    if (StringUtils.isEmpty(json)) {
      return Collections.emptyList();
    }
    try {
      return JSON.parseArray(json, clazz);
    } catch (Exception e) {
      logger.error("JSONArray.parseArray exception!",e);
    }

    return Collections.emptyList();
  }

}
