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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.k8s.K8sClientService;
import org.apache.dolphinscheduler.api.service.K8sNamespaceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * k8s namespace service impl
 */
@Service
public class K8SNamespaceServiceImpl extends BaseServiceImpl implements K8sNamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(K8SNamespaceServiceImpl.class);

    private static String resourceYaml = "apiVersion: v1\n"
            + "kind: ResourceQuota\n"
            + "metadata:\n"
            + "  name: ${name}\n"
            + "  namespace: ${namespace}\n"
            + "spec:\n"
            + "  hard:\n"
            + "    ${limitCpu}\n"
            + "    ${limitMemory}\n";

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Autowired
    private K8sClientService k8sClientService;

    @Autowired
    private ClusterMapper clusterMapper;

    /**
     * query namespace list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return k8s namespace list
     */
    @Override
    public Result queryListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            logger.warn("Only admin can query namespace list, current login user name:{}.", loginUser.getUserName());
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<K8sNamespace> page = new Page<>(pageNo, pageSize);

        IPage<K8sNamespace> k8sNamespaceList = k8sNamespaceMapper.queryK8sNamespacePaging(page, searchVal);

        Integer count = (int) k8sNamespaceList.getTotal();
        PageInfo<K8sNamespace> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal(count);
        pageInfo.setTotalList(k8sNamespaceList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

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
    @Override
    public Map<String, Object> createK8sNamespace(User loginUser, String namespace, Long clusterCode, Double limitsCpu,
                                                  Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can create K8s namespace, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        if (StringUtils.isEmpty(namespace)) {
            logger.warn("Parameter namespace is empty.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (clusterCode == null) {
            logger.warn("Parameter clusterCode is null.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.CLUSTER);
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            logger.warn("Parameter limitsCpu is invalid.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            logger.warn("Parameter limitsMemory is invalid.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, clusterCode)) {
            logger.warn("K8S namespace already exists.");
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, clusterCode);
            return result;
        }

        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            logger.error("Cluster does not exist, clusterCode:{}", clusterCode);
            putMsg(result, Status.CLUSTER_NOT_EXISTS, namespace, clusterCode);
            return result;
        }

        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            cluster.setCode(code);
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Generate cluster code error.", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating cluster code");
            return result;
        }

        K8sNamespace k8sNamespaceObj = new K8sNamespace();
        Date now = new Date();

        k8sNamespaceObj.setCode(code);
        k8sNamespaceObj.setNamespace(namespace);
        k8sNamespaceObj.setClusterCode(clusterCode);
        k8sNamespaceObj.setUserId(loginUser.getId());
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setPodReplicas(0);
        k8sNamespaceObj.setPodRequestCpu(0.0);
        k8sNamespaceObj.setPodRequestMemory(0);
        k8sNamespaceObj.setCreateTime(now);
        k8sNamespaceObj.setUpdateTime(now);

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
                k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
            } catch (Exception e) {
                logger.error("Namespace create to k8s error", e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
                return result;
            }
        }

        k8sNamespaceMapper.insert(k8sNamespaceObj);
        logger.info("K8s namespace create complete, namespace:{}.", k8sNamespaceObj.getNamespace());
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * update K8s Namespace tag and resource limit
     *
     * @param loginUser    login user
     * @param userName     owner
     * @param limitsCpu    max cpu
     * @param limitsMemory max memory
     * @return
     */
    @Override
    public Map<String, Object> updateK8sNamespace(User loginUser, int id, String userName, Double limitsCpu,
                                                  Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can update K8s namespace, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            logger.warn("Parameter limitsCpu is invalid.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            logger.warn("Parameter limitsMemory is invalid.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            logger.error("K8s namespace does not exist, namespaceId:{}.", id);
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }

        Date now = new Date();
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setUpdateTime(now);

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
                k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
            } catch (Exception e) {
                logger.error("Namespace update to k8s error", e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
                return result;
            }
        }
        // update to db
        k8sNamespaceMapper.updateById(k8sNamespaceObj);
        logger.info("K8s namespace update complete, namespace:{}.", k8sNamespaceObj.getNamespace());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify namespace and k8s
     *
     * @param namespace   namespace
     * @param clusterCode cluster code
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyNamespaceK8s(String namespace, Long clusterCode) {
        Result<Object> result = new Result<>();
        if (StringUtils.isEmpty(namespace)) {
            logger.warn("Parameter namespace is empty.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (clusterCode == null) {
            logger.warn("Parameter clusterCode is null.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.CLUSTER);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, clusterCode)) {
            logger.warn("K8S namespace already exists.");
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, clusterCode);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete namespace by id
     *
     * @param loginUser login user
     * @param id        namespace id
     * @return
     */
    @Override
    public Map<String, Object> deleteNamespaceById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            logger.warn("Only admin can delete K8s namespace, current login user name:{}.", loginUser.getUserName());
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            logger.error("K8s namespace does not exist, namespaceId:{}.", id);
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }
        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                k8sClientService.deleteNamespaceToK8s(k8sNamespaceObj.getNamespace(), k8sNamespaceObj.getClusterCode());
            } catch (RemotingException e) {
                logger.error("Namespace delete in k8s error, namespaceId:{}.", id, e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, id);
                return result;
            }
        }
        k8sNamespaceMapper.deleteById(id);
        logger.info("K8s namespace delete complete, namespace:{}.", k8sNamespaceObj.getNamespace());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * check namespace name exist
     *
     * @param namespace namespace
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    private boolean checkNamespaceExistInDb(String namespace, Long clusterCode) {
        return k8sNamespaceMapper.existNamespace(namespace, clusterCode) == Boolean.TRUE;
    }

    /**
     * use cpu memory create yaml
     *
     * @param k8sNamespace
     * @return yaml file
     */
    private String genDefaultResourceYaml(K8sNamespace k8sNamespace) {
        // resource use same name with namespace
        String name = k8sNamespace.getNamespace();
        String namespace = k8sNamespace.getNamespace();
        String cpuStr = null;
        if (k8sNamespace.getLimitsCpu() != null) {
            cpuStr = k8sNamespace.getLimitsCpu() + "";
        }

        String memoryStr = null;
        if (k8sNamespace.getLimitsMemory() != null) {
            memoryStr = k8sNamespace.getLimitsMemory() + "Gi";
        }

        String result = resourceYaml.replace("${name}", name)
                .replace("${namespace}", namespace);
        if (cpuStr == null) {
            result = result.replace("${limitCpu}", "");
        } else {
            result = result.replace("${limitCpu}", "limits.cpu: '" + cpuStr + "'");
        }

        if (memoryStr == null) {
            result = result.replace("${limitMemory}", "");
        } else {
            result = result.replace("${limitMemory}", "limits.memory: " + memoryStr);
        }
        return result;
    }

    /**
     * query unauthorized namespace
     *
     * @param loginUser login user
     * @param userId    user id
     * @return the namespaces which user have not permission to see
     */
    @Override
    public Map<String, Object> queryUnauthorizedNamespace(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (loginUser.getId() != userId && isNotAdmin(loginUser, result)) {
            return result;
        }
        // query all namespace list, this auth does not like project
        List<K8sNamespace> namespaceList = k8sNamespaceMapper.selectList(null);
        List<K8sNamespace> resultList = new ArrayList<>();
        Set<K8sNamespace> namespaceSet;
        if (namespaceList != null && !namespaceList.isEmpty()) {
            namespaceSet = new HashSet<>(namespaceList);
            List<K8sNamespace> authedProjectList = k8sNamespaceMapper.queryAuthedNamespaceListByUserId(userId);
            resultList = getUnauthorizedNamespaces(namespaceSet, authedProjectList);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query authorized namespace
     *
     * @param loginUser login user
     * @param userId    user id
     * @return namespaces which the user have permission to see
     */
    @Override
    public Map<String, Object> queryAuthorizedNamespace(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        if (loginUser.getId() != userId && isNotAdmin(loginUser, result)) {
            return result;
        }

        List<K8sNamespace> namespaces = k8sNamespaceMapper.queryAuthedNamespaceListByUserId(userId);
        result.put(Constants.DATA_LIST, namespaces);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query namespace can use
     *
     * @param loginUser login user
     * @return namespace list
     */
    @Override
    public List<K8sNamespace> queryNamespaceAvailable(User loginUser) {
        List<K8sNamespace> k8sNamespaces;
        if (isAdmin(loginUser)) {
            k8sNamespaces = k8sNamespaceMapper.selectList(null);
        } else {
             k8sNamespaces = k8sNamespaceMapper.queryNamespaceAvailable(loginUser.getId());
        }
        setClusterName(k8sNamespaces);
        return k8sNamespaces;
    }

    /**
     * set cluster_name
     * @param k8sNamespaces source data
     */
    private void setClusterName(List<K8sNamespace> k8sNamespaces) {
        if (CollectionUtils.isNotEmpty(k8sNamespaces)) {
            List<Cluster> clusters = clusterMapper.queryAllClusterList();
            if (CollectionUtils.isNotEmpty(clusters)) {
                Map<Long, String> codeNameMap = clusters.stream()
                        .collect(Collectors.toMap(Cluster::getCode, Cluster::getName, (a, b) -> a));
                for (K8sNamespace k8sNamespace : k8sNamespaces) {
                    String clusterName = codeNameMap.get(k8sNamespace.getClusterCode());
                    k8sNamespace.setClusterName(clusterName);
                }
            }
        }
    }

    /**
     * get unauthorized namespace
     *
     * @param namespaceSet        namespace set
     * @param authedNamespaceList authed namespace list
     * @return namespace list that authorization
     */
    private List<K8sNamespace> getUnauthorizedNamespaces(Set<K8sNamespace> namespaceSet,
                                                         List<K8sNamespace> authedNamespaceList) {
        List<K8sNamespace> resultList = new ArrayList<>();
        for (K8sNamespace k8sNamespace : namespaceSet) {
            boolean existAuth = false;
            if (authedNamespaceList != null && !authedNamespaceList.isEmpty()) {
                for (K8sNamespace k8sNamespaceAuth : authedNamespaceList) {
                    if (k8sNamespace.equals(k8sNamespaceAuth)) {
                        existAuth = true;
                    }
                }
            }

            if (!existAuth) {
                resultList.add(k8sNamespace);
            }
        }
        return resultList;
    }
}
