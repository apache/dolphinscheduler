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
package cn.escheduler.alert.runner;

import cn.escheduler.alert.manager.EmailManager;
import cn.escheduler.alert.manager.EnterpriseWeChatManager;
import cn.escheduler.alert.utils.Constants;
import cn.escheduler.alert.utils.EnterpriseWeChatUtils;
import cn.escheduler.common.enums.AlertStatus;
import cn.escheduler.common.enums.AlertType;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.model.Alert;
import cn.escheduler.dao.model.User;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * alert sender
 */
public class AlertSender{

    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private static final EmailManager emailManager= new EmailManager();
    private static final EnterpriseWeChatManager weChatManager= new EnterpriseWeChatManager();


    private List<Alert> alertList;
    private AlertDao alertDao;

    public AlertSender(){}
    public AlertSender(List<Alert> alertList, AlertDao alertDao){
        super();
        this.alertList = alertList;
        this.alertDao = alertDao;
    }

    public void run() {

        List<User> users;

        Map<String, Object> retMaps = null;
        for(Alert alert:alertList){
            users = alertDao.listUserByAlertgroupId(alert.getAlertGroupId());



            // receiving group list
            List<String> receviersList = new ArrayList<String>();
            for(User user:users){
                receviersList.add(user.getEmail());
            }
            // custom receiver
            String receivers = alert.getReceivers();
            if (StringUtils.isNotEmpty(receivers)){
                String[] splits = receivers.split(",");
                for (String receiver : splits){
                    receviersList.add(receiver);
                }
            }

            // copy list
            List<String> receviersCcList = new ArrayList<String>();


            // Custom Copier
            String receiversCc = alert.getReceiversCc();

            if (StringUtils.isNotEmpty(receiversCc)){
                String[] splits = receiversCc.split(",");
                for (String receiverCc : splits){
                    receviersCcList.add(receiverCc);
                }
            }

            if (CollectionUtils.isEmpty(receviersList) && CollectionUtils.isEmpty(receviersCcList)) {
                logger.warn("alert send error : At least one receiver address required");
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "execution failure,At least one receiver address required.", alert.getId());
                continue;
            }

            if (alert.getAlertType() == AlertType.EMAIL){
                retMaps = emailManager.send(receviersList,receviersCcList, alert.getTitle(), alert.getContent(),alert.getShowType());

                alert.setInfo(retMaps);
            }else if (alert.getAlertType() == AlertType.SMS){
                retMaps = emailManager.send(getReciversForSMS(users), alert.getTitle(), alert.getContent(),alert.getShowType());
                alert.setInfo(retMaps);
            }

            boolean flag = Boolean.parseBoolean(String.valueOf(retMaps.get(Constants.STATUS)));
            if (flag){
                alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, "execution success", alert.getId());
                logger.info("alert send success");
                try {
                    String token = EnterpriseWeChatUtils.getToken();
                    weChatManager.send(alert,token);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }else {
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE,String.valueOf(retMaps.get(Constants.MESSAGE)),alert.getId());
                logger.info("alert send error : {}" , String.valueOf(retMaps.get(Constants.MESSAGE)));
            }
        }

    }


    /**
     * get a list of SMS users
     * @param users
     * @return
     */
    private List<String> getReciversForSMS(List<User> users){
        List<String> list = new ArrayList<>();
        for (User user : users){
            list.add(user.getPhone());
        }
        return list;
    }
}
