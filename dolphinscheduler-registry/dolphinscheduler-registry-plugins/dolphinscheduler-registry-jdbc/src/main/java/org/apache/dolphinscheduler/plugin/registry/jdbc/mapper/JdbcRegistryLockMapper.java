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

package org.apache.dolphinscheduler.plugin.registry.jdbc.mapper;

import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryLock;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface JdbcRegistryLockMapper extends BaseMapper<JdbcRegistryLock> {

    @Select("select count(1) from t_ds_jdbc_registry_lock")
    int countAll();

    @Delete("delete from t_ds_jdbc_registry_lock where last_term < #{term}")
    void clearExpireLock(@Param("term") long term);

    @Update({"<script>",
            "update t_ds_jdbc_registry_lock",
            "set last_term = #{term}",
            "where id IN ",
            "<foreach item='id' index='index' collection='ids' open='(' separator=',' close=')'>",
            "   #{id}",
            "</foreach>",
            "</script>"})
    int updateTermByIds(@Param("ids") Collection<Long> ids, @Param("term") long term);
}
