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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface ProcessLineageMapper extends BaseMapper<ProcessLineage> {

    int batchDeleteByProcessDefinitionCode(@Param("processDefinitionCodes") List<Long> processDefinitionCodes);

    int batchInsert(@Param("processLineages") List<ProcessLineage> processLineages);

    List<ProcessLineage> queryByProjectCode(@Param("projectCode") long projectCode);

    List<WorkFlowRelationDetail> queryWorkFlowLineageByCode(@Param("processDefinitionCode") long processDefinitionCode);

    List<WorkFlowRelationDetail> queryWorkFlowLineageByName(@Param("projectCode") long projectCode,
                                                            @Param("processDefinitionName") String processDefinitionName);

    List<ProcessLineage> queryWorkFlowLineageByDept(@Param("deptProjectCode") long deptProjectCode,
                                                    @Param("deptProcessDefinitionCode") long deptProcessDefinitionCode,
                                                    @Param("deptTaskDefinitionCode") long deptTaskDefinitionCode);

    List<ProcessLineage> queryByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    void truncateTable();

}
