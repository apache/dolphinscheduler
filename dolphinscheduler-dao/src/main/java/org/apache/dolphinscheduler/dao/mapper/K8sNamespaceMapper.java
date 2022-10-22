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

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * namespace interface
 */
public interface K8sNamespaceMapper extends BaseMapper<K8sNamespace> {

    /**
     * k8s namespace page
     *
     * @param page      page
     * @param searchVal searchVal
     * @return k8s namespace IPage
     */
    IPage<K8sNamespace> queryK8sNamespacePaging(IPage<K8sNamespace> page,
                                                @Param("searchVal") String searchVal);

    /**
     * check the target namespace exist
     *
     * @param namespace   namespace
     * @param clusterCode clusterCode
     * @return true if exist else return null
     */
    Boolean existNamespace(@Param("namespace") String namespace, @Param("clusterCode") Long clusterCode);

    /**
     * query namespace except userId
     *
     * @param userId userId
     * @return namespace list
     */
    List<K8sNamespace> queryNamespaceExceptUserId(@Param("userId") int userId);

    /**
     * query authed namespace list by userId
     *
     * @param userId userId
     * @return namespace list
     */
    List<K8sNamespace> queryAuthedNamespaceListByUserId(@Param("userId") Integer userId);

    /**
     * check the target namespace
     *
     * @param namespaceCode namespaceCode
     * @return true if exist else return null
     */
    K8sNamespace queryByNamespaceCode(@Param("clusterCode") Long namespaceCode);
}
