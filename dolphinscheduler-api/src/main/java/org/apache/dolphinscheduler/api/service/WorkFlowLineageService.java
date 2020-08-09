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
package org.apache.dolphinscheduler.api.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * work flow lineage service
 */
@Service
public interface WorkFlowLineageService {

    /**
     * query work flow lineage by work flow name
     *
     * @param workFlowName work flow name
     * @param projectId project id
     * @return work flow lineage info
     */
    Map<String, Object> queryWorkFlowLineageByName(String workFlowName, int projectId);

    /**
     * query work flow lineage by work flow ids
     *
     * @param ids ids
     * @param projectId project id
     * @return work flow lineage info
     */
    Map<String, Object> queryWorkFlowLineageByIds(Set<Integer> ids, int projectId);
}
