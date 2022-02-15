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
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * k8s namespace service impl
 */
public interface K8sNameSpaceService {
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
     * @param k8s          k8s not null
     * @param owner        owner can null
     * @param tag          can null,if set means just used for one type job,such as flink,spark
     * @param limitsCpu    limits cpu, can null means not limit
     * @param limitsMemory limits memory, can null means not limit
     * @return
     */
    Map<String, Object> createK8sNamespace(User loginUser, String namespace, String k8s, String owner, String tag, Double limitsCpu, Integer limitsMemory);


    /**
     * update K8s Namespace tag and resource limit
     *
     * @param loginUser    login user
     * @param owner        owner
     * @param tag          Which type of job is available,such as flink,means only flink job can use, can be empty, all available
     * @param limitsCpu    max cpu
     * @param limitsMemory max memory
     * @return
     */
    Map<String, Object> updateK8sNamespace(User loginUser, int id, String owner, String tag, Double limitsCpu, Integer limitsMemory);

    /**
     * verify namespace and k8s
     *
     * @param namespace namespace
     * @param k8s       k8s
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    Result<Object> verifyNamespaceK8s(String namespace, String k8s);

    /**
     * delete namespace by id
     *
     * @param loginUser login user
     * @param id        namespace id
     * @return
     */
    Map<String, Object> deleteNamespaceById(User loginUser, int id);
}
