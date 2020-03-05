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
package org.apache.dolphinscheduler.service.queue;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * task queue factory
 */
public class TaskQueueFactory {

  private static final Logger logger = LoggerFactory.getLogger(TaskQueueFactory.class);


  private TaskQueueFactory(){

  }


  /**
   * get instance (singleton)
   *
   * @return instance
   */
  public static TaskUpdateQueue getTaskQueueInstance() {
    String queueImplValue = CommonUtils.getQueueImplValue();
    if (StringUtils.isNotBlank(queueImplValue)) {
        logger.info("task queue impl use zookeeper ");
        return SpringApplicationContext.getBean(TaskUpdateQueueImpl.class);
    }else{
      logger.error("property dolphinscheduler.queue.impl can't be blank, system will exit ");
      System.exit(-1);
    }

    return null;
  }
}
