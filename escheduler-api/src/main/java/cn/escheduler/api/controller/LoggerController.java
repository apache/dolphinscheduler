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
package cn.escheduler.api.controller;


import cn.escheduler.api.service.LoggerService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static cn.escheduler.api.enums.Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR;
import static cn.escheduler.api.enums.Status.QUERY_TASK_INSTANCE_LOG_ERROR;


/**
 * log controller
 */
@RestController
@RequestMapping("/log")
public class LoggerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoggerController.class);


    @Autowired
    private LoggerService loggerService;

    /**
     * query task log
     */
    @GetMapping(value = "/detail")
    @ResponseStatus(HttpStatus.OK)
    public Result queryLog(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @RequestParam(value = "taskInstId") int taskInstanceId,
                           @RequestParam(value = "skipLineNum") int skipNum,
                           @RequestParam(value = "limit") int limit) {
        try {

            logger.info(
                    "login user {}, view {} task instance log ,skipLineNum {} , limit {}", loginUser.getUserName(), taskInstanceId, skipNum, limit);
            return loggerService.queryLog(taskInstanceId, skipNum, limit);
        } catch (Exception e) {
            logger.error(QUERY_TASK_INSTANCE_LOG_ERROR.getMsg(), e);
            return error(QUERY_TASK_INSTANCE_LOG_ERROR.getCode(), QUERY_TASK_INSTANCE_LOG_ERROR.getMsg());
        }
    }


    /**
     * download log file
     *
     * @param loginUser
     * @param taskInstanceId
     */
    @GetMapping(value = "/download-log")
    @ResponseBody
    public ResponseEntity downloadTaskLog(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "taskInstId") int taskInstanceId) {
        try {
            byte[] logBytes = loggerService.getLogBytes(taskInstanceId);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + System.currentTimeMillis() + ".queryLog" + "\"")
                    .body(logBytes);
        } catch (Exception e) {
            logger.error(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR.getMsg(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR.getMsg());
        }
    }

}
