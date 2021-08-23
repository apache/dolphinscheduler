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

package org.apache.dolphinscheduler.common.config.validators.impl.worker;

import org.apache.dolphinscheduler.common.config.validators.PropertyValidator;
import org.apache.dolphinscheduler.common.config.validators.ValidationConstants;
import org.apache.dolphinscheduler.common.exception.ValidationException;

/**
 * Worker Listen port validator.
 */
public class ListenPortValidator implements PropertyValidator<Integer> {

  public String getPropertyKey() {
    return ValidationConstants.WORKER_LISTEN_PORT_KEY;
  }

  public Integer getPropertyValue(String value) {
    return Integer.valueOf(value);
  }

  public void validate(String value) {
    int port = Integer.valueOf(value);
    if ((1 <= port) && (port <= 65535)) {
      // do nothing
    } else {
      throw new ValidationException("Port value : " + value + " expected to in the range 1-65535.");
    }
  }

}
