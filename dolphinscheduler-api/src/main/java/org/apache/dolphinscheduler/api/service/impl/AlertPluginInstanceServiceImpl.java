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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALART_INSTANCE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_UPDATE;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.extract.alert.IAlertOperator;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.extract.alert.request.AlertTestSendRequest;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert plugin instance service impl
 */
@Service
@Lazy
@Slf4j
public class AlertPluginInstanceServiceImpl extends BaseServiceImpl implements AlertPluginInstanceService {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    private final Integer GLOBAL_ALERT_GROUP_ID = 2;

    @Autowired
    private RegistryClient registryClient;

    /**
     * creat alert plugin instance
     *
     * @param loginUser            login user
     * @param pluginDefineId       plugin define id
     * @param instanceName         instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public AlertPluginInstance create(User loginUser,
                                      int pluginDefineId,
                                      String instanceName,
                                      AlertPluginInstanceType instanceType,
                                      WarningType warningType,
                                      String pluginInstanceParams) {

        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ALERT_PLUGIN_INSTANCE, ALART_INSTANCE_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        alertPluginInstance.setPluginInstanceParams(paramsMapJson);
        alertPluginInstance.setInstanceName(instanceName);
        alertPluginInstance.setPluginDefineId(pluginDefineId);
        alertPluginInstance.setInstanceType(instanceType);
        alertPluginInstance.setWarningType(warningType);

        if (alertPluginInstanceMapper.existInstanceName(alertPluginInstance.getInstanceName()) == Boolean.TRUE) {
            throw new ServiceException(Status.PLUGIN_INSTANCE_ALREADY_EXISTS);
        }

        int i = alertPluginInstanceMapper.insert(alertPluginInstance);
        if (i > 0) {
            log.info("Create alert plugin instance complete, name:{}", alertPluginInstance.getInstanceName());
            // global instance will be added into global alert group automatically
            if (instanceType == AlertPluginInstanceType.GLOBAL) {
                AlertGroup globalAlertGroup = alertGroupMapper.selectById(GLOBAL_ALERT_GROUP_ID);
                if (StringUtils.isEmpty(globalAlertGroup.getAlertInstanceIds())) {
                    globalAlertGroup.setAlertInstanceIds(String.valueOf(alertPluginInstance.getId()));
                } else {
                    List<Integer> ids = Arrays.stream(globalAlertGroup.getAlertInstanceIds().split(","))
                            .map(s -> Integer.parseInt(s.trim()))
                            .collect(Collectors.toList());
                    ids.add(alertPluginInstance.getId());
                    globalAlertGroup.setAlertInstanceIds(StringUtils.join(ids, ","));
                }
                alertGroupMapper.updateById(globalAlertGroup);
            }
            return alertPluginInstance;
        }
        throw new ServiceException(Status.SAVE_ERROR);
    }

    /**
     * update alert plugin instance
     *
     * @param loginUser            login user
     * @param pluginInstanceId     plugin instance id
     * @param instanceName         instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public AlertPluginInstance updateById(User loginUser, int pluginInstanceId, String instanceName,
                                          WarningType warningType, String pluginInstanceParams) {

        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ALERT_PLUGIN_INSTANCE, ALERT_PLUGIN_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        AlertPluginInstance alertPluginInstance =
                new AlertPluginInstance(pluginInstanceId, paramsMapJson, instanceName, warningType, new Date());

        int i = alertPluginInstanceMapper.updateById(alertPluginInstance);

        if (i > 0) {
            log.info("Update alert plugin instance complete, instanceId:{}, name:{}", alertPluginInstance.getId(),
                    alertPluginInstance.getInstanceName());
            return alertPluginInstance;
        }
        throw new ServiceException(Status.SAVE_ERROR);
    }

    /**
     * delete alert plugin instance
     *
     * @param loginUser             login user
     * @param alertPluginInstanceId id
     * @return result
     */
    @Override
    @Transactional
    public void deleteById(User loginUser, int alertPluginInstanceId) {
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ALERT_PLUGIN_INSTANCE, ALERT_PLUGIN_DELETE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        AlertPluginInstance alertPluginInstance = alertPluginInstanceMapper.selectById(alertPluginInstanceId);

        if (alertPluginInstance.getInstanceType() == AlertPluginInstanceType.GLOBAL) {
            // global instance will be removed from global alert group automatically
            AlertGroup globalAlertGroup = alertGroupMapper.selectById(GLOBAL_ALERT_GROUP_ID);
            List<Integer> ids = Arrays.stream(globalAlertGroup.getAlertInstanceIds().split(","))
                    .map(s -> Integer.parseInt(s.trim()))
                    .collect(Collectors.toList());
            ids = ids.stream().filter(x -> x != alertPluginInstanceId).collect(Collectors.toList());
            globalAlertGroup.setAlertInstanceIds(StringUtils.join(ids, ","));
            alertGroupMapper.updateById(globalAlertGroup);
            log.info("Remove global alert plugin instance from global alert group automatically, name:{}",
                    alertPluginInstance.getInstanceName());
        } else {
            // check if there is an associated alert group
            boolean hasAssociatedAlertGroup = checkHasAssociatedAlertGroup(String.valueOf(alertPluginInstanceId));
            if (hasAssociatedAlertGroup) {
                throw new ServiceException(Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED);
            }
        }

        alertPluginInstanceMapper.deleteById(alertPluginInstanceId);
    }

    /**
     * get alert plugin instance
     *
     * @param loginUser login user
     * @param id get id
     * @return alert plugin
     */
    @Override
    public AlertPluginInstance getById(User loginUser, int id) {
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ALERT_PLUGIN_INSTANCE,
                ApiFuncIdentificationConstant.ALARM_INSTANCE_MANAGE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        return alertPluginInstanceMapper.selectById(id);
    }

    @Override
    public List<AlertPluginInstanceVO> queryAll() {
        List<AlertPluginInstance> alertPluginInstances = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        return buildPluginInstanceVOList(alertPluginInstances);
    }

    @Override
    public boolean checkExistPluginInstanceName(String pluginInstanceName) {
        return alertPluginInstanceMapper.existInstanceName(pluginInstanceName) == Boolean.TRUE;
    }

    @Override
    public PageInfo<AlertPluginInstanceVO> listPaging(User loginUser, String searchVal, int pageNo, int pageSize) {

        IPage<AlertPluginInstance> alertPluginInstanceIPage =
                alertPluginInstanceMapper.queryByInstanceNamePage(new Page<>(pageNo, pageSize), searchVal);

        PageInfo<AlertPluginInstanceVO> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) alertPluginInstanceIPage.getTotal());
        pageInfo.setTotalList(buildPluginInstanceVOList(alertPluginInstanceIPage.getRecords()));
        return pageInfo;
    }

    private List<AlertPluginInstanceVO> buildPluginInstanceVOList(List<AlertPluginInstance> alertPluginInstances) {
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(alertPluginInstances)) {
            return alertPluginInstanceVOS;
        }
        List<PluginDefine> pluginDefineList = pluginDefineMapper.queryAllPluginDefineList();
        if (CollectionUtils.isEmpty(pluginDefineList)) {
            return alertPluginInstanceVOS;
        }
        Map<Integer, PluginDefine> pluginDefineMap =
                pluginDefineList.stream().collect(Collectors.toMap(PluginDefine::getId, Function.identity()));
        alertPluginInstances.forEach(alertPluginInstance -> {
            AlertPluginInstanceVO alertPluginInstanceVO = new AlertPluginInstanceVO();
            alertPluginInstanceVO.setCreateTime(alertPluginInstance.getCreateTime());
            alertPluginInstanceVO.setUpdateTime(alertPluginInstance.getUpdateTime());
            alertPluginInstanceVO.setPluginDefineId(alertPluginInstance.getPluginDefineId());
            alertPluginInstanceVO.setInstanceName(alertPluginInstance.getInstanceName());
            alertPluginInstanceVO.setId(alertPluginInstance.getId());
            alertPluginInstanceVO.setInstanceType(alertPluginInstance.getInstanceType().getDescp());
            if (alertPluginInstance.getWarningType() != null) {
                alertPluginInstanceVO.setWarningType(alertPluginInstance.getWarningType().getDescp().toUpperCase());
            }
            PluginDefine pluginDefine = pluginDefineMap.get(alertPluginInstance.getPluginDefineId());
            // FIXME When the user removes the plug-in, this will happen. At this time, maybe we should add a new field
            // to indicate that the plug-in has expired?
            if (null == pluginDefine) {
                return;
            }
            alertPluginInstanceVO.setAlertPluginName(pluginDefine.getPluginName());
            // todo List pages do not recommend returning this parameter
            String pluginParamsMapString = alertPluginInstance.getPluginInstanceParams();
            String uiPluginParams = parseToPluginUiParams(pluginParamsMapString, pluginDefine.getPluginParams());
            alertPluginInstanceVO.setPluginInstanceParams(uiPluginParams);
            alertPluginInstanceVOS.add(alertPluginInstanceVO);
        });
        return alertPluginInstanceVOS;

    }

    /**
     * Get the parameters actually needed by the plugin
     *
     * @param pluginParams Complete parameters(include ui)
     * @return k, v(json string)
     */
    private String parsePluginParamsMap(String pluginParams) {
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(pluginParams);
        return JSONUtils.toJsonString(paramsMap);
    }

    /**
     * parse To Plugin Ui Params
     *
     * @param pluginParamsMapString k-v data
     * @param pluginUiParams Complete parameters(include ui)
     * @return Complete parameters list(include ui)
     */
    private String parseToPluginUiParams(String pluginParamsMapString, String pluginUiParams) {
        List<Map<String, Object>> pluginParamsList =
                PluginParamsTransfer.generatePluginParams(pluginParamsMapString, pluginUiParams);
        return JSONUtils.toJsonString(pluginParamsList);
    }

    private boolean checkHasAssociatedAlertGroup(String id) {
        List<String> idsList = alertGroupMapper.queryInstanceIdsList();
        if (CollectionUtils.isEmpty(idsList)) {
            return false;
        }
        Optional<String> first =
                idsList.stream().filter(k -> null != k && Arrays.asList(k.split(",")).contains(id)).findFirst();
        return first.isPresent();
    }

    public Optional<Host> getAlertServerAddress() {
        List<Server> serverList = registryClient.getServerList(RegistryNodeType.ALERT_SERVER);
        if (CollectionUtils.isEmpty(serverList)) {
            return Optional.empty();
        }
        Server server = serverList.get(0);
        return Optional.of(new Host(server.getHost(), server.getPort()));
    }

    @Override
    public void testSend(int pluginDefineId, String pluginInstanceParams) {
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            throw new ServiceException(Status.ALERT_SERVER_NOT_EXIST);
        }

        Host alertServerAddress = alertServerAddressOptional.get();
        AlertTestSendRequest alertTestSendRequest = new AlertTestSendRequest(
                pluginDefineId,
                pluginInstanceParams);

        AlertSendResponse alertSendResponse;

        try {
            IAlertOperator alertOperator = SingletonJdkDynamicRpcClientProxyFactory
                    .getProxyClient(alertServerAddress.getAddress(), IAlertOperator.class);
            alertSendResponse = alertOperator.sendTestAlert(alertTestSendRequest);
            log.info("Send alert to: {} successfully, response: {}", alertServerAddress, alertSendResponse);
        } catch (Exception e) {
            log.error("Send alert: {} to: {} failed", alertTestSendRequest, alertServerAddress, e);
            throw new ServiceException(Status.ALERT_TEST_SENDING_FAILED, e.getMessage());
        }

        if (alertSendResponse.isSuccess()) {
            throw new ServiceException(Status.ALERT_TEST_SENDING_FAILED,
                    alertSendResponse.getResResults().get(0).getMessage());
        }
    }
}
