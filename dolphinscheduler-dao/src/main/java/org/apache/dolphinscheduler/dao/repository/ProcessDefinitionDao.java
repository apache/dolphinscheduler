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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.model.PageListingResult;

import javax.annotation.Nullable;

public interface ProcessDefinitionDao {

    /**
     * Listing the process definition belongs to the given userId and projectCode.
     * If the searchValue is not null, will used at processDefinitionName or processDefinitionDescription.
     */
    // todo: Right now this method will use fuzzy query at searchVal, this will be very slow if there are exist a lot of
    // processDefinition belongs to the target user/project.
    PageListingResult<ProcessDefinition> listingProcessDefinition(
                                                                  int pageNumber,
                                                                  int pageSize,
                                                                  @Nullable String searchVal,
                                                                  int userId,
                                                                  long projectCode);

}
