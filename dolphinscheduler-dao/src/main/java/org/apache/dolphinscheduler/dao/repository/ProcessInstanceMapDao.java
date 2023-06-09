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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;

import java.util.List;

/**
 * Process Instance Map DAO
 */
public interface ProcessInstanceMapDao extends IDao<ProcessInstanceMap> {

    /**
     * find work process map by parent process id and parent task id.
     * @param parentWorkProcessId parentWorkProcessId
     * @param parentTaskId        parentTaskId
     * @return process instance map
     */
    ProcessInstanceMap queryWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId);

    List<Integer> querySubWorkflowInstanceIds(int workflowInstanceId);

    void deleteByParentId(int workflowInstanceId);
}
