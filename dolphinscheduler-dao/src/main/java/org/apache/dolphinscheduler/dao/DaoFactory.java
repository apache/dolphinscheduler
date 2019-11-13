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
package org.apache.dolphinscheduler.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dao factory
 */
public class DaoFactory {

  private static final Logger logger = LoggerFactory.getLogger(DaoFactory.class);

  private static Map<String, AbstractBaseDao> daoMap = new ConcurrentHashMap<>();

  private DaoFactory(){

  }

  /**
   * get dao instance
   * @param clazz clazz
   * @param <T> T
   * @return T object
   */
  @SuppressWarnings("unchecked")
  public static <T extends AbstractBaseDao> T getDaoInstance(Class<T> clazz) {
    String className = clazz.getName();
    synchronized (daoMap) {
      if (!daoMap.containsKey(className)) {
        try {
          T t = clazz.getConstructor().newInstance();
          // init
          t.init();
          daoMap.put(className, t);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
    }

    return (T) daoMap.get(className);
  }
}
