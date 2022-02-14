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
import org.apache.dolphinscheduler.api.service.K8sNameSpaceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.service.k8s.K8sClientService;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
public class K8sNameSpaceServiceImpl extends BaseServiceImpl implements K8sNameSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(K8sNameSpaceServiceImpl.class);

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
     * @param k8s          k8s not null
     * @param owner        owner can null
     * @param tag          can null,if set means just used for one type job,such as flink,spark
     * @param limitsCpu    limits cpu, can null means not limit
     * @param limitsMemory limits memory, can null means not limit
     * @return
     */
    @Override
    public Map<String, Object> createK8sNamespace(User loginUser, String namespace, String k8s, String owner, String tag, Double limitsCpu, Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (StringUtils.isEmpty(k8s)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.K8S);
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, k8s)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, k8s);
            return result;
        }

        K8sNamespace k8sNamespaceObj = new K8sNamespace();
        Date now = new Date();

        k8sNamespaceObj.setNamespace(namespace);
        k8sNamespaceObj.setK8s(k8s);
        k8sNamespaceObj.setOwner(owner);
        k8sNamespaceObj.setTag(tag);
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setOnlineJobNum(0);
        k8sNamespaceObj.setPodReplicas(0);
        k8sNamespaceObj.setPodRequestCpu(0.0);
        k8sNamespaceObj.setPodRequestMemory(0);
        k8sNamespaceObj.setCreateTime(now);
        k8sNamespaceObj.setUpdateTime(now);

        try {
            String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
            k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
        } catch (Exception e) {
            logger.error("namespace create to k8s error", e);
            putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
            return result;
        }

        k8sNamespaceMapper.insert(k8sNamespaceObj);
        putMsg(result, Status.SUCCESS);

        return result;
    }

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
    @Override
    public Map<String, Object> updateK8sNamespace(User loginUser, int id, String owner, String tag, Double limitsCpu, Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }

        Date now = new Date();
        k8sNamespaceObj.setTag(tag);
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setUpdateTime(now);
        k8sNamespaceObj.setOwner(owner);
        try {
            String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
            k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
        } catch (Exception e) {
            logger.error("namespace update to k8s error", e);
            putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
            return result;
        }
        // update to db
        k8sNamespaceMapper.updateById(k8sNamespaceObj);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify namespace and k8s
     *
     * @param namespace namespace
     * @param k8s       k8s
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyNamespaceK8s(String namespace, String k8s) {
        Result<Object> result = new Result<>();
        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (StringUtils.isEmpty(k8s)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.K8S);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, k8s)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, k8s);
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
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }

        k8sClientService.deleteNamespaceToK8s(k8sNamespaceObj.getNamespace(), k8sNamespaceObj.getK8s());
        k8sNamespaceMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * check namespace name exist
     *
     * @param namespace namespace
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    private boolean checkNamespaceExistInDb(String namespace, String k8s) {
        return k8sNamespaceMapper.existNamespace(namespace, k8s) == Boolean.TRUE;
    }

    /**
     * use cpu memory create yaml
     *
     * @param k8sNamespace
     * @return yaml file
     */
    private String genDefaultResourceYaml(K8sNamespace k8sNamespace) {
        //resource use same name with namespace
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
}
