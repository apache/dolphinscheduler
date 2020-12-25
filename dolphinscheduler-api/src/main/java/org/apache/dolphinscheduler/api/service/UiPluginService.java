package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.common.enums.PluginType;

import java.util.Map;

/**
 * UiPluginService
 */
public interface UiPluginService {

    Map<String, Object> queryUiPluginsByType(PluginType pluginType);
}
