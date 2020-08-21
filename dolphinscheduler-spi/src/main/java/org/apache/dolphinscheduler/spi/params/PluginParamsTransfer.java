package org.apache.dolphinscheduler.spi.params;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.List;

/**
 * plugin params pojo and json transfer tool
 */
public class PluginParamsTransfer {

    public static String transferParamsToJson(List<PluginParams> list) {
        return JSONUtils.toJsonString(list);
    }

    public static List<PluginParams> transferJsonToParamsList(String str) {
        return JSONUtils.toList(str, PluginParams.class);
    }
}
