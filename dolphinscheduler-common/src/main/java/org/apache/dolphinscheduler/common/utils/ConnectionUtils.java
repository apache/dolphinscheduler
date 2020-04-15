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

import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUtils {

  public static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

  private ConnectionUtils() {
    throw new IllegalStateException("ConnectionUtils class");
  }

  /**
   * release resource
   * @param resources resources
   */
  public static void releaseResource(AutoCloseable... resources) {

    if (resources == null || resources.length == 0) {
      return;
    }
    Arrays.stream(resources).filter(Objects::nonNull)
        .forEach(resource -> {
          try {
            resource.close();
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        });
  }
}
