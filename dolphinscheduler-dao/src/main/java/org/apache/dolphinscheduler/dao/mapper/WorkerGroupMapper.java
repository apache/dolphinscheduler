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

import static org.apache.dolphinscheduler.common.constants.Constants.CACHE_KEY_VALUE_ALL;

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * worker group mapper interface
 */
@CacheConfig(cacheNames = "workerGroup", keyGenerator = "cacheKeyGenerator")
public interface WorkerGroupMapper extends BaseMapper<WorkerGroup> {

    /**
     * query all worker group
     *
     * @return worker group list
     */
    @Cacheable(sync = true, key = CACHE_KEY_VALUE_ALL)
    List<WorkerGroup> queryAllWorkerGroup();

    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int deleteById(Integer id);

    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int insert(WorkerGroup entity);

    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int updateById(@Param("et") WorkerGroup entity);

    /**
     * query worker group by name
     *
     * @param name name
     * @return worker group list
     */
    List<WorkerGroup> queryWorkerGroupByName(@Param("name") String name);

}
