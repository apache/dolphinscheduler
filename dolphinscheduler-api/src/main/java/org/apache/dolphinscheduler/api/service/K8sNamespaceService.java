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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * k8s namespace service impl
 */
public interface K8sNamespaceService {

    /**
     * query namespace list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return k8s namespace list
     */
    Result queryListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * create namespace,if not exist on k8s,will create,if exist only register in db
     *
     * @param loginUser    login user
     * @param namespace    namespace
     * @param clusterCode  k8s not null
     * @param limitsCpu    limits cpu, can null means not limit
     * @param limitsMemory limits memory, can null means not limit
     * @return
     */
    Map<String, Object> createK8sNamespace(User loginUser, String namespace, Long clusterCode, Double limitsCpu,
                                           Integer limitsMemory);

    /**
     * update K8s Namespace tag and resource limit
     *
     * @param loginUser    login user
     * @param userName     owner
     * @param limitsCpu    max cpu
     * @param limitsMemory max memory
     * @return
     */
    Map<String, Object> updateK8sNamespace(User loginUser, int id, String userName, Double limitsCpu,
                                           Integer limitsMemory);

    /**
     * verify namespace and k8s
     *
     * @param namespace   namespace
     * @param clusterCode cluster code
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    Result<Object> verifyNamespaceK8s(String namespace, Long clusterCode);

    /**
     * delete namespace by id
     *
     * @param loginUser login user
     * @param id        namespace id
     * @return
     */
    Map<String, Object> deleteNamespaceById(User loginUser, int id);

    /**
     * query unauthorized namespace
     *
     * @param loginUser login user
     * @param userId    user id
     * @return the namespaces which user have not permission to see
     */
    Map<String, Object> queryUnauthorizedNamespace(User loginUser, Integer userId);

    /**
     * query unauthorized namespace
     *
     * @param loginUser login user
     * @param userId    user id
     * @return namespaces which the user have permission to see
     */
    Map<String, Object> queryAuthorizedNamespace(User loginUser, Integer userId);

    /**
     * query namespace can use
     *
     * @param loginUser login user
     * @return namespace list
     */
    List<K8sNamespace> queryNamespaceAvailable(User loginUser);
}
