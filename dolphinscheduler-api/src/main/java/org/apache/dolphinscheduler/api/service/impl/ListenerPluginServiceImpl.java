package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.rpc.ApiRpcClient;
import org.apache.dolphinscheduler.api.service.ListenerPluginService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.ListenerInstanceVO;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.ListenerPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.remote.command.listener.*;
import org.apache.dolphinscheduler.remote.utils.Host;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
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

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Autowired
    private ListenerPluginInstanceMapper listenerPluginInstanceMapper;

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
            RegisterListenerPluginRequest request = new RegisterListenerPluginRequest(StringUtils.replace(file.getOriginalFilename(), ".jar",""), classPath, file.getBytes());
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
            UpdateListenerPluginRequest request = new UpdateListenerPluginRequest(id, StringUtils.replace(file.getOriginalFilename(), ".jar",""), classPath, file.getBytes());
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
    public Result listPluginPaging(String searchVal, Integer pageNo, Integer pageSize){
        Result result = new Result();
        PageInfo<PluginDefine> pageInfo = new PageInfo<>(pageNo, pageSize);
        LambdaQueryWrapper<PluginDefine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginDefine::getPluginType, PluginType.LISTENER.getDesc());
        if (StringUtils.isNotEmpty(searchVal)){
            queryWrapper.like(PluginDefine::getPluginName, searchVal);
        }
        queryWrapper.orderByDesc(PluginDefine::getUpdateTime);
        Page<PluginDefine> page = new Page<>(pageNo, pageSize);
        Page<PluginDefine> plugins = pluginDefineMapper.selectPage(page, queryWrapper);
        pageInfo.setTotal((int) plugins.getTotal());
        pageInfo.setTotalList(plugins.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result listPluginList(){
        LambdaQueryWrapper<PluginDefine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginDefine::getPluginType, PluginType.LISTENER.getDesc());
        queryWrapper.orderByDesc(PluginDefine::getUpdateTime);
        List<PluginDefine> plugins = pluginDefineMapper.selectList(queryWrapper);
        return Result.success(plugins);
    }

    @Override
    public Result createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                         List<ListenerEventType> listenerEventTypes) {
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
                                         List<ListenerEventType> listenerEventTypes) {
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

    @Override
    public Result listInstancePaging(String searchVal, Integer pageNo, Integer pageSize){
        Result result = new Result();
        PageInfo<ListenerInstanceVO> pageInfo = new PageInfo<>(pageNo, pageSize);
        LambdaQueryWrapper<ListenerPluginInstance> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(searchVal)){
            queryWrapper.like(ListenerPluginInstance::getInstanceName, searchVal);
        }
        queryWrapper.orderByDesc(ListenerPluginInstance::getUpdateTime);
        Page<ListenerPluginInstance> page = new Page<>(pageNo, pageSize);
        Page<ListenerPluginInstance> instances = listenerPluginInstanceMapper.selectPage(page, queryWrapper);
        pageInfo.setTotal((int) instances.getTotal());
        pageInfo.setTotalList(buildListenerInstanceVoList(instances.getRecords()));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public boolean checkExistPluginInstanceName(String instanceName){
        return listenerPluginInstanceMapper.existInstanceName(instanceName) == Boolean.TRUE;
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

    private List<ListenerInstanceVO> buildListenerInstanceVoList(List<ListenerPluginInstance> listenerPluginInstances){
        List<ListenerInstanceVO> listenerInstanceVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(listenerPluginInstances)){
            return listenerInstanceVOS;
        }
        LambdaQueryWrapper<PluginDefine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PluginDefine::getPluginType, PluginType.LISTENER.getDesc());
        List<PluginDefine> listenerPlugins = pluginDefineMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(listenerPlugins)){
            return listenerInstanceVOS;
        }
        Map<Integer, PluginDefine> pluginDefineMap =
                listenerPlugins.stream().collect(Collectors.toMap(PluginDefine::getId, Function.identity()));
        listenerPluginInstances.forEach(listenerPluginInstance -> {
            ListenerInstanceVO listenerInstanceVO = new ListenerInstanceVO();
            listenerInstanceVO.setId(listenerPluginInstance.getId());
            listenerInstanceVO.setPluginDefineId(listenerPluginInstance.getPluginDefineId());
            PluginDefine pluginDefine = pluginDefineMap.get(listenerPluginInstance.getPluginDefineId());
            if (Objects.isNull(pluginDefine)){
                return;
            }
            listenerInstanceVO.setListenerPluginName(pluginDefine.getPluginName());
            listenerInstanceVO.setInstanceName(listenerPluginInstance.getInstanceName());
            listenerInstanceVO.setPluginInstanceParams(parseToPluginUiParams(listenerPluginInstance.getPluginInstanceParams(), pluginDefine.getPluginParams()));
            List<String> eventTypes =
                    Arrays.stream(listenerPluginInstance.getListenerEventTypes().split(",")).map(eventTypeCode -> ListenerEventType.of(Integer.parseInt(eventTypeCode)).getDescp()).collect(Collectors.toList());
            listenerInstanceVO.setListenerEventTypes(eventTypes);
            listenerInstanceVO.setCreateTime(listenerPluginInstance.getCreateTime());
            listenerInstanceVO.setUpdateTime(listenerPluginInstance.getUpdateTime());
            listenerInstanceVOS.add(listenerInstanceVO);
        });
        return listenerInstanceVOS;
    }

    private String parseToPluginUiParams(String pluginParamsMapString, String pluginUiParams) {
        List<Map<String, Object>> pluginParamsList =
                PluginParamsTransfer.generatePluginParams(pluginParamsMapString, pluginUiParams);
        return JSONUtils.toJsonString(pluginParamsList);
    }
}
