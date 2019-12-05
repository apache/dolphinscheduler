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

import cn.escheduler.dao.model.Queue;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * queue mapper
 */
public interface MonitorMapper {

    /**
     * insert queue
     * @param queue
     * @return
     */
    @InsertProvider(type = QueueMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "queue.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "queue.id", before = false, resultType = int.class)
    int insert(@Param("queue") Queue queue);


    /**
     * delete queue
     * @param queueId
     * @return
     */
    @DeleteProvider(type = QueueMapperProvider.class, method = "delete")
    int delete(@Param("queueId") int queueId);


    /**
     * update queue
     *
     * @param queue
     * @return
     */
    @UpdateProvider(type = QueueMapperProvider.class, method = "update")
    int update(@Param("queue") Queue queue);


    /**
     * query queue by id
     * @param queueId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queue", column = "queue", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = QueueMapperProvider.class, method = "queryById")
    Queue queryById(@Param("queueId") int queueId);


    /**
     * query all queue list
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queue", column = "queue", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = QueueMapperProvider.class, method = "queryAllQueue")
    List<Queue> queryAllQueue();





}
