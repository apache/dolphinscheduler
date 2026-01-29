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

package org.apache.dolphinscheduler.scheduler.quartz.exception;

import org.apache.dolphinscheduler.scheduler.api.SchedulerExceptionEnum;

public enum QuartzSchedulerExceptionEnum implements SchedulerExceptionEnum {

    QUARTZ_SCHEDULER_START_ERROR("QUARTZ-001", "Quartz Scheduler start error"),
    QUARTZ_UPSERT_JOB_ERROR("QUARTZ-002", "Upsert quartz job error"),
    QUARTZ_DELETE_JOB_ERROR("QUARTZ-003", "Delete quartz job error"),
    QUARTZ_SCHEDULER_SHOWDOWN_ERROR("QUARTZ-004", "Quartz Scheduler shutdown error"),
    ;

    private final String code;

    private final String message;

    QuartzSchedulerExceptionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
