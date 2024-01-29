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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_QUEUE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_QUEUE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_QUEUE_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * queue controller
 */
@Tag(name = "QUEUE_TAG")
@RestController
@RequestMapping("/queues")
public class QueueController extends BaseController {

    @Autowired
    private QueueService queueService;

    /**
     * query queue list
     *
     * @param loginUser login user
     * @return queue list
     */
    @Operation(summary = "queryList", description = "QUERY_QUEUE_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    public Result<List<Queue>> queryList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<Queue> queues = queueService.queryList(loginUser);
        return Result.success(queues);
    }

    /**
     * query queue list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return queue list
     */
    @Operation(summary = "queryQueueListPaging", description = "QUERY_QUEUE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    public Result<PageInfo<Queue>> queryQueueListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam("pageNo") Integer pageNo,
                                                        @RequestParam(value = "searchVal", required = false) String searchVal,
                                                        @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);

        searchVal = ParameterUtils.handleEscapes(searchVal);
        PageInfo<Queue> queuePageInfo = queueService.queryList(loginUser, searchVal, pageNo, pageSize);
        return Result.success(queuePageInfo);
    }

    /**
     * create queue
     *
     * @param loginUser login user
     * @param queue     queue
     * @param queueName queue name
     * @return create result
     */
    @Operation(summary = "createQueue", description = "CREATE_QUEUE_NOTES")
    @Parameters({
            @Parameter(name = "queue", description = "YARN_QUEUE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "queueName", description = "QUEUE_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_QUEUE_ERROR)
    public Result<Queue> createQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "queue") String queue,
                                     @RequestParam(value = "queueName") String queueName) {
        return Result.success(queueService.createQueue(loginUser, queue, queueName));
    }

    /**
     * update queue
     *
     * @param loginUser login user
     * @param queue     queue
     * @param id        queue id
     * @param queueName queue name
     * @return update result code
     */
    @Operation(summary = "updateQueue", description = "UPDATE_QUEUE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "QUEUE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "queue", description = "YARN_QUEUE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "queueName", description = "QUEUE_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_QUEUE_ERROR)
    public Result<Queue> updateQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable(value = "id") int id,
                                     @RequestParam(value = "queue") String queue,
                                     @RequestParam(value = "queueName") String queueName) {
        return Result.success(queueService.updateQueue(loginUser, id, queue, queueName));
    }

    /**
     *  delete queue by id
     *
     * @param loginUser login user
     * @param id        queue id
     * @return update result code
     */
    @Operation(summary = "deleteQueueById", description = "DELETE_QUEUE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "QUEUE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_QUEUE_BY_ID_ERROR)
    public Result<Boolean> deleteQueueById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") int id) throws Exception {
        queueService.deleteQueueById(loginUser, id);
        return Result.success(true);
    }

    /**
     * verify queue and queue name
     *
     * @param loginUser login user
     * @param queue     queue
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    @Operation(summary = "verifyQueue", description = "VERIFY_QUEUE_NOTES")
    @Parameters({
            @Parameter(name = "queue", description = "YARN_QUEUE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "queueName", description = "QUEUE_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/verify")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_QUEUE_ERROR)
    public Result<Boolean> verifyQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "queue") String queue,
                                       @RequestParam(value = "queueName") String queueName) {
        queueService.verifyQueue(queue, queueName);
        return Result.success(true);
    }
}
