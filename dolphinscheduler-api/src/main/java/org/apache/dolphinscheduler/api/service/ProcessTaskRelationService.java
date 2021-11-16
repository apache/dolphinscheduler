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

import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * process task relation service
 */
public interface ProcessTaskRelationService {

    /**
     * create process task relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return create result code
     */
    Map<String, Object> createProcessTaskRelation(User loginUser,
                                                  long projectCode,
                                                  long processDefinitionCode,
                                                  long preTaskCode,
                                                  long postTaskCode);

    /**
     * move task to other processDefinition
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param targetProcessDefinitionCode target process definition code
     * @param taskCode the current task code (the post task code)
     * @return move result code
     */
    Map<String, Object> moveTaskProcessRelation(User loginUser,
                                                long projectCode,
                                                long processDefinitionCode,
                                                long targetProcessDefinitionCode,
                                                long taskCode);

    /**
     * delete process task relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param taskCode the post task code
     * @return delete result code
     */
    Map<String, Object> deleteTaskProcessRelation(User loginUser,
                                                  long projectCode,
                                                  long processDefinitionCode,
                                                  long taskCode);

    /**
     * delete task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param preTaskCodes the pre task codes, sep ','
     * @param taskCode the post task code
     * @return delete result code
     */
    Map<String, Object> deleteUpstreamRelation(User loginUser,
                                               long projectCode,
                                               String preTaskCodes,
                                               long taskCode);

    /**
     * delete task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param postTaskCodes the post task codes, sep ','
     * @param taskCode the pre task code
     * @return delete result code
     */
    Map<String, Object> deleteDownstreamRelation(User loginUser,
                                                 long projectCode,
                                                 String postTaskCodes,
                                                 long taskCode);

    /**
     * query task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode current task code (post task code)
     * @return process task relation list
     */
    Map<String, Object> queryUpstreamRelation(User loginUser,
                                              long projectCode,
                                              long taskCode);

    /**
     * query task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode pre task code
     * @return process task relation list
     */
    Map<String, Object> queryDownstreamRelation(User loginUser,
                                                long projectCode,
                                                long taskCode);
}
