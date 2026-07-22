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

package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorkflowUtils {

    public static List<Long> parseStartNodeList(String startNodes) {
        try {
            if (StringUtils.isEmpty(startNodes)) {
                return new ArrayList<>();
            }
            return Arrays.stream(startNodes.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new ServiceException("Parse startNodes: " + startNodes + " error", ex);
        }
    }

    public static WorkflowBackFillRequest.BackfillTime parseBackfillTime(String backfillTimeJson) {
        try {
            if (StringUtils.isEmpty(backfillTimeJson)) {
                throw new IllegalArgumentException("backfillTime is empty");
            }
            WorkflowBackFillRequest.BackfillTime backfillTime =
                    JSONUtils.parseObject(backfillTimeJson, WorkflowBackFillRequest.BackfillTime.class);
            if (backfillTime == null) {
                throw new IllegalArgumentException("backfillTime is invalid");
            }
            return backfillTime;
        } catch (Exception ex) {
            throw new ServiceException("Parse backfillTime: " + backfillTimeJson + " error", ex);
        }
    }

}
