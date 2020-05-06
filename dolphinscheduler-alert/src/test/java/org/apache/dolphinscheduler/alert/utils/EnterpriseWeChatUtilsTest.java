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

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.plugin.model.AlertData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;

/**
 * Please manually modify the configuration file before testing.
 * file: alert.properties
 *   enterprise.wechat.corp.id
 *   enterprise.wechat.secret
 *   enterprise.wechat.token.url
 *   enterprise.wechat.push.url
 *   enterprise.wechat.send.msg
 *   enterprise.wechat.agent.id
 *   enterprise.wechat.users
 */
@PrepareForTest(PropertyUtils.class)
@RunWith(PowerMockRunner.class)
public class EnterpriseWeChatUtilsTest {

    private static final String toParty = "wwc99134b6fc1edb6";
    private static final String enterpriseWechatSecret = "Uuv2KFrkdf7SeKOsTDCpsTkpawXBMNRhFy6VKX5FV";
    private static final String enterpriseWechatAgentId = "1000004";
    private static final String enterpriseWechatUsers="LiGang,journey";
    private static final String msg = "hello world";

    private static final String enterpriseWechatTeamSendMsg = "{\\\"toparty\\\":\\\"$toParty\\\",\\\"agentid\\\":\\\"$agentId\\\",\\\"msgtype\\\":\\\"text\\\",\\\"text\\\":{\\\"content\\\":\\\"$msg\\\"},\\\"safe\\\":\\\"0\\\"}";
    private static final String enterpriseWechatUserSendMsg = "{\\\"touser\\\":\\\"$toUser\\\",\\\"agentid\\\":\\\"$agentId\\\",\\\"msgtype\\\":\\\"markdown\\\",\\\"markdown\\\":{\\\"content\\\":\\\"$msg\\\"}}";

    @Before
    public void init(){
        PowerMockito.mockStatic(PropertyUtils.class);
        Mockito.when(PropertyUtils.getBoolean(Constants.ENTERPRISE_WECHAT_ENABLE)).thenReturn(true);
        Mockito.when(PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_USER_SEND_MSG)).thenReturn(enterpriseWechatUserSendMsg);
        Mockito.when(PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_TEAM_SEND_MSG)).thenReturn(enterpriseWechatTeamSendMsg);
    }

    @Test
    public void testIsEnable(){
        Boolean weChartEnable = EnterpriseWeChatUtils.isEnable();
        Assert.assertTrue(weChartEnable);
    }


    @Test
    public void testMakeTeamSendMsg1(){
        String sendMsg = EnterpriseWeChatUtils.makeTeamSendMsg(toParty, enterpriseWechatSecret, msg);
        Assert.assertTrue(sendMsg.contains(toParty));
        Assert.assertTrue(sendMsg.contains(enterpriseWechatSecret));
        Assert.assertTrue(sendMsg.contains(msg));

    }


    @Test
    public void testMakeTeamSendMsg2(){
        List<String> parties = new ArrayList<>();
        parties.add(toParty);
        parties.add("test1");

        String sendMsg = EnterpriseWeChatUtils.makeTeamSendMsg(parties, enterpriseWechatSecret, msg);
        Assert.assertTrue(sendMsg.contains(toParty));
        Assert.assertTrue(sendMsg.contains(enterpriseWechatSecret));
        Assert.assertTrue(sendMsg.contains(msg));
    }

    @Test
    public void tesMakeUserSendMsg1(){

        String sendMsg = EnterpriseWeChatUtils.makeUserSendMsg(enterpriseWechatUsers, enterpriseWechatAgentId, msg);
        Assert.assertTrue(sendMsg.contains(enterpriseWechatUsers));
        Assert.assertTrue(sendMsg.contains(enterpriseWechatAgentId));
        Assert.assertTrue(sendMsg.contains(msg));
    }

    @Test
    public void tesMakeUserSendMsg2(){
        List<String> users = new ArrayList<>();
        users.add("user1");
        users.add("user2");

        String sendMsg = EnterpriseWeChatUtils.makeUserSendMsg(users, enterpriseWechatAgentId, msg);
        Assert.assertTrue(sendMsg.contains(users.get(0)));
        Assert.assertTrue(sendMsg.contains(users.get(1)));
        Assert.assertTrue(sendMsg.contains(enterpriseWechatAgentId));
        Assert.assertTrue(sendMsg.contains(msg));
    }

    @Test
    public void testMarkdownByAlertForText(){
        Alert alertForText = createAlertForText();
        AlertData alertData = new AlertData();
        alertData.setTitle(alertForText.getTitle())
                .setShowType(alertForText.getShowType().getDescp())
                .setContent(alertForText.getContent());
        String result = EnterpriseWeChatUtils.markdownByAlert(alertData);
        Assert.assertNotNull(result);
    }

    @Test
    public void testMarkdownByAlertForTable(){
        Alert alertForText = createAlertForTable();
        AlertData alertData = new AlertData();
        alertData.setTitle(alertForText.getTitle())
                .setShowType(alertForText.getShowType().getDescp())
                .setContent(alertForText.getContent());
        String result = EnterpriseWeChatUtils.markdownByAlert(alertData);
        Assert.assertNotNull(result);
    }

    private Alert createAlertForText(){
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

        return alert;
    }

    private String list2String(){

        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name","mysql200");
        map1.put("mysql address","192.168.xx.xx");
        map1.put("port","3306");
        map1.put("no index of number","80");
        map1.put("database client connections","190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name","mysql210");
        map2.put("mysql address","192.168.xx.xx");
        map2.put("port", "3306");
        map2.put("no index of number", "10");
        map2.put("database client connections", "90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0, map1);
        maps.add(1, map2);
        String mapjson = JSONUtils.toJsonString(maps);
        return mapjson;
    }

    private Alert createAlertForTable(){
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TABLE);
        String content= list2String();
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        return alert;
    }




//    @Test
//    public void testSendSingleTeamWeChat() {
//        try {
//            String token = EnterpriseWeChatUtils.getToken();
//            String msg = EnterpriseWeChatUtils.makeTeamSendMsg(partyId, agentId, "hello world");
//            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);
//
//            String errmsg = JSON.parseObject(resp).getString("errmsg");
//            Assert.assertEquals("ok",errmsg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testSendMultiTeamWeChat() {
//
//        try {
//            String token = EnterpriseWeChatUtils.getToken();
//            String msg = EnterpriseWeChatUtils.makeTeamSendMsg(listPartyId, agentId, "hello world");
//            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);
//
//            String errmsg = JSON.parseObject(resp).getString("errmsg");
//            Assert.assertEquals("ok",errmsg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testSendSingleUserWeChat() {
//        try {
//            String token = EnterpriseWeChatUtils.getToken();
//            String msg = EnterpriseWeChatUtils.makeUserSendMsg(listUserId.stream().findFirst().get(), agentId, "your meeting room has been booked and will be synced to the 'mailbox' later \n" +
//                    ">**matter details** \n" +
//                    ">matter：<font color='info'>meeting</font> <br>" +
//                    ">organizer：@miglioguan \n" +
//                    ">participant：@miglioguan、@kunliu、@jamdeezhou、@kanexiong、@kisonwang \n" +
//                    "> \n" +
//                    ">meeting room：<font color='info'>Guangzhou TIT 1st Floor 301</font> \n" +
//                    ">date：<font color='warning'>May 18, 2018</font> \n" +
//                    ">time：<font color='comment'>9:00-11:00 am</font> \n" +
//                    "> \n" +
//                    ">please attend the meeting on time\n" +
//                    "> \n" +
//                    ">to modify the meeting information, please click: [Modify Meeting Information](https://work.weixin.qq.com)\"");
//
//            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);
//
//            String errmsg = JSON.parseObject(resp).getString("errmsg");
//            Assert.assertEquals("ok",errmsg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testSendMultiUserWeChat() {
//        try {
//            String token = EnterpriseWeChatUtils.getToken();
//
//            String msg = EnterpriseWeChatUtils.makeUserSendMsg(listUserId, agentId, "hello world");
//            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);
//
//            String errmsg = JSON.parseObject(resp).getString("errmsg");
//            Assert.assertEquals("ok",errmsg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
