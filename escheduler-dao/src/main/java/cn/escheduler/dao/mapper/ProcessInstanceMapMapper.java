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
package cn.escheduler.dao.mapper;

import cn.escheduler.dao.model.ProcessInstanceMap;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

/**
 * work process instance map mapper
 */
public interface ProcessInstanceMapMapper {

    /**
     * insert process instance relation
     * @param processInstanceMap
     * @return
     */
    @InsertProvider(type = ProcessInstanceMapMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "processInstanceMap.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "processInstanceMap.id", before = false, resultType = int.class)
    int insert(@Param("processInstanceMap") ProcessInstanceMap processInstanceMap);


    /**
     * delete process instance relation by id
     * @param processInstanceMapId
     * @return
     */
    @DeleteProvider(type = ProcessInstanceMapMapperProvider.class, method = "delete")
    int delete(@Param("processInstanceMapId") int processInstanceMapId);

    /**
     * delete process instance relation by parent work process id
     * @param parentProcessId
     * @return
     */
    @DeleteProvider(type = ProcessInstanceMapMapperProvider.class, method = "deleteByParentProcessId")
    int deleteByParentProcessId(@Param("parentProcessId") int parentProcessId);

    /**
     * update process instance relation
     *
     * @param processInstanceMap
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapMapperProvider.class, method = "update")
    int update(@Param("processInstanceMap") ProcessInstanceMap processInstanceMap);

    /**
     * query process instance relation by id
     * @param processMapId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentProcessInstanceId", column = "parent_process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentTaskInstanceId", column = "parent_task_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER)
    })
    @SelectProvider(type = ProcessInstanceMapMapperProvider.class, method = "queryById")
    ProcessInstanceMap queryById(@Param("processMapId") int processMapId);

    /**
     * query by parent instance id
     * @param parentProcessId
     * @param parentTaskId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentProcessInstanceId", column = "parent_process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentTaskInstanceId", column = "parent_task_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER)
    })
    @SelectProvider(type = ProcessInstanceMapMapperProvider.class, method = "queryByParentId")
    ProcessInstanceMap queryByParentId(@Param("parentProcessId") int parentProcessId, @Param("parentTaskId") int parentTaskId);

    /**
     * query relation by sub process id
     * @param subProcessId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentProcessInstanceId", column = "parent_process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "parentTaskInstanceId", column = "parent_task_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER)
    })
    @SelectProvider(type = ProcessInstanceMapMapperProvider.class, method = "queryBySubProcessId")
    ProcessInstanceMap queryBySubProcessId(@Param("subProcessId")Integer subProcessId);
}
