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
package org.apache.dolphinscheduler.alert.utils;


import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 */
public class MailUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(MailUtilsTest.class);
    @Test
    public void testSendMails() {
        String[] receivers = new String[]{"347801120@qq.com"};
        String[] receiversCc = new String[]{"347801120@qq.com"};

        String content ="[\"id:69\"," +
                "\"name:UserBehavior-0--1193959466\"," +
                "\"Job name: Start workflow\"," +
                "\"State: SUCCESS\"," +
                "\"Recovery:NO\"," +
                "\"Run time: 1\"," +
                "\"Start time: 2018-08-06 10:31:34.0\"," +
                "\"End time: 2018-08-06 10:31:49.0\"," +
                "\"Host: 192.168.xx.xx\"," +
                "\"Notify group :4\"]";

        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TEXT);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(4);

        MailUtils.sendMails(Arrays.asList(receivers),Arrays.asList(receiversCc),alert.getTitle(),alert.getContent(), ShowType.TEXT.getDescp());
    }


    @Test
    public void testQuery(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        List<Alert> alerts = alertDao.listWaitExecutionAlert();

        String[] mails = new String[]{"xx@xx.com"};

        for(Alert alert : alerts){
            MailUtils.sendMails(Arrays.asList(mails),"gaojing", alert.getContent(), ShowType.TABLE.getDescp());
        }

    }

    public String list2String(){

        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name","mysql200");
        map1.put("mysql address","192.168.xx.xx");
        map1.put("port","3306");
        map1.put("no index of number","80");
        map1.put("database client connections","190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name","mysql210");
        map2.put("mysql address","192.168.xx.xx");
        map2.put("port","3306");
        map2.put("no index of number","10");
        map2.put("database client connections","90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0,map1);
        maps.add(1,map2);
        String mapjson = JSONUtils.toJsonString(maps);
        logger.info(mapjson);

        return mapjson;

    }

    @Test
    public void testSendTableMail(){
        String[] mails = new String[]{"347801120@qq.com"};
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLE);
        String content= list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        MailUtils.sendMails(Arrays.asList(mails),"gaojing", alert.getContent(), ShowType.TABLE.getDescp());
    }

    /**
     * Used to test add alarm information, mail sent
     * Text
     */
    @Test
    public void addAlertText(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TEXT);
        alert.setContent("[\"alarm time：2018-02-05\", \"service name：MYSQL_ALTER\", \"alarm name：MYSQL_ALTER_DUMP\", " +
                "\"get the alarm exception.！，interface error，exception information：timed out\", \"request address：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        alertDao.addAlert(alert);
    }


    /**
     * Used to test add alarm information, mail sent
     * Table
     */
    @Test
    public void testAddAlertTable(){
        logger.info("testAddAlertTable");
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        Assert.assertNotNull(alertDao);
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLE);

        String content = list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        alertDao.addAlert(alert);
        logger.info("" +alert);
    }

    @Test
    public void testAlertDao(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        List<User> users = alertDao.listUserByAlertgroupId(3);
        logger.info(users.toString());
    }

    @Test
    public void testAttachmentFile()throws Exception{
        String[] mails = new String[]{"xx@xx.com"};
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.ATTACHMENT);
        String content = list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        MailUtils.sendMails(Arrays.asList(mails),"gaojing",alert.getContent(),ShowType.ATTACHMENT.getDescp());
    }

    @Test
    public void testTableAttachmentFile()throws Exception{
        String[] mails = new String[]{"xx@xx.com"};
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        String content = list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        MailUtils.sendMails(Arrays.asList(mails),"gaojing",alert.getContent(),ShowType.TABLEATTACHMENT.getDescp());
    }

}
