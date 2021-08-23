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

package org.apache.dolphinscheduler.common.config;

import org.apache.dolphinscheduler.common.config.validators.PropertyValidator;
import org.apache.dolphinscheduler.common.config.validators.ValidationConstants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.util.Objects;

/**
 * Configuration provider class that can fetch property from JVM args or config files.
 */
public class ConfigProvider {

  private static ConfigProvider instance;
  private static ConfigValidationProvider configValidationProvider;

  static {
    PropertyUtils.loadPropertyFile(ValidationConstants.MASTER_PROPERTIES_PATH);
    PropertyUtils.loadPropertyFile(ValidationConstants.WORKER_PROPERTIES_PATH);
  }

  private ConfigProvider() {
    configValidationProvider = ConfigValidationProvider.getInstance();
  }

  public static synchronized ConfigProvider getInstance() {
    if (instance == null) {
      instance = new ConfigProvider();
    }
    return instance;
  }


  public <T> T getProperty(String key, Class<T> type) {
    String value = System.getProperty(key);
    PropertyValidator validator = configValidationProvider.getPropertyValidator(key);
    if (Objects.nonNull(value)) {
      validator.validate(value);
      return type.cast(validator.getPropertyValue(value));
    } else {
      return type.cast(validator.getPropertyValue(PropertyUtils.getString(key)));
    }
  }

}
