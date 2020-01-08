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
package org.apache.dolphinscheduler.alert.sender.impl;

import org.apache.dolphinscheduler.alert.AlertException;
import org.apache.dolphinscheduler.alert.sender.Sender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Email Sender
 */
public class EmailSender implements Sender {
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    @Override
    public boolean send(Alert alert) throws AlertException {
        List<String> receiverList = new ArrayList<>();
        // custom receiver
        String receivers = alert.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(",");
            receiverList.addAll(Arrays.asList(splits));
        }

        // copy list
        List<String> receiverCcList = new ArrayList<>();
        // Custom Copier
        String receiversCc = alert.getReceiversCc();

        if (StringUtils.isNotEmpty(receiversCc)) {
            String[] splits = receiversCc.split(",");
            receiverCcList.addAll(Arrays.asList(splits));
        }

        if (receiverList.isEmpty() && receiverCcList.isEmpty()) {
            return false;
        }

        Map<String, Object> retMap = MailUtils.sendMails(receiverList, receiverCcList, alert.getTitle(),
                                                         alert.getContent(), alert.getShowType());
        boolean flag = Boolean.parseBoolean(String.valueOf(retMap.get(Constants.STATUS)));
        if (!flag && retMap.containsKey(Constants.MESSAGE)) {
            throw new AlertException((String) retMap.get(Constants.MESSAGE), alert);
        }
        return flag;
    }

    @Override
    public boolean sendGroup(Alert alert) throws AlertException {
        List<User> users = alertDao.listUserByAlertgroupId(alert.getAlertGroupId());
        if (users == null) {
            return false;
        }
        // receiving group list
        List<String> receiverList = new ArrayList<>();
        for (User user : users) {
            receiverList.add(user.getEmail());
        }

        if (receiverList.isEmpty()) {
            return false;
        }

        Map<String, Object> retMap = MailUtils.sendMails(receiverList, alert.getTitle(), alert.getContent(),
                                                         alert.getShowType());
        boolean flag = Boolean.parseBoolean(String.valueOf(retMap.get(Constants.STATUS)));
        if (!flag && retMap.containsKey(Constants.MESSAGE)) {
            throw new AlertException((String) retMap.get(Constants.MESSAGE), alert);
        }
        return flag;
    }

    @Override
    public String getName() {
        return "EMAIL";
    }
}
