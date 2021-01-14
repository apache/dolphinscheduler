/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.UiPluginService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UiPluginServiceImpl
 */
@Service
public class UiPluginServiceImpl extends BaseService implements UiPluginService {

    @Autowired
    PluginDefineMapper pluginDefineMapper;

    private static final String LANGUAGE_REGEX="\"([^\"]*)\"";

    private static final String LANGUAGE_SYMBOL="$t";

    private static final String ESCAPE_SYMBOL="\\";

    @Override
    public Map<String, Object> queryUiPluginsByType(PluginType pluginType) {
        Map<String, Object> result = new HashMap<>();
        if (!pluginType.getHasUi()) {
            putMsg(result, Status.PLUGIN_NOT_A_UI_COMPONENT);
            return result;
        }
        List<PluginDefine> pluginDefines = pluginDefineMapper.queryByPluginType(pluginType.getDesc());

        if (CollectionUtils.isEmpty(pluginDefines)) {
            putMsg(result, Status.QUERY_PLUGINS_RESULT_IS_NULL);
            return result;
        }
       // pluginDefines=buildPluginParams(pluginDefines);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pluginDefines);
        return result;
    }

    @Override
    public Map<String, Object> queryUiPluginDetailById(int id) {
        Map<String, Object> result = new HashMap<>();
        PluginDefine pluginDefine = pluginDefineMapper.queryDetailById(id);
        if (null == pluginDefine) {
            putMsg(result, Status.QUERY_PLUGIN_DETAIL_RESULT_IS_NULL);
            return result;
        }
       // String params=pluginDefine.getPluginParams();
       // pluginDefine.setPluginParams(parseParams(params));
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pluginDefine);
        return result;
    }
/*

    private List<PluginDefine> buildPluginParams(List<PluginDefine> pluginDefines){
        List<PluginDefine> newPluginDefines=new ArrayList<>(pluginDefines.size());
        pluginDefines.forEach(pluginDefine -> {
            PluginDefine newPluginDefine=pluginDefine;
            newPluginDefine.setPluginParams(parseParams(pluginDefine.getPluginParams()));
            newPluginDefines.add(newPluginDefine);
        });
        return newPluginDefines;
    }*/
/*
    private static String parseParams(String param){
        Pattern pattern = Pattern.compile(LANGUAGE_REGEX);

        StringBuffer newValue = new StringBuffer(param.length());

        Matcher matcher = pattern.matcher(param);

        while (matcher.find()) {
            String key = matcher.group(1);
            if(key.contains(LANGUAGE_SYMBOL)) {
                matcher.appendReplacement(newValue, ESCAPE_SYMBOL+key);
            }
        }

        matcher.appendTail(newValue);
        return newValue.toString();
    }

    public static void main(String[] args) {
        String msg="[{\"field\":\"receivers\",\"props\":{\"placeholder\":\"please input receives\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t('receivers')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"receiverCcs\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t('receiverCcs')\",\"value\":null,\"validate\":null},{\"field\":\"mailServerHost\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.smtp.host\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"mailServerPort\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.smtp.port\",\"value\":25,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"number\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"mailSender\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.sender\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"enableSmtpAuth\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"radio\",\"title\":\"mail.smtp.auth\",\"value\":true,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"mailUser\",\"props\":{\"placeholder\":\"if enable use authentication, you need input user\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.user\",\"value\":null,\"validate\":null},{\"field\":\"mailPasswd\",\"props\":{\"placeholder\":\"if enable use authentication, you need input password\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.passwd\",\"value\":null,\"validate\":null},{\"field\":\"starttlsEnable\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"radio\",\"title\":\"mail.smtp.starttls.enable\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"sslEnable\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"radio\",\"title\":\"mail.smtp.ssl.enable\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"mailSmtpSslTrust\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"input\",\"title\":\"mail.smtp.ssl.trust\",\"value\":\"*\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"show_type\",\"props\":{\"placeholder\":null,\"size\":\"small\"},\"type\":\"radio\",\"title\":\"show_type\",\"value\":\"table\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"table\",\"value\":\"table\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},{\"label\":\"attachment\",\"value\":\"attachment\",\"disabled\":false},{\"label\":\"table attachment\",\"value\":\"table attachment\",\"disabled\":false}]}]";
        System.out.println(parseParams(msg));
    }*/

}
