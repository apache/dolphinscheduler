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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

public interface K8sNamespaceDao extends IDao<K8sNamespace> {

    IPage<K8sNamespace> queryK8sNamespacePaging(IPage<K8sNamespace> page, String searchVal);

    boolean existNamespace(String namespace, Long clusterCode);

    List<K8sNamespace> queryNamespaceExceptUserId(int userId);

    List<K8sNamespace> queryAuthedNamespaceListByUserId(Integer userId);

    K8sNamespace queryByNamespaceCode(Long namespaceCode);

    long countByClusterCode(Long clusterCode);
}
