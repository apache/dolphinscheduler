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

import org.apache.dolphinscheduler.dao.entity.Cluster;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * cluster mapper interface
 */
public interface ClusterMapper extends BaseMapper<Cluster> {

    /**
     * query cluster by name
     *
     * @param name name
     * @return cluster
     */
    Cluster queryByClusterName(@Param("clusterName") String name);

    /**
     * query cluster by code
     *
     * @param clusterCode clusterCode
     * @return cluster
     */
    Cluster queryByClusterCode(@Param("clusterCode") Long clusterCode);

    /**
     * query all cluster list
     * @return cluster list
     */
    List<Cluster> queryAllClusterList();

    /**
     * cluster page
     * @param page page
     * @param searchName searchName
     * @return cluster IPage
     */
    IPage<Cluster> queryClusterListPaging(IPage<Cluster> page, @Param("searchName") String searchName);

    /**
     * delete cluster by code
     *
     * @param code code
     * @return int
     */
    int deleteByCode(@Param("code") Long code);
}
