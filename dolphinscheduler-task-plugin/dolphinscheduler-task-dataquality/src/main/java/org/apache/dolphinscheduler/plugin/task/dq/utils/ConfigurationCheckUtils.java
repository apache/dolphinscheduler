package org.apache.dolphinscheduler.plugin.task.dq.utils;

import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.ERROR_OUTPUT_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SQL;

import org.apache.dolphinscheduler.plugin.task.api.parser.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.BaseConfig;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.DataQualityConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Slf4j
public class ConfigurationCheckUtils {

    private static final Set<String> NEED_CHECK_KEY_NAME = Sets.newHashSet(SQL);
    private static final Set<String> ALLOW_EMPTY_KEY_NAME = Sets.newHashSet(ERROR_OUTPUT_PATH);

    public static void checkMissingFormat(DataQualityConfiguration dataQualityConfiguration) {
        // ERROR_OUTPUT_PATH is base on resource center, if resource center not start up, data quality task will throw
        // exception
        Map<String, String> allowEmptyMap = Maps.newHashMap();
        for (String allowEmptyKey : ALLOW_EMPTY_KEY_NAME) {
            allowEmptyMap.put(allowEmptyKey, "");
        }

        List<BaseConfig> transformerConfigs = dataQualityConfiguration.getTransformerConfigs();
        List<BaseConfig> writerConfigs = dataQualityConfiguration.getWriterConfigs();

        for (BaseConfig transformerConfig : transformerConfigs) {
            Map<String, Object> config = transformerConfig.getConfig();
            config.forEach((k, v) -> {
                if (NEED_CHECK_KEY_NAME.contains(k)) {
                    PlaceholderUtils.replacePlaceholders(v.toString(), allowEmptyMap, false);
                }
            });
        }

        for (BaseConfig writerConfig : writerConfigs) {
            Map<String, Object> config = writerConfig.getConfig();
            config.forEach((k, v) -> {
                if (NEED_CHECK_KEY_NAME.contains(k)) {
                    PlaceholderUtils.replacePlaceholders(v.toString(), allowEmptyMap, false);
                }
            });
        }
    }
}
