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

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled("The test case makes no sense")
public class MailUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(MailUtilsTest.class);
    static MailSender mailSender;
    private static Map<String, String> emailConfig = new HashMap<>();

    @BeforeAll
    public static void initEmailConfig() {
        emailConfig.put(MailParamsConstants.NAME_MAIL_PROTOCOL, "smtp");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_HOST, "xxx.xxx.com");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_PORT, "25");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SENDER, "xxx1.xxx.com");
        emailConfig.put(MailParamsConstants.NAME_MAIL_USER, "xxx2.xxx.com");
        emailConfig.put(MailParamsConstants.NAME_MAIL_PASSWD, "111111");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "true");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, "true");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, "false");
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, "false");
        emailConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "347801120@qq.com");
        emailConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, "347801120@qq.com");
        emailConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        mailSender = new MailSender(emailConfig);
    }

    @Test
    public void testSendMails() {

        String content = "[\"id:69\","
                + "\"name:UserBehavior-0--1193959466\","
                + "\"Job name: Start workflow\","
                + "\"State: SUCCESS\","
                + "\"Recovery:NO\","
                + "\"Run time: 1\","
                + "\"Start time: 2018-08-06 10:31:34.0\","
                + "\"End time: 2018-08-06 10:31:49.0\","
                + "\"Host: 192.168.xx.xx\","
                + "\"Notify group :4\"]";

        AlertResult alertResult = mailSender.sendMails(
                "Mysql Exception",
                content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    void testAuthCheck() {
        String title = "Auth Exception";
        String content = list2String();

        // test auth false and user && pwd null will pass
        emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "false");
        emailConfig.put(MailParamsConstants.NAME_MAIL_USER, null);
        emailConfig.put(MailParamsConstants.NAME_MAIL_PASSWD, null);
        mailSender = new MailSender(emailConfig);
        mailSender.sendMails(title, content);

        try {
            // test auth true and user null will throw exception
            emailConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "true");
            emailConfig.put(MailParamsConstants.NAME_MAIL_USER, null);
            mailSender = new MailSender(emailConfig);
            mailSender.sendMails(title, content);
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains(MailParamsConstants.NAME_MAIL_USER));
        }

        // test auth true and user && pwd not null will pass
        emailConfig.put(MailParamsConstants.NAME_MAIL_USER, "user");
        emailConfig.put(MailParamsConstants.NAME_MAIL_PASSWD, "passwd");
        mailSender = new MailSender(emailConfig);
        AlertResult alertResult = mailSender.sendMails(title, content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    public String list2String() {

        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name", "mysql200");
        map1.put("mysql address", "192.168.xx.xx");
        map1.put("port", "3306");
        map1.put("no index of number", "80");
        map1.put("database client connections", "190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name", "mysql210");
        map2.put("mysql address", "192.168.xx.xx");
        map2.put("port", "3306");
        map2.put("no index of number", "10");
        map2.put("database client connections", "90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0, map1);
        maps.add(1, map2);
        String mapjson = JSONUtils.toJsonString(maps);
        logger.info(mapjson);

        return mapjson;
    }

    @Test
    public void testSendTableMail() {
        String title = "Mysql Exception";
        String content = list2String();
        emailConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TABLE.getDescp());
        mailSender = new MailSender(emailConfig);
        AlertResult alertResult = mailSender.sendMails(title, content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testAttachmentFile() {
        String content = list2String();
        emailConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.ATTACHMENT.getDescp());
        mailSender = new MailSender(emailConfig);
        AlertResult alertResult = mailSender.sendMails("gaojing", content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testTableAttachmentFile() {
        String content = list2String();
        emailConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TABLE_ATTACHMENT.getDescp());
        mailSender = new MailSender(emailConfig);
        AlertResult alertResult = mailSender.sendMails("gaojing", content);
        Assertions.assertFalse(alertResult.isSuccess());
    }
}
