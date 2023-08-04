package org.apache.dolphinscheduler.listener.service;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostServiceStatus;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.listener.util.ClassLoaderUtil;
import org.apache.dolphinscheduler.remote.command.listener.ListenerResponse;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * @author wxn
 * @date 2023/5/14
 */
@Component
@Slf4j
public class ListenerPluginService implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private ApplicationContext applicationContext;

    @Resource
    private PluginDefineMapper pluginDefineMapper;

    @Resource
    private ListenerPluginInstanceMapper pluginInstanceMapper;

    @Resource
    private ListenerEventMapper listenerEventMapper;

    @Resource
    private ClassLoaderUtil classLoaderUtil;

    // TODO: 先写死
    private final String path = "/root/program/dolphinscheduler/test-plugins/plugin/";

    private final ConcurrentHashMap<Integer, ListenerPlugin> listenerPlugins = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Integer, ListenerInstancePostService> listenerInstancePostServices =
            new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ConfigurableApplicationContext configurableApplicationContext =
                (ConfigurableApplicationContext) applicationContext;
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        LambdaQueryWrapper<PluginDefine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginDefine::getPluginType, "listener");
        List<PluginDefine> pluginDefines = pluginDefineMapper.selectList(wrapper);
        log.info("init listener plugins");
        for (PluginDefine pluginDefine : pluginDefines) {
            try {
                ListenerPlugin plugin =
                        getListenerPluginFromJar(pluginDefine.getPluginLocation(), pluginDefine.getPluginClassName());
                listenerPlugins.put(pluginDefine.getId(), plugin);
                log.info("init listener plugin {}", pluginDefine.getPluginName());
            } catch (Exception e) {
                log.error("failed when init listener plugin {}", pluginDefine.getPluginName(), e);
            }
        }
        log.info("init listener instances");
        List<ListenerPluginInstance> pluginInstances = pluginInstanceMapper.selectList(new QueryWrapper<>());
        for (ListenerPluginInstance pluginInstance : pluginInstances) {
            int pluginId = pluginInstance.getPluginDefineId();
            if (!listenerPlugins.containsKey(pluginId)) {
                log.error("failed to init listener instance {} because listener plugin {} cannot be loaded",
                        pluginInstance.getInstanceName(), pluginId);
                continue;
            }
            ListenerInstancePostService listenerInstancePostService =
                    new ListenerInstancePostService(listenerPlugins.get(pluginId), pluginInstance, listenerEventMapper);
            listenerInstancePostService.start();
            listenerInstancePostServices.put(pluginInstance.getId(), listenerInstancePostService);
            log.info("init listener instance {}：", pluginInstance.getInstanceName());
        }
    }

    public ListenerResponse registerListenerPlugin(String originalFileName, String classPath, byte[] pluginJar) {
        String fileName = String.format("%s@%s.jar", originalFileName,UUID.randomUUID());
        String filePath = path + fileName;
        boolean success = true;
        try {
            File dest = new File(filePath);
            Files.write(dest.toPath(), pluginJar);
            ListenerPlugin plugin = getListenerPluginFromJar(filePath, classPath);
            PluginDefine pluginDefine = PluginDefine.builder()
                    .pluginName(plugin.name())
                    .pluginParams(JSONUtils.toJsonString(plugin.params()))
                    .pluginType("listener")
                    .pluginLocation(filePath)
                    .pluginClassName(classPath)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            pluginDefineMapper.insert(pluginDefine);
            listenerPlugins.put(pluginDefine.getId(), plugin);
        } catch (IOException e) {
            success = false;
            log.error(e.getMessage(), e);
            return ListenerResponse.fail("failed when upload jar：" + e.getMessage());
        } catch (ClassNotFoundException e) {
            success = false;
            log.error(e.getMessage(), e);
            return ListenerResponse.fail("cannot load class：" + e.getMessage());
        } catch (Exception e) {
            success = false;
            return ListenerResponse.fail("failed when register listener plugin：" + e.getMessage());
        }finally {
            if (!success){
                FileUtils.deleteFile(filePath);
            }
        }
        return ListenerResponse.success();
    }

    public ListenerResponse updateListenerPlugin(int id, String originalFileName, String classPath, byte[] pluginJar) {
        if (!listenerPlugins.containsKey(id)) {
            return ListenerResponse.fail(String.format("listener plugin %d not exist in concurrent hash map", id));
        }
        // 先把所有的实例都暂停
        LambdaQueryWrapper<ListenerPluginInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ListenerPluginInstance::getId)
                .eq(ListenerPluginInstance::getPluginDefineId, id);
        List<ListenerPluginInstance> instances = pluginInstanceMapper.selectList(wrapper);
        List<ListenerInstancePostService> services = new ArrayList<>();
        for (ListenerPluginInstance instance : instances) {
            if (listenerInstancePostServices.containsKey(instance.getId())) {
                services.add(listenerInstancePostServices.get(instance.getId()));
            }
        }
        services.forEach(x -> x.setServiceStatus(ListenerEventPostServiceStatus.PAUSE));
        PluginDefine plugin = pluginDefineMapper.selectById(id);

        try {
            // 先卸载旧的插件
            classLoaderUtil.removeJarFile(plugin.getPluginLocation());
            defaultListableBeanFactory.removeBeanDefinition(plugin.getPluginClassName());
            // 安装新的plugin
            String fileName = String.format("%s@%s.jar", originalFileName,UUID.randomUUID());
            String filePath = path + fileName;
            File dest = new File(filePath);
            Files.write(dest.toPath(), pluginJar);
            ListenerPlugin newPlugin = getListenerPluginFromJar(filePath, classPath);
            PluginDefine pluginDefine = PluginDefine.builder()
                    .id(id)
                    .pluginName(newPlugin.name())
                    .pluginParams(JSONUtils.toJsonString(newPlugin.params()))
                    .pluginType("listener")
                    .pluginLocation(filePath)
                    .pluginClassName(classPath)
                    .updateTime(new Date())
                    .build();
            pluginDefineMapper.updateById(pluginDefine);
            Files.delete(new File(plugin.getPluginLocation()).toPath());
            listenerPlugins.put(id, newPlugin);
            services.forEach(x -> {
                x.updateListenerPlugin(newPlugin);
                x.setServiceStatus(ListenerEventPostServiceStatus.RUN);
            });
            return ListenerResponse.success();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ListenerResponse.fail("failed when remove jar：" + e.getMessage());
        } catch (Exception e) {
            return ListenerResponse.fail("failed when register listener plugin：" + e.getMessage());
        }

    }

    public ListenerResponse removeListenerPlugin(int id) {
        if (!listenerPlugins.containsKey(id)) {
            return ListenerResponse.fail(String.format("listener plugin %d not exist in concurrent hash map", id));
        }
        PluginDefine plugin = pluginDefineMapper.selectById(id);
        if (Objects.isNull(plugin)) {
            return ListenerResponse.fail(String.format("listener plugin %d not exist in db", id));
        }
        LambdaQueryWrapper<ListenerPluginInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ListenerPluginInstance::getPluginDefineId, id);
        List<ListenerPluginInstance> pluginInstances = pluginInstanceMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(pluginInstances)) {
            return ListenerResponse
                    .fail(String.format("please remove listener instances of plugin %s first", plugin.getPluginName()));
        }
        try {
            classLoaderUtil.removeJarFile(plugin.getPluginLocation());
            defaultListableBeanFactory.removeBeanDefinition(plugin.getPluginClassName());
            Files.delete(new File(plugin.getPluginLocation()).toPath());
            pluginDefineMapper.deleteById(id);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ListenerResponse.fail("failed when remove jar：" + e.getMessage());
        } catch (Exception e) {
            return ListenerResponse.fail("failed when register listener plugin：" + e.getMessage());
        }
        return ListenerResponse.success();
    }

    @Transactional
    public ListenerResponse createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                                   List<ListenerEventType> listenerEventTypes) {
        if (!listenerPlugins.containsKey(pluginDefineId)) {
            return ListenerResponse.fail(
                    String.format("failed when register listener instance %s because listener plugin %d cannot loaded",
                            instanceName, pluginDefineId));
        }
        ListenerPluginInstance listenerPluginInstance = new ListenerPluginInstance();
        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        listenerPluginInstance.setInstanceName(instanceName);
        listenerPluginInstance.setPluginInstanceParams(paramsMapJson);
        listenerPluginInstance.setPluginDefineId(pluginDefineId);
        listenerPluginInstance.setListenerEventTypes(StringUtils.join(listenerEventTypes.stream().map(ListenerEventType::getCode).collect(Collectors.toSet()), ","));
        pluginInstanceMapper.insert(listenerPluginInstance);
        ListenerInstancePostService listenerInstancePostService = new ListenerInstancePostService(
                listenerPlugins.get(pluginDefineId), listenerPluginInstance, listenerEventMapper);
        listenerInstancePostService.start();
        listenerInstancePostServices.put(listenerPluginInstance.getId(), listenerInstancePostService);
        return ListenerResponse.success(listenerPluginInstance);
    }

    public ListenerResponse updateListenerInstance(int instanceId, String instanceName, String pluginInstanceParams,
                                                   List<ListenerEventType> listenerEventTypes) {
        if (!listenerInstancePostServices.containsKey(instanceId)) {
            return ListenerResponse.fail(String.format(
                    "failed when update listener instance %s because listener instance %d not exist in map",
                    instanceName, instanceId));
        }
        ListenerInstancePostService instancePostService = listenerInstancePostServices.get(instanceId);
        instancePostService.setServiceStatus(ListenerEventPostServiceStatus.PAUSE);
        ListenerPluginInstance listenerPluginInstance = new ListenerPluginInstance();
        listenerPluginInstance.setId(instanceId);
        listenerPluginInstance.setInstanceName(instanceName);
        listenerPluginInstance.setPluginInstanceParams(pluginInstanceParams);
        listenerPluginInstance.setListenerEventTypes(StringUtils.join(listenerEventTypes.stream().map(ListenerEventType::getCode).collect(Collectors.toSet()), ","));
        listenerPluginInstance.setUpdateTime(new Date());
        pluginInstanceMapper.updateById(listenerPluginInstance);
        instancePostService.updateListenerPluginInstance(listenerPluginInstance);
        instancePostService.setServiceStatus(ListenerEventPostServiceStatus.RUN);
        return ListenerResponse.success();
    }

    public ListenerResponse removeListenerInstance(int instanceId) {
        if (!listenerInstancePostServices.containsKey(instanceId)) {
            return ListenerResponse
                    .fail(String.format("listener instance service %d not exist in concurrent hash map", instanceId));
        }
        ListenerPluginInstance instance = pluginInstanceMapper.selectById(instanceId);
        if (Objects.isNull(instance)) {
            return ListenerResponse.fail(String.format("listener instance %d not exist in db", instanceId));
        }
        // 停止服务线程
        ListenerInstancePostService listenerInstancePostService = listenerInstancePostServices.get(instanceId);
        listenerInstancePostService.setServiceStatus(ListenerEventPostServiceStatus.STOP);
        listenerInstancePostServices.remove(instanceId);
        // 删除该监听实例
        pluginInstanceMapper.deleteById(instanceId);
        // 删除该监听实力所有的消息（可能有失败的，可能有删除监听示例时未来得及处理的）
        LambdaQueryWrapper<ListenerEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ListenerEvent::getPluginInstanceId, instanceId);
        listenerEventMapper.delete(wrapper);
        return ListenerResponse.success();
    }

    private String parsePluginParamsMap(String pluginParams) {
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(pluginParams);
        return JSONUtils.toJsonString(paramsMap);
    }

    private ListenerPlugin getListenerPluginFromJar(String filePath, String classPath) throws Exception {
        ClassLoader classLoader = classLoaderUtil.getClassLoader(filePath);
        Class<?> clazz = null;
        clazz = classLoader.loadClass(classPath);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        defaultListableBeanFactory.registerBeanDefinition(clazz.getName(),
                beanDefinitionBuilder.getRawBeanDefinition());
        return (ListenerPlugin) applicationContext.getBean(clazz.getName());
    }

}
