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

import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * process instance map mapper interface
 */
public interface ProcessInstanceMapMapper extends BaseMapper<ProcessInstanceMap> {

    /**
     * query process instance by parentId
     * @param parentProcessId parentProcessId
     * @param parentTaskId parentTaskId
     * @return process instance map
     */
    ProcessInstanceMap queryByParentId(@Param("parentProcessId") int parentProcessId,
                                       @Param("parentTaskId") int parentTaskId);


    /**
     * query by sub process id
     * @param subProcessId subProcessId
     * @return process instance map
     */
    ProcessInstanceMap queryBySubProcessId(@Param("subProcessId") Integer subProcessId);

    /**
     * delete by parent process id
     * @param parentProcessId parentProcessId
     * @return delete result
     */
    int deleteByParentProcessId(@Param("parentProcessId") int parentProcessId);

    /**
     *  query sub process instance  ids by parent instance id
     * @param parentInstanceId parentInstanceId
     * @return sub process instance ids
     */
    List<Integer> querySubIdListByParentId(@Param("parentInstanceId") int parentInstanceId);

}
