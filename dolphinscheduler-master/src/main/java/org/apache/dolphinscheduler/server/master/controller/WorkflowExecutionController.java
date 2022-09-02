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

package org.apache.dolphinscheduler.server.master.controller;

import org.apache.dolphinscheduler.remote.dto.WorkflowInstanceExecuteDetailDto;
import org.apache.dolphinscheduler.remote.dto.MasterWorkflowInstanceExecutingListingDto;
import org.apache.dolphinscheduler.server.master.service.ExecutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workflow")
public class WorkflowExecutionController {

    @Autowired
    private ExecutingService executingService;

    /**
     * query workflow execute data in memory
     *
     * @param processInstanceId
     * @return
     */
    @GetMapping("/detail/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowInstanceExecuteDetailDto queryExecuteData(@PathVariable("id") int processInstanceId) {
        Optional<WorkflowInstanceExecuteDetailDto> workflowExecuteDtoOptional = executingService.queryWorkflowExecutingData(processInstanceId);
        return workflowExecuteDtoOptional.orElse(null);
    }

    @GetMapping("/listingExecutingWorkflows")
    @ResponseStatus(HttpStatus.OK)
    public List<MasterWorkflowInstanceExecutingListingDto> listingExecuteData() {
        return executingService.listingExecutingWorkflows();
    }
}
