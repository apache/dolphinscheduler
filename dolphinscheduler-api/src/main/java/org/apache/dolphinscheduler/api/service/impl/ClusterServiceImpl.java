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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.dto.ClusterDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.k8s.K8sManager;
import org.apache.dolphinscheduler.api.service.ClusterService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * cluster definition service impl
 */
@Service
@Slf4j
public class ClusterServiceImpl extends BaseServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private K8sManager k8sManager;

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;

    /**
     * create cluster
     *
     * @param loginUser login user
     * @param name      cluster name
     * @param config    cluster config
     * @param desc      cluster desc
     */
    @Transactional
    @Override
    public Long createCluster(User loginUser, String name, String config, String desc) {
        if (isNotAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        checkParams(name, config);

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null) {
            throw new ServiceException(Status.CLUSTER_NAME_EXISTS, name);
        }

        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setConfig(config);
        cluster.setDescription(desc);
        cluster.setOperator(loginUser.getId());
        cluster.setCreateTime(new Date());
        cluster.setUpdateTime(new Date());
        cluster.setCode(CodeGenerateUtils.getInstance().genCode());

        if (clusterMapper.insert(cluster) > 0) {
            return cluster.getCode();
        }
        throw new ServiceException(Status.CREATE_CLUSTER_ERROR);
    }

    /**
     * query cluster paging
     *
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return cluster list page
     */
    @Override
    public PageInfo<ClusterDto> queryClusterListPaging(Integer pageNo, Integer pageSize, String searchVal) {

        Page<Cluster> page = new Page<>(pageNo, pageSize);

        IPage<Cluster> clusterIPage = clusterMapper.queryClusterListPaging(page, searchVal);

        PageInfo<ClusterDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) clusterIPage.getTotal());

        if (CollectionUtils.isEmpty(clusterIPage.getRecords())) {
            return pageInfo;
        }
        List<ClusterDto> dtoList = clusterIPage.getRecords().stream().map(cluster -> {
            ClusterDto dto = new ClusterDto();
            BeanUtils.copyProperties(cluster, dto);
            return dto;
        }).collect(Collectors.toList());
        pageInfo.setTotalList(dtoList);
        return pageInfo;
    }

    /**
     * query all cluster
     *
     * @return all cluster list
     */
    @Override
    public List<ClusterDto> queryAllClusterList() {
        List<Cluster> clusterList = clusterMapper.queryAllClusterList();
        if (CollectionUtils.isEmpty(clusterList)) {
            return Collections.emptyList();
        }

        return clusterList.stream()
                .map(cluster -> {
                    ClusterDto dto = new ClusterDto();
                    // todo: Don't use copy
                    BeanUtils.copyProperties(cluster, dto);
                    return dto;
                }).collect(Collectors.toList());
    }

    /**
     * query cluster
     *
     * @param code cluster code
     */
    @Override
    public ClusterDto queryClusterByCode(Long code) {

        Cluster cluster = clusterMapper.queryByClusterCode(code);

        if (cluster == null) {
            throw new ServiceException(Status.QUERY_CLUSTER_BY_CODE_ERROR, code);
        }
        ClusterDto dto = new ClusterDto();
        BeanUtils.copyProperties(cluster, dto);
        return dto;
    }

    /**
     * query cluster
     *
     * @param name cluster name
     */
    @Override
    public ClusterDto queryClusterByName(String name) {

        Cluster cluster = clusterMapper.queryByClusterName(name);
        if (cluster == null) {
            throw new ServiceException(Status.QUERY_CLUSTER_BY_NAME_ERROR, name);
        }
        ClusterDto dto = new ClusterDto();
        BeanUtils.copyProperties(cluster, dto);
        return dto;
    }

    /**
     * delete cluster
     *
     * @param loginUser login user
     * @param code      cluster code
     */
    @Override
    public void deleteClusterByCode(User loginUser, Long code) {
        if (isNotAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Long relatedNamespaceNumber = k8sNamespaceMapper
                .selectCount(new QueryWrapper<K8sNamespace>().lambda().eq(K8sNamespace::getClusterCode, code));

        if (relatedNamespaceNumber > 0) {
            throw new ServiceException(Status.DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS);
        }

        int delete = clusterMapper.deleteByCode(code);
        if (delete > 0) {
            return;
        }
        throw new ServiceException(Status.DELETE_CLUSTER_ERROR);
    }

    /**
     * update cluster
     *
     * @param loginUser login user
     * @param code      cluster code
     * @param name      cluster name
     * @param config    cluster config
     * @param desc      cluster desc
     */
    @Override
    public Cluster updateClusterByCode(User loginUser,
                                       Long code,
                                       String name,
                                       String config,
                                       String desc) {
        if (isNotAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        if (checkDescriptionLength(desc)) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }

        checkParams(name, config);

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null && !clusterExistByName.getCode().equals(code)) {
            throw new ServiceException(Status.CLUSTER_NAME_EXISTS, name);
        }

        Cluster clusterExist = clusterMapper.queryByClusterCode(code);
        if (clusterExist == null) {
            throw new ServiceException(Status.CLUSTER_NOT_EXISTS, name);
        }

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(clusterExist.getCode())
                && !config.equals(ClusterConfUtils.getK8sConfig(clusterExist.getConfig()))) {
            try {
                k8sManager.getAndUpdateK8sClient(code, true);
            } catch (Exception e) {
                throw new ServiceException(Status.K8S_CLIENT_OPS_ERROR, name);
            }
        }

        // update cluster
        // need not update relation
        clusterExist.setConfig(config);
        clusterExist.setName(name);
        clusterExist.setDescription(desc);
        clusterMapper.updateById(clusterExist);
        return clusterExist;
    }

    /**
     * verify cluster name
     *
     * @param clusterName cluster name
     * @return true if the cluster name not exists, otherwise return false
     */
    @Override
    public void verifyCluster(String clusterName) {

        if (StringUtils.isEmpty(clusterName)) {
            throw new ServiceException(Status.CLUSTER_NAME_IS_NULL);
        }

        Cluster cluster = clusterMapper.queryByClusterName(clusterName);
        if (cluster != null) {
            throw new ServiceException(Status.CLUSTER_NAME_EXISTS);
        }
    }

    protected void checkParams(String name, String config) {
        if (StringUtils.isEmpty(name)) {
            throw new ServiceException(Status.CLUSTER_NAME_IS_NULL);
        }
        if (StringUtils.isEmpty(config)) {
            throw new ServiceException(Status.CLUSTER_CONFIG_IS_NULL);
        }
    }

}
