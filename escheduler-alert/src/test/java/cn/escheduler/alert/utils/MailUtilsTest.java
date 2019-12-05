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
package cn.escheduler.alert.utils;


import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.model.Alert;
import cn.escheduler.dao.model.User;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;


/**
 */
@Ignore
public class MailUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(MailUtilsTest.class);
    @Test
    public void testSendMails() {
        String[] receivers = new String[]{"xx@xx.com"};
        String[] receiversCc = new String[]{"xxx@xxx.com"};

        String content ="[\"id:69\"," +
                "\"name:UserBehavior-0--1193959466\"," +
                "\"Job name: 启动工作流\"," +
                "\"State: SUCCESS\"," +
                "\"Recovery:NO\"," +
                "\"Run time: 1\"," +
                "\"Start time: 2018-08-06 10:31:34.0\"," +
                "\"End time: 2018-08-06 10:31:49.0\"," +
                "\"Host: 192.168.xx.xx\"," +
                "\"Notify group :4\"]";

        Alert alert = new Alert();
        alert.setTitle("Mysql异常");
        alert.setShowType(ShowType.TEXT);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(4);

        MailUtils.sendMails(Arrays.asList(receivers),Arrays.asList(receiversCc),alert.getTitle(),alert.getContent(), ShowType.TEXT);
    }


    @Test
    public void testQuery(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        List<Alert> alerts = alertDao.listWaitExecutionAlert();

        String[] mails = new String[]{"xx@xx.com"};

        for(Alert alert : alerts){
            MailUtils.sendMails(Arrays.asList(mails),"gaojing", alert.getContent(), alert.getShowType());
        }

    }

    public String list2String(){

        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql服务名称","mysql200");
        map1.put("mysql地址","192.168.xx.xx");
        map1.put("端口","3306");
        map1.put("期间内没有使用索引的查询数握","80");
        map1.put("数据库客户端连接数","190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql服务名称","mysql210");
        map2.put("mysql地址","192.168.xx.xx");
        map2.put("端口","3306");
        map2.put("期间内没有使用索引的查询数握","10");
        map2.put("数据库客户端连接数","90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0,map1);
        maps.add(1,map2);
        String mapjson = JSONUtils.toJsonString(maps);
        logger.info(mapjson);

        return mapjson;

    }

    @Test
    public void testSendTableMail(){
        String[] mails = new String[]{"xx@xx.com"};
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLE);
        String content= list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        MailUtils.sendMails(Arrays.asList(mails),"gaojing", alert.getContent(), ShowType.TABLE);
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
        alert.setContent("[\"告警时间：2018-02-05\", \"服务名：MYSQL_ALTER\", \"告警名：MYSQL_ALTER_DUMP\", \"获取告警异常！，接口报错，异常信息：timed out\", \"请求地址：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        alertDao.addAlert(alert);
    }


    /**
     * Used to test add alarm information, mail sent
     * Table
     */
    @Test
    public void addAlertTable(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLE);

        String content = list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        alertDao.addAlert(alert);
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
        MailUtils.sendMails(Arrays.asList(mails),"gaojing",alert.getContent(),ShowType.ATTACHMENT);
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
        MailUtils.sendMails(Arrays.asList(mails),"gaojing",alert.getContent(),ShowType.TABLEATTACHMENT);
    }

    @Test
    public void template(){
        Template MAIL_TEMPLATE;
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setDefaultEncoding(Constants.UTF_8);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringTemplateLoader);
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(ResourceUtils.getFile(Constants.CLASSPATH_MAIL_TEMPLATES_ALERT_MAIL_TEMPLATE_FTL)),
                    Constants.UTF_8);

            MAIL_TEMPLATE = new Template("alert_mail_template", isr, cfg);
        } catch (Exception e) {
            MAIL_TEMPLATE = null;
        } finally {
            IOUtils.closeQuietly(isr);
        }


        StringWriter out = new StringWriter();
        Map<String,String> map = new HashMap<>();
        map.put(Constants.TITLE,"title_test");
        try {
            MAIL_TEMPLATE.process(map, out);
            logger.info(out.toString());

        } catch (TemplateException e) {
            logger.error(e.getMessage(),e);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

    }

}
