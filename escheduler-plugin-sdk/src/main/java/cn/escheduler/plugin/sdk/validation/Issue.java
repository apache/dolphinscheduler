/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.validation;

import cn.escheduler.plugin.api.ErrorCode;
import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.LocalizableString;
import cn.escheduler.plugin.api.impl.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Issue implements Serializable, ValidationIssue {
    private final String instanceName;
    private final String serviceName;
    private final String configGroup;
    private final String configName;
    private long count;
    private final LocalizableString message;
    private Map<String, Object> additionalInfo;

    protected Issue(String instanceName, String serviceName, String configGroup, String configName, ErrorCode error, Object... args) {
        this.instanceName = instanceName;
        this.serviceName = serviceName;
        this.configGroup = configGroup;
        this.configName = configName;
        this.count = 1;
        message = new ErrorMessage(error, args);
    }

    public void setAdditionalInfo(String key, Object value) {
        if (additionalInfo == null) {
            additionalInfo = new HashMap<>();
        }
        additionalInfo.put(key, value);
    }

    @Override
    public Map getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String getMessage() {
        return message.getLocalized();
    }

    @Override
    public String getErrorCode() {
        return ((ErrorMessage)message).getErrorCode();
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getLevel() {
        String level;
        if (instanceName == null) {
            level = (getConfigName() == null) ? "PIPELINE" : "PIPELINE_CONFIG";
        } else {
            level = (getConfigName() == null) ? "STAGE" : "STAGE_CONFIG";
        }
        return level;
    }

    @Override
    public String getConfigGroup() {
        return configGroup;
    }

    @Override
    public String getConfigName() {
        return configName;
    }

    @Override
    public String toString() {
        return Utils.format("Issue[instance='{}' service='{}' group='{}' config='{}' message='{}']",
                instanceName,
                serviceName,
                configGroup,
                configName,
                message.getNonLocalized()
        );
    }

    public long getCount() {
        return count;
    }

    public void incCount() {
        count++;
    }

}
