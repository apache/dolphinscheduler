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


import cn.escheduler.api.service.MonitorService;
import cn.escheduler.api.service.ServerService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;


/**
 * monitor controller
 */
@Api(tags = "MONITOR_TAG", position = 1)
@RestController
@RequestMapping("/monitor")
public class MonitorController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private ServerService serverService;

    @Autowired
    private MonitorService monitorService;

    /**
     * master list
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "listMaster", notes= "MASTER_LIST_NOTES")
    @GetMapping(value = "/master/list")
    @ResponseStatus(HttpStatus.OK)
    public Result listMaster(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user: {}, query all master", loginUser.getUserName());
        try{
            logger.info("list master, user:{}", loginUser.getUserName());
            Map<String, Object> result = monitorService.queryMaster(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(LIST_MASTERS_ERROR.getMsg(),e);
            return error(LIST_MASTERS_ERROR.getCode(),
                    LIST_MASTERS_ERROR.getMsg());
        }
    }

    /**
     * worker list
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "listWorker", notes= "WORKER_LIST_NOTES")
    @GetMapping(value = "/worker/list")
    @ResponseStatus(HttpStatus.OK)
    public Result listWorker(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user: {}, query all workers", loginUser.getUserName());
        try{
            Map<String, Object> result = monitorService.queryWorker(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(LIST_WORKERS_ERROR.getMsg(),e);
            return error(LIST_WORKERS_ERROR.getCode(),
                    LIST_WORKERS_ERROR.getMsg());
        }
    }

    /**
     * query database state
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "queryDatabaseState", notes= "QUERY_DATABASE_STATE_NOTES")
    @GetMapping(value = "/database")
    @ResponseStatus(HttpStatus.OK)
    public Result queryDatabaseState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user: {}, query database state", loginUser.getUserName());
        try{

            Map<String, Object> result = monitorService.queryDatabaseState(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_DATABASE_STATE_ERROR.getMsg(),e);
            return error(QUERY_DATABASE_STATE_ERROR.getCode(),
                    QUERY_DATABASE_STATE_ERROR.getMsg());
        }
    }

    /**
     * query zookeeper state
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "queryZookeeperState", notes= "QUERY_ZOOKEEPER_STATE_NOTES")
    @GetMapping(value = "/zookeeper/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryZookeeperState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user: {}, query zookeeper state", loginUser.getUserName());
        try{
            Map<String, Object> result = monitorService.queryZookeeperState(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_ZOOKEEPER_STATE_ERROR.getMsg(),e);
            return error(QUERY_ZOOKEEPER_STATE_ERROR.getCode(),
                    QUERY_ZOOKEEPER_STATE_ERROR.getMsg());
        }
    }

}
