package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxn
 * @date 2023/8/1
 */
public interface ListenerPluginService {

    Result registerListenerPlugin(MultipartFile file, String classPath);

    Result updateListenerPlugin(int id, MultipartFile file, String classPath);

    Result removeListenerPlugin(int id);

    Result createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                  List<Integer> listenerEventTypes);

    Result updateListenerInstance(int instanceId, String instanceName, String pluginInstanceParams,
                                  List<Integer> listenerEventType);

    Result removeListenerInstance(int id);
}
