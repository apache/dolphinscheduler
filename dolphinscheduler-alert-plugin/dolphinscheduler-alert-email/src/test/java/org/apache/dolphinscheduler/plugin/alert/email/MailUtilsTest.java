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

import org.apache.dolphinscheduler.plugin.alert.email.template.AlertTemplate;
import org.apache.dolphinscheduler.plugin.alert.email.template.DefaultHTMLTemplate;
import org.apache.dolphinscheduler.plugin.alert.email.template.ShowType;
import org.apache.dolphinscheduler.spi.utils.JacksonUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 */
public class MailUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(MailUtilsTest.class);

    private Map<String, String> emailConfig = new HashMap<>();

    private AlertTemplate alertTemplate;

    MailUtils mailUtils;

    @BeforeClass
    public void initEmailConfig(){
        emailConfig.put(Constants.MAIL_PROTOCOL, "SMTP");
        emailConfig.put(Constants.MAIL_SERVER_HOST, "xxx.xxx.com");
        emailConfig.put(Constants.MAIL_SERVER_PORT, "25");
        emailConfig.put(Constants.MAIL_SENDER, "xxx1.xxx.com");
        emailConfig.put(Constants.MAIL_USER, "xxx2.xxx.com");
        emailConfig.put(Constants.MAIL_PASSWD, "111111");
        emailConfig.put(Constants.MAIL_SMTP_STARTTLS_ENABLE, "true");
        emailConfig.put(Constants.MAIL_SMTP_SSL_ENABLE, "false");
        emailConfig.put(Constants.MAIL_SMTP_SSL_ENABLE, "false");
        alertTemplate = new DefaultHTMLTemplate();
        mailUtils = new MailUtils(emailConfig);
    }

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

        mailUtils.sendMails(
                Arrays.asList(receivers),
                Arrays.asList(receiversCc),
                "Mysql Exception",
                content,
                ShowType.TEXT.getDescp());
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
        String mapjson = JacksonUtils.toJsonString(maps);
        logger.info(mapjson);

        return mapjson;

    }

    @Test
    public void testSendTableMail(){
        String[] mails = new String[]{"347801120@qq.com"};
        String title = "Mysql Exception";
        String content= list2String();
        mailUtils.sendMails(
                Arrays.asList(mails),
                title,
                content,
                ShowType.TABLE.getDescp());
    }

    @Test
    public void testAttachmentFile()throws Exception{
        String[] mails = new String[]{"xx@xx.com"};
        String content = list2String();
        mailUtils.sendMails(
                Arrays.asList(mails),
                "gaojing",
                content,
                ShowType.ATTACHMENT.getDescp());
    }

    @Test
    public void testTableAttachmentFile()throws Exception{
        String[] mails = new String[]{"xx@xx.com"};
        String content = list2String();
        mailUtils.sendMails(
                Arrays.asList(mails),
                "gaojing",
                content,
                ShowType.TABLEATTACHMENT.getDescp());
    }

}
