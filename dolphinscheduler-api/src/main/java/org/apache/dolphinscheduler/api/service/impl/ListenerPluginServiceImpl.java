package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.rpc.ApiRpcClient;
import org.apache.dolphinscheduler.api.service.ListenerPluginService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.remote.command.listener.*;
import org.apache.dolphinscheduler.remote.utils.Host;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxn
 * @date 2023/8/1
 */
@Service
@Slf4j
public class ListenerPluginServiceImpl extends BaseServiceImpl implements ListenerPluginService {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private ApiRpcClient apiRpcClient;

    @Override
    public Result registerListenerPlugin(MultipartFile file, String classPath) {
        if (!checkPluginJar(file)) {
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, "plugin jar is empty or has wrong type");
        }
        if (StringUtils.isEmpty(classPath)) {
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, "empty class path");
        }
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            RegisterListenerPluginRequest request = new RegisterListenerPluginRequest(classPath, file.getBytes());
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }

    }

    @Override
    public Result updateListenerPlugin(int id, MultipartFile file, String classPath) {
        if (!checkPluginJar(file)) {
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, "plugin jar is empty or has wrong type");
        }
        if (StringUtils.isEmpty(classPath)) {
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, "empty class path");
        }
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            UpdateListenerPluginRequest request = new UpdateListenerPluginRequest(id, classPath, file.getBytes());
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
    }

    @Override
    public Result removeListenerPlugin(int id) {
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            RemoveListenerPluginRequest request = new RemoveListenerPluginRequest(id);
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
    }

    @Override
    public Result createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                         List<Integer> listenerEventTypes) {
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            CreateListenerPluginInstanceRequest request =
                    new CreateListenerPluginInstanceRequest(pluginDefineId, instanceName, pluginInstanceParams, listenerEventTypes);
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
    }

    @Override
    public Result updateListenerInstance(int instanceId, String instanceName, String pluginInstanceParams,
                                         List<Integer> listenerEventTypes) {
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            UpdateListenerPluginInstanceRequest request =
                    new UpdateListenerPluginInstanceRequest(instanceId, instanceName, pluginInstanceParams, listenerEventTypes);
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
    }

    @Override
    public Result removeListenerInstance(int id) {
        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return Result.errorWithArgs(Status.ALERT_NOT_EXISTS);
        }
        Host alertServerAddress = alertServerAddressOptional.get();
        try {
            RemoveListenerPluginInstanceRequest request = new RemoveListenerPluginInstanceRequest(id);
            ListenerResponse response =
                    apiRpcClient.sendListenerMessageSync(alertServerAddress, request.convert2Command());
            if (response.isSuccess()) {
                return Result.success();
            } else {
                return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, response.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, e.getMessage());
        }
    }

    public Optional<Host> getAlertServerAddress() {
        List<Server> serverList = registryClient.getServerList(RegistryNodeType.ALERT_SERVER);
        if (CollectionUtils.isEmpty(serverList)) {
            return Optional.empty();
        }
        Server server = serverList.get(0);
        return Optional.of(new Host(server.getHost(), server.getPort()));
    }

    private boolean checkPluginJar(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        if (StringUtils.isEmpty(fileName) || fileSize == 0 || file.isEmpty() || !fileName.endsWith(".jar")) {
            return false;
        }
        return true;
    }
}
