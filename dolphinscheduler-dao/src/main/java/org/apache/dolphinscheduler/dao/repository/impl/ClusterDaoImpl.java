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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ClusterDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class ClusterDaoImpl extends BaseDao<Cluster, ClusterMapper> implements ClusterDao {

    public ClusterDaoImpl(@NonNull ClusterMapper clusterMapper) {
        super(clusterMapper);
    }

    @Override
    public Cluster queryByClusterName(String name) {
        return mybatisMapper.queryByClusterName(name);
    }

    @Override
    public Cluster queryByClusterCode(Long clusterCode) {
        return mybatisMapper.queryByClusterCode(clusterCode);
    }

    @Override
    public List<Cluster> queryAllClusterList() {
        return mybatisMapper.queryAllClusterList();
    }

    @Override
    public IPage<Cluster> queryClusterListPaging(IPage<Cluster> page, String searchName) {
        return mybatisMapper.queryClusterListPaging(page, searchName);
    }

    @Override
    public boolean deleteByCode(Long code) {
        return mybatisMapper.deleteByCode(code) > 0;
    }
}
