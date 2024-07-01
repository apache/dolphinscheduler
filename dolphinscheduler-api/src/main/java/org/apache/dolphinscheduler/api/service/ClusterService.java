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

import org.apache.dolphinscheduler.api.dto.ClusterDto;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * cluster service
 */
public interface ClusterService {

    /**
     * create cluster
     *
     * @param loginUser login user
     * @param name      cluster name
     * @param config    cluster config
     * @param desc      cluster desc
     * @return cluster code
     */
    Long createCluster(User loginUser, String name, String config, String desc);

    /**
     * query cluster
     *
     * @param name cluster name
     */
    ClusterDto queryClusterByName(String name);

    /**
     * query cluster
     *
     * @param code cluster code
     */
    ClusterDto queryClusterByCode(Long code);

    /**
     * delete cluster
     *
     * @param loginUser login user
     * @param code      cluster code
     */
    void deleteClusterByCode(User loginUser, Long code);

    /**
     * update cluster
     *
     * @param loginUser login user
     * @param code      cluster code
     * @param name      cluster name
     * @param config    cluster config
     * @param desc      cluster desc
     */
    Cluster updateClusterByCode(User loginUser, Long code, String name, String config, String desc);

    /**
     * query cluster paging
     *
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return cluster list page
     */
    PageInfo<ClusterDto> queryClusterListPaging(Integer pageNo, Integer pageSize, String searchVal);

    /**
     * query all cluster
     *
     * @return all cluster list
     */
    List<ClusterDto> queryAllClusterList();

    /**
     * verify cluster name
     *
     * @param clusterName cluster name
     * @return true if the cluster name not exists, otherwise return false
     */
    void verifyCluster(String clusterName);

}
