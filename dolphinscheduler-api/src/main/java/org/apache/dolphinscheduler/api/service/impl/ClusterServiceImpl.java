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
import org.apache.dolphinscheduler.api.k8s.K8sManager;
import org.apache.dolphinscheduler.api.service.ClusterService;
import org.apache.dolphinscheduler.api.utils.ClusterConfUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ClusterServiceImpl extends BaseServiceImpl implements ClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);

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
    public Map<String, Object> createCluster(User loginUser, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can create cluster, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        Map<String, Object> checkResult = checkParams(name, config);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null) {
            logger.warn("Cluster with the same name already exists, clusterName:{}.", clusterExistByName.getName());
            putMsg(result, Status.CLUSTER_NAME_EXISTS, name);
            return result;
        }

        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setConfig(config);
        cluster.setDescription(desc);
        cluster.setOperator(loginUser.getId());
        cluster.setCreateTime(new Date());
        cluster.setUpdateTime(new Date());
        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            cluster.setCode(code);
        } catch (CodeGenerateException e) {
            logger.error("Generate cluster code error.", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating cluster code");
            return result;
        }

        if (clusterMapper.insert(cluster) > 0) {
            logger.info("Cluster create complete, clusterName:{}.", cluster.getName());
            result.put(Constants.DATA_LIST, cluster.getCode());
            putMsg(result, Status.SUCCESS);
        } else {
            logger.error("Cluster create error, clusterName:{}.", cluster.getName());
            putMsg(result, Status.CREATE_CLUSTER_ERROR);
        }
        return result;
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
    public Result queryClusterListPaging(Integer pageNo, Integer pageSize, String searchVal) {
        Result result = new Result();

        Page<Cluster> page = new Page<>(pageNo, pageSize);

        IPage<Cluster> clusterIPage = clusterMapper.queryClusterListPaging(page, searchVal);

        PageInfo<ClusterDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) clusterIPage.getTotal());

        if (CollectionUtils.isNotEmpty(clusterIPage.getRecords())) {

            List<ClusterDto> dtoList = clusterIPage.getRecords().stream().map(cluster -> {
                ClusterDto dto = new ClusterDto();
                BeanUtils.copyProperties(cluster, dto);
                return dto;
            }).collect(Collectors.toList());

            pageInfo.setTotalList(dtoList);
        } else {
            pageInfo.setTotalList(new ArrayList<>());
        }

        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all cluster
     *
     * @return all cluster list
     */
    @Override
    public Map<String, Object> queryAllClusterList() {
        Map<String, Object> result = new HashMap<>();
        List<Cluster> clusterList = clusterMapper.queryAllClusterList();

        if (CollectionUtils.isNotEmpty(clusterList)) {

            List<ClusterDto> dtoList = clusterList.stream().map(cluster -> {
                ClusterDto dto = new ClusterDto();
                BeanUtils.copyProperties(cluster, dto);
                return dto;
            }).collect(Collectors.toList());
            result.put(Constants.DATA_LIST, dtoList);
        } else {
            result.put(Constants.DATA_LIST, new ArrayList<>());
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query cluster
     *
     * @param code cluster code
     */
    @Override
    public Map<String, Object> queryClusterByCode(Long code) {
        Map<String, Object> result = new HashMap<>();

        Cluster cluster = clusterMapper.queryByClusterCode(code);

        if (cluster == null) {
            putMsg(result, Status.QUERY_CLUSTER_BY_CODE_ERROR, code);
        } else {

            ClusterDto dto = new ClusterDto();
            BeanUtils.copyProperties(cluster, dto);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * query cluster
     *
     * @param name cluster name
     */
    @Override
    public Map<String, Object> queryClusterByName(String name) {
        Map<String, Object> result = new HashMap<>();

        Cluster cluster = clusterMapper.queryByClusterName(name);
        if (cluster == null) {
            logger.warn("Cluster does not exist, name:{}.", name);
            putMsg(result, Status.QUERY_CLUSTER_BY_NAME_ERROR, name);
        } else {

            ClusterDto dto = new ClusterDto();
            BeanUtils.copyProperties(cluster, dto);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * delete cluster
     *
     * @param loginUser login user
     * @param code      cluster code
     */
    @Transactional
    @Override
    public Map<String, Object> deleteClusterByCode(User loginUser, Long code) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can delete cluster, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        Long relatedNamespaceNumber = k8sNamespaceMapper
                .selectCount(new QueryWrapper<K8sNamespace>().lambda().eq(K8sNamespace::getClusterCode, code));

        if (relatedNamespaceNumber > 0) {
            logger.warn("Delete cluster failed because {} namespace(s) is(are) using it, clusterCode:{}.", relatedNamespaceNumber, code);
            putMsg(result, Status.DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS);
            return result;
        }

        int delete = clusterMapper.deleteByCode(code);
        if (delete > 0) {
            logger.info("Delete cluster complete, clusterCode:{}.", code);
            putMsg(result, Status.SUCCESS);
        } else {
            logger.error("Delete cluster error, clusterCode:{}.", code);
            putMsg(result, Status.DELETE_CLUSTER_ERROR);
        }
        return result;
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
    @Transactional
    @Override
    public Map<String, Object> updateClusterByCode(User loginUser, Long code, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can update cluster, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        if (checkDescriptionLength(desc)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }

        Map<String, Object> checkResult = checkParams(name, config);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null && !clusterExistByName.getCode().equals(code)) {
            logger.warn("Cluster with the same name already exists, name:{}.", clusterExistByName.getName());
            putMsg(result, Status.CLUSTER_NAME_EXISTS, name);
            return result;
        }

        Cluster clusterExist = clusterMapper.queryByClusterCode(code);
        if (clusterExist == null) {
            logger.error("Cluster does not exist, code:{}.", code);
            putMsg(result, Status.CLUSTER_NOT_EXISTS, name);
            return result;
        }

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(clusterExist.getCode())
                && !config.equals(ClusterConfUtils.getK8sConfig(clusterExist.getConfig()))) {
            try {
                k8sManager.getAndUpdateK8sClient(code, true);
            } catch (RemotingException e) {
                logger.error("Update K8s error.", e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, name);
                return result;
            }
        }

        // update cluster
        clusterExist.setConfig(config);
        clusterExist.setName(name);
        clusterExist.setDescription(desc);
        clusterMapper.updateById(clusterExist);
        // need not update relation
        logger.info("Cluster update complete, clusterId:{}.", clusterExist.getId());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify cluster name
     *
     * @param clusterName cluster name
     * @return true if the cluster name not exists, otherwise return false
     */
    @Override
    public Map<String, Object> verifyCluster(String clusterName) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(clusterName)) {
            logger.warn("Parameter cluster name is empty.");
            putMsg(result, Status.CLUSTER_NAME_IS_NULL);
            return result;
        }

        Cluster cluster = clusterMapper.queryByClusterName(clusterName);
        if (cluster != null) {
            logger.warn("Cluster with the same name already exists, name:{}.", cluster.getName());
            putMsg(result, Status.CLUSTER_NAME_EXISTS, clusterName);
            return result;
        }

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    public Map<String, Object> checkParams(String name, String config) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(name)) {
            logger.warn("Parameter cluster name is empty.");
            putMsg(result, Status.CLUSTER_NAME_IS_NULL);
            return result;
        }
        if (StringUtils.isEmpty(config)) {
            logger.warn("Parameter cluster config is empty.");
            putMsg(result, Status.CLUSTER_CONFIG_IS_NULL);
            return result;
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

}
