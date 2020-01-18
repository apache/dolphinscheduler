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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.log.LogClient;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * log service
 */
@Service
public class LoggerService {

  private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);

  @Autowired
  private ProcessDao processDao;

  /**
   * view log
   *
   * @param taskInstId task instance id
   * @param skipLineNum skip line number
   * @param limit limit
   * @return log string data
   */
  public Result queryLog(int taskInstId, int skipLineNum, int limit) {

    TaskInstance taskInstance = processDao.findTaskInstanceById(taskInstId);

    if (taskInstance == null){
      return new Result(Status.TASK_INSTANCE_NOT_FOUND.getCode(), Status.TASK_INSTANCE_NOT_FOUND.getMsg());
    }

    String host = taskInstance.getHost();
    if(StringUtils.isEmpty(host)){
      return new Result(Status.TASK_INSTANCE_NOT_FOUND.getCode(), Status.TASK_INSTANCE_NOT_FOUND.getMsg());
    }


    Result result = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());

    logger.info("log host : {} , logPath : {} , logServer port : {}",host,taskInstance.getLogPath(),Constants.LOGGER_SERVER_RPC_PORT);

    LogClient logClient = new LogClient(host, Integer.parseInt(Constants.LOGGER_SERVER_RPC_PORT));
    String log = logClient.rollViewLog(taskInstance.getLogPath(),skipLineNum,limit);
    result.setData(log);
    logger.info(log);

    return result;
  }

  /**
   * get log size
   *
   * @param taskInstId task instance id
   * @return log byte array
   */
  public byte[] getLogBytes(int taskInstId) {
    TaskInstance taskInstance = processDao.findTaskInstanceById(taskInstId);
    if (taskInstance == null){
      throw new RuntimeException("task instance is null");
    }
    String host = taskInstance.getHost();
    LogClient logClient = new LogClient(host, Integer.parseInt(Constants.LOGGER_SERVER_RPC_PORT));
    return logClient.getLogBytes(taskInstance.getLogPath());
  }
}
