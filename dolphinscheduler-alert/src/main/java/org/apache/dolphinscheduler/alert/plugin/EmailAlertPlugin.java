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
package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.alert.manager.EmailManager;
import org.apache.dolphinscheduler.alert.manager.EnterpriseWeChatManager;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.EnterpriseWeChatUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertData;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.PluginName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * EmailAlertPlugin
 *
 * This plugin is a default plugin, and mix up email and enterprise wechat, because adapt with former alert behavior
 */
public class EmailAlertPlugin implements AlertPlugin {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertPlugin.class);

    private PluginName pluginName;

    private static final EmailManager emailManager = new EmailManager();
    private static final EnterpriseWeChatManager weChatManager = new EnterpriseWeChatManager();

    public EmailAlertPlugin() {
        this.pluginName = new PluginName();
        this.pluginName.setEnglish(Constants.PLUGIN_DEFAULT_EMAIL_EN);
        this.pluginName.setChinese(Constants.PLUGIN_DEFAULT_EMAIL_CH);
    }

    @Override
    public String getId() {
        return Constants.PLUGIN_DEFAULT_EMAIL;
    }

    @Override
    public PluginName getName() {
        return pluginName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> process(AlertInfo info) {
        Map<String, Object> retMaps = new HashMap<>();

        AlertData alert = info.getAlertData();

        List<String> receviersList = (List<String>) info.getProp(Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERS);

        // receiving group list
        // custom receiver
        String receivers = alert.getReceivers();
        if (StringUtils.isNotEmpty(receivers)) {
            String[] splits = receivers.split(",");
            receviersList.addAll(Arrays.asList(splits));
        }

        List<String> receviersCcList = new ArrayList<>();
        // Custom Copier
        String receiversCc = alert.getReceiversCc();
        if (StringUtils.isNotEmpty(receiversCc)) {
            String[] splits = receiversCc.split(",");
            receviersCcList.addAll(Arrays.asList(splits));
        }

        if (CollectionUtils.isEmpty(receviersList) && CollectionUtils.isEmpty(receviersCcList)) {
            logger.warn("alert send error : At least one receiver address required");
            retMaps.put(Constants.STATUS, "false");
            retMaps.put(Constants.MESSAGE, "execution failure,At least one receiver address required.");
            return retMaps;
        }

        retMaps = emailManager.send(receviersList, receviersCcList, alert.getTitle(), alert.getContent(),
                alert.getShowType());

        //send flag
        boolean flag = false;

        if (retMaps == null) {
            retMaps = new HashMap<>();
            retMaps.put(Constants.MESSAGE, "alert send error.");
            retMaps.put(Constants.STATUS, "false");
            logger.info("alert send error : {}", retMaps.get(Constants.MESSAGE));
            return retMaps;
        }

        flag = Boolean.parseBoolean(String.valueOf(retMaps.get(Constants.STATUS)));

        if (flag) {
            logger.info("alert send success");
            retMaps.put(Constants.MESSAGE, "email send success.");
            if (EnterpriseWeChatUtils.isEnable()) {
                logger.info("Enterprise WeChat is enable!");
                try {
                    String token = EnterpriseWeChatUtils.getToken();
                    weChatManager.send(info, token);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

        } else {
            retMaps.put(Constants.MESSAGE, "alert send error.");
            logger.info("alert send error : {}", retMaps.get(Constants.MESSAGE));
        }

        return retMaps;
    }

}
