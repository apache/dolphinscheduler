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

package org.apache.dolphinscheduler.api.controller.v2;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_QUEUE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_QUEUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.dto.queue.QueueCreateRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueCreateResponse;
import org.apache.dolphinscheduler.api.dto.queue.QueueListPagingResponse;
import org.apache.dolphinscheduler.api.dto.queue.QueueListResponse;
import org.apache.dolphinscheduler.api.dto.queue.QueueQueryRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueUpdateRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueUpdateResponse;
import org.apache.dolphinscheduler.api.dto.queue.QueueVerifyRequest;
import org.apache.dolphinscheduler.api.dto.queue.QueueVerifyResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/v2/queues")
public class QueueV2Controller extends BaseController {

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
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueueListResponse queryList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Result result = queueService.queryList(loginUser);
        return new QueueListResponse(result);
    }

    /**
     * query queue list paging
     *
     * @param loginUser         login user
     * @param queueQueryRequest queueQueryRequest
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
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueueListPagingResponse queryQueueListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        QueueQueryRequest queueQueryRequest) {
        Result result = checkPageParams(queueQueryRequest.getPageNo(), queueQueryRequest.getPageSize());
        if (!result.checkResult()) {
            return new QueueListPagingResponse(result);
        }

        String searchVal = ParameterUtils.handleEscapes(queueQueryRequest.getSearchVal());
        result = queueService.queryList(loginUser, searchVal, queueQueryRequest.getPageNo(),
                queueQueryRequest.getPageSize());
        return new QueueListPagingResponse(result);
    }

    /**
     * create queue
     *
     * @param loginUser          login user
     * @param queueCreateRequest queueCreateRequest
     * @return create result
     */
    @Operation(summary = "createQueue", description = "CREATE_QUEUE_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueueCreateResponse createQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestBody QueueCreateRequest queueCreateRequest) {
        Result result =
                queueService.createQueue(loginUser, queueCreateRequest.getQueue(), queueCreateRequest.getQueueName());
        return new QueueCreateResponse(result);
    }

    /**
     * update queue
     *
     * @param loginUser          login user
     * @param id                 queue id
     * @param queueUpdateRequest queueUpdateRequest
     * @return update result code
     */
    @Operation(summary = "updateQueue", description = "UPDATE_QUEUE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "QUEUE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueueUpdateResponse updateQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") int id,
                                           @RequestBody QueueUpdateRequest queueUpdateRequest) {
        Result result = queueService.updateQueue(loginUser, id, queueUpdateRequest.getQueue(),
                queueUpdateRequest.getQueueName());
        return new QueueUpdateResponse(result);
    }

    /**
     * verify queue and queue name
     *
     * @param loginUser          login user
     * @param queueVerifyRequest queueVerifyRequest
     * @return true if the queue name not exists, otherwise return false
     */
    @Operation(summary = "verifyQueue", description = "VERIFY_QUEUE_NOTES")
    @PostMapping(value = "/verify", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueueVerifyResponse verifyQueue(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestBody QueueVerifyRequest queueVerifyRequest) {
        Result result = queueService.verifyQueue(queueVerifyRequest.getQueue(), queueVerifyRequest.getQueueName());
        return new QueueVerifyResponse(result);
    }
}
