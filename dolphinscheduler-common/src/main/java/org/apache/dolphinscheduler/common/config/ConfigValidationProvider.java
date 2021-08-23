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
import org.apache.dolphinscheduler.common.config.validators.impl.worker.ExecThreadsValidator;
import org.apache.dolphinscheduler.common.config.validators.impl.worker.ListenPortValidator;

import java.util.HashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.common.config.validators.ValidationConstants.WORKER_EXEC_THREADS_KEY;
import static org.apache.dolphinscheduler.common.config.validators.ValidationConstants.WORKER_LISTEN_PORT_KEY;

/**
 * Configuration Validation provider class.
 */
public class ConfigValidationProvider {

  private static ConfigValidationProvider instance;
  private static Map<String, PropertyValidator> validatorMapper = new HashMap<>();

  static {
    validatorMapper.put(WORKER_LISTEN_PORT_KEY, new ListenPortValidator());
    validatorMapper.put(WORKER_EXEC_THREADS_KEY, new ExecThreadsValidator());
  }

  private ConfigValidationProvider() {
  }

  public static synchronized ConfigValidationProvider getInstance() {
    if (instance == null) {
      instance = new ConfigValidationProvider();
    }
    return instance;
  }

  public PropertyValidator getPropertyValidator(String key) {
    return validatorMapper.get(key);
  }

}
