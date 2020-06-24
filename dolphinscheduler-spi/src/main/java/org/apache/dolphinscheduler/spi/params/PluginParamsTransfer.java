package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.List;

/**
 * alpacajs json and PluginParams java bean transfer
 */
public class PluginParamsTransfer {

    public static final String ALPACAJS_JSON_DATA_NODE = "data";

    public static final String ALPACAJS_JSON_SCHEMA_NODE = "schema";

    public static final String ALPACAJS_JSON_OPTIONS_NODE = "options";

    public static final String ALPACAJS_JSON_TYPE_NODE = "type";

    public static final String ALPACAJS_JSON_OPTIONS_FIELDS_NODE = "fields";

    public static final String ALPACAJS_JSON_SCHEMA_PROPERTIES_NODE = "properties";


    /**
     * transfe plugin params to alpacajs json
     * @param pluginParamsList
     *   pluginParamsList
     * @return
     */
    public static String getAlpacajsJson(List<AbsPluginParams> pluginParamsList) {
        JsonNode jsonNode = JSONUtils.createObjectNode();
        jsonNode = ((ObjectNode) jsonNode).set(PluginParamsTransfer.ALPACAJS_JSON_DATA_NODE, JSONUtils.createObjectNode());
        ((ObjectNode) jsonNode).set(PluginParamsTransfer.ALPACAJS_JSON_SCHEMA_NODE, JSONUtils.createObjectNode());
        ((ObjectNode) jsonNode).set(PluginParamsTransfer.ALPACAJS_JSON_OPTIONS_NODE, JSONUtils.createObjectNode());

        ((ObjectNode)jsonNode.get(PluginParamsTransfer.ALPACAJS_JSON_SCHEMA_NODE))
                .put(PluginParamsTransfer.ALPACAJS_JSON_TYPE_NODE, "object")
                .set(PluginParamsTransfer.ALPACAJS_JSON_SCHEMA_PROPERTIES_NODE, JSONUtils.createObjectNode());

        ((ObjectNode)jsonNode.get(PluginParamsTransfer.ALPACAJS_JSON_OPTIONS_NODE))
                .set(PluginParamsTransfer.ALPACAJS_JSON_OPTIONS_FIELDS_NODE, JSONUtils.createObjectNode());

        for(AbsPluginParams pluginParams : pluginParamsList) {
            AlpacajsSchema alpacajsSchema = pluginParams.getAlpacajsSchema();
            AlpacajsOptions alpacajsOptions = pluginParams.getAlpacajsOptions();

            ((ObjectNode)jsonNode.get(PluginParamsTransfer.ALPACAJS_JSON_DATA_NODE))
                    .put(pluginParams.getName(), alpacajsSchema.getDefaultValue());

            //schema
            ((ObjectNode) jsonNode.findValue(PluginParamsTransfer.ALPACAJS_JSON_SCHEMA_PROPERTIES_NODE))
                    .set(pluginParams.getName(), JSONUtils.toJsonNode(alpacajsSchema));

            //options
            ((ObjectNode) jsonNode.findValue(PluginParamsTransfer.ALPACAJS_JSON_OPTIONS_FIELDS_NODE))
                    .set(pluginParams.getName(), JSONUtils.toJsonNode(alpacajsOptions));

        }

        return JSONUtils.toJsonString(jsonNode);
    }


}
