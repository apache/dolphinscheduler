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

package org.apache.dolphinscheduler.server.config;

import org.apache.dolphinscheduler.common.config.ConfigProvider;
import org.apache.dolphinscheduler.common.config.validators.ValidationConstants;
import org.apache.dolphinscheduler.common.exception.ValidationException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConfigProviderTests {

  @Test
  public void testWorkerListenPortRetrieval() {
    System.setProperty(ValidationConstants.WORKER_LISTEN_PORT_KEY, "256");
    ConfigProvider configProvider = ConfigProvider.getInstance();
    Integer workerListenPort = configProvider.getProperty(ValidationConstants.WORKER_LISTEN_PORT_KEY, Integer.class);
    assertTrue(workerListenPort == 256);
  }

  @Test
  public void testWorkerListenPortRetrievalInvalid() {
    System.setProperty(ValidationConstants.WORKER_LISTEN_PORT_KEY, "256666");
    ConfigProvider configProvider = ConfigProvider.getInstance();
    Integer workerListenPort;
    try {
      workerListenPort = configProvider.getProperty(ValidationConstants.WORKER_LISTEN_PORT_KEY, Integer.class);
      assertTrue(false);
    } catch (Throwable e) {
      assertTrue(e instanceof ValidationException);
    }
  }

  @Test
  public void testExecThreadRetrieval() {
    System.setProperty(ValidationConstants.WORKER_EXEC_THREADS_KEY, "50");
    ConfigProvider configProvider = ConfigProvider.getInstance();
    Integer execThreadsCount = configProvider.getProperty(ValidationConstants.WORKER_EXEC_THREADS_KEY, Integer.class);
    assertTrue(execThreadsCount == 50);
  }

  @Test
  public void testExecThreadRetrievalInvalid() {
    System.setProperty(ValidationConstants.WORKER_EXEC_THREADS_KEY, "256666");
    ConfigProvider configProvider = ConfigProvider.getInstance();
    Integer execThreadsCount;
    try {
      execThreadsCount = configProvider.getProperty(ValidationConstants.WORKER_EXEC_THREADS_KEY, Integer.class);
      assertTrue(false);
    } catch (Throwable e) {
      assertTrue(e instanceof ValidationException);
    }
  }
}
