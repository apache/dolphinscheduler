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


import cn.escheduler.api.service.QueueService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.QUERY_QUEUE_LIST_ERROR;


/**
 * queue controller
 */
@RestController
@RequestMapping("/queue")
public class QueueController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(QueueController.class);

    @Autowired
    private QueueService queueService;


    /**
     * query queue list
     * @param loginUser
     * @return
     */
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryList(@RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        try{
            logger.info("login user {}, query queue list", loginUser.getUserName());
            Map<String, Object> result = queueService.queryList(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_QUEUE_LIST_ERROR.getMsg(),e);
            return error(QUERY_QUEUE_LIST_ERROR.getCode(), QUERY_QUEUE_LIST_ERROR.getMsg());
        }
    }


}
