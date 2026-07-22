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

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class K8sNamespaceDaoImpl extends BaseDao<K8sNamespace, K8sNamespaceMapper> implements K8sNamespaceDao {

    public K8sNamespaceDaoImpl(@NonNull K8sNamespaceMapper k8sNamespaceMapper) {
        super(k8sNamespaceMapper);
    }

    @Override
    public IPage<K8sNamespace> queryK8sNamespacePaging(IPage<K8sNamespace> page, String searchVal) {
        return mybatisMapper.queryK8sNamespacePaging(page, searchVal);
    }

    @Override
    public boolean existNamespace(String namespace, Long clusterCode) {
        return Boolean.TRUE.equals(mybatisMapper.existNamespace(namespace, clusterCode));
    }

    @Override
    public List<K8sNamespace> queryNamespaceExceptUserId(int userId) {
        return mybatisMapper.queryNamespaceExceptUserId(userId);
    }

    @Override
    public List<K8sNamespace> queryAuthedNamespaceListByUserId(Integer userId) {
        return mybatisMapper.queryAuthedNamespaceListByUserId(userId);
    }

    @Override
    public K8sNamespace queryByNamespaceCode(Long namespaceCode) {
        return mybatisMapper.queryByNamespaceCode(namespaceCode);
    }

    @Override
    public long countByClusterCode(Long clusterCode) {
        return mybatisMapper.selectCount(
                new QueryWrapper<K8sNamespace>().lambda().eq(K8sNamespace::getClusterCode, clusterCode));
    }
}
