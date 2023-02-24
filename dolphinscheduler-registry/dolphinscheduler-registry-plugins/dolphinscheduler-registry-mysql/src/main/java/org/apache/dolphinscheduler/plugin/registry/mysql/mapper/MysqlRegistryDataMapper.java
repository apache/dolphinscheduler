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

package org.apache.dolphinscheduler.plugin.registry.mysql.mapper;

import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryData;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface MysqlRegistryDataMapper extends BaseMapper<MysqlRegistryData> {

    @Select("select * from t_ds_mysql_registry_data")
    List<MysqlRegistryData> selectAll();

    @Select("select * from t_ds_mysql_registry_data where `key` = #{key}")
    MysqlRegistryData selectByKey(@Param("key") String key);

    @Select("select * from t_ds_mysql_registry_data where `key` like CONCAT (#{key}, '%')")
    List<MysqlRegistryData> fuzzyQueryByKey(@Param("key") String key);

    @Update("update t_ds_mysql_registry_data set `data` = #{data}, `last_term` = #{term} where `id` = #{id}")
    int updateDataAndTermById(@Param("id") long id, @Param("data") String data, @Param("term") long term);

    @Delete("delete from t_ds_mysql_registry_data where `key` = #{key}")
    void deleteByKey(@Param("key") String key);

    @Delete("delete from t_ds_mysql_registry_data where `last_term` < #{term} and `type` = #{type}")
    void clearExpireEphemeralDate(@Param("term") long term, @Param("type") int type);

    @Update({"<script>",
            "update t_ds_mysql_registry_data",
            "set `last_term` = #{term}",
            "where id IN ",
            "<foreach item='id' index='index' collection='ids' open='(' separator=',' close=')'>",
            "   #{id}",
            "</foreach>",
            "</script>"})
    int updateTermByIds(@Param("ids") Collection<Long> ids, @Param("term") long term);
}
