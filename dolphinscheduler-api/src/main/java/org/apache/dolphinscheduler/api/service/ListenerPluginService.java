package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;

import java.util.List;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxn
 * @date 2023/8/1
 */
public interface ListenerPluginService {

    Result registerListenerPlugin(MultipartFile file, String classPath);

    Result updateListenerPlugin(int id, MultipartFile file, String classPath);

    Result removeListenerPlugin(int id);

    Result listPluginPaging(String searchVal, Integer pageNo, Integer pageSize);

    Result listPluginList();

    Result createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                  List<ListenerEventType> listenerEventTypes);

    Result updateListenerInstance(int instanceId, String instanceName, String pluginInstanceParams,
                                  List<ListenerEventType> listenerEventType);

    Result removeListenerInstance(int id);

    Result listInstancePaging(String searchVal, Integer pageNo, Integer pageSize);

    boolean checkExistPluginInstanceName(String instanceName);
}
