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


import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.QueueService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
 * queue controller
 */
@Api(tags = "QUEUE_TAG", position = 1)
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
    @ApiOperation(value = "queryList", notes= "QUERY_QUEUE_LIST_NOTES")
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        try{
            logger.info("login user {}, query queue list", loginUser.getUserName());
            Map<String, Object> result = queueService.queryList(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_QUEUE_LIST_ERROR.getMsg(),e);
            return error(QUERY_QUEUE_LIST_ERROR.getCode(), QUERY_QUEUE_LIST_ERROR.getMsg());
        }
    }

    /**
     * query queue list paging
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "queryQueueListPaging", notes= "QUERY_QUEUE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryQueueListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("pageNo") Integer pageNo,
                                  @RequestParam(value = "searchVal", required = false) String searchVal,
                                  @RequestParam("pageSize") Integer pageSize){
        try{
            logger.info("login user {}, query queue list,search value:{}", loginUser.getUserName(),searchVal);
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }

            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = queueService.queryList(loginUser,searchVal,pageNo,pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_QUEUE_LIST_ERROR.getMsg(),e);
            return error(QUERY_QUEUE_LIST_ERROR.getCode(), QUERY_QUEUE_LIST_ERROR.getMsg());
        }
    }

    /**
     * create queue
     *
     * @param loginUser
     * @param queue
     * @param queueName
     * @return
     */
    @ApiOperation(value = "createQueue", notes= "CREATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true,dataType ="String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME",required = true, dataType ="String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "queue") String queue,
                               @RequestParam(value = "queueName") String queueName) {
        logger.info("login user {}, create queue, queue: {}, queueName: {}",
                loginUser.getUserName(), queue, queueName);
        try {
            Map<String, Object> result = queueService.createQueue(loginUser,queue,queueName);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(CREATE_QUEUE_ERROR.getMsg(),e);
            return error(CREATE_QUEUE_ERROR.getCode(), CREATE_QUEUE_ERROR.getMsg());
        }
    }

    /**
     * update queue
     *
     * @param loginUser
     * @param queue
     * @param queueName
     * @return
     */
    @ApiOperation(value = "updateQueue", notes= "UPDATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "QUEUE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME",required = true, dataType ="String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME",required = true, dataType ="String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.CREATED)
    public Result updateQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "id") int id,
                              @RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        logger.info("login user {}, update queue, id: {}, queue: {}, queueName: {}",
                loginUser.getUserName(), id,queue, queueName);
        try {
            Map<String, Object> result = queueService.updateQueue(loginUser,id,queue,queueName);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(UPDATE_QUEUE_ERROR.getMsg(),e);
            return error(UPDATE_QUEUE_ERROR.getCode(), UPDATE_QUEUE_ERROR.getMsg());
        }
    }

    /**
     * verify queue and queue name
     *
     * @param loginUser
     * @param queue
     * @param queueName
     * @return
     */
    @ApiOperation(value = "verifyQueue", notes= "VERIFY_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "QUEUE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME",required = true, dataType ="String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME",required = true, dataType ="String")
    })
    @PostMapping(value = "/verify-queue")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value ="queue") String queue,
                                   @RequestParam(value ="queueName") String queueName
    ) {

        try{
            logger.info("login user {}, verfiy queue: {} queue name: {}",
                    loginUser.getUserName(),queue,queueName);
            return queueService.verifyQueue(queue,queueName);
        }catch (Exception e){
            logger.error(VERIFY_QUEUE_ERROR.getMsg(),e);
            return error(Status.VERIFY_QUEUE_ERROR.getCode(), Status.VERIFY_QUEUE_ERROR.getMsg());
        }
    }


}
