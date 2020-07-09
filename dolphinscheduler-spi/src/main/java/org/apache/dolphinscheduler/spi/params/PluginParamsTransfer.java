package org.apache.dolphinscheduler.spi.params;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * alpacajs json and PluginParams java bean transfer
 */
public class PluginParamsTransfer {

    /**
     * transfe plugin params to form-create json
     * @param pluginParamsList
     *   pluginParamsList
     * @return
     */
    public static String getParamsJson(List<PluginParams> pluginParamsList) {
        return JSONUtils.toJsonString(pluginParamsList);
    }

    /**
     * transfe plugin params json string to PluginParams
     * @param paramsJson
     * @return
     */
    public static List<PluginParams> getPluginParams(String paramsJson) {
        return JSONUtils.toList(paramsJson, PluginParams.class);
    }

    /**
     * get the name and value of the plugin params
     * @param paramsJson
     * @return
     */
    public static Map<String, String> getPluginParamsMap(String paramsJson) {
        List<PluginParams> pluginParams = JSONUtils.toList(paramsJson, PluginParams.class);
        Map<String, String> paramsMap = new HashMap<>();
        for(PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue().toString());
        }

        return paramsMap;
    }

}
