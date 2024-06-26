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

package org.apache.dolphinscheduler.api.exceptions;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

/**
 * Exception Handler
 */
@RestControllerAdvice
@ResponseBody
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Result<Object> exceptionHandler(ServiceException e, HandlerMethod hm) {
        log.error("{} Meet a ServiceException: {}", hm.getShortLogMessage(), e.getMessage());
        return new Result<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public Result<Object> exceptionHandler(Throwable e, HandlerMethod hm) {
        ApiException ce = hm.getMethodAnnotation(ApiException.class);
        log.error("Meet an unknown exception: ", e);
        if (ce == null) {
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
        Status st = ce.value();
        return Result.error(st);
    }

}
