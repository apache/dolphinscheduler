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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

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
@Ignore
public class EnterpriseWeChatUtilsTest {

    private String agentId = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_AGENT_ID); // app id
    private Collection<String> listUserId = Arrays.asList(PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_USERS).split(","));

    // Please change
    private String partyId = "2";
    private Collection<String> listPartyId = Arrays.asList("2","4");
    @Test
    public void testSendSingleTeamWeChat() {
        try {
            String token = EnterpriseWeChatUtils.getToken();
            String msg = EnterpriseWeChatUtils.makeTeamSendMsg(partyId, agentId, "hello world");
            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals("ok",errmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMultiTeamWeChat() {

        try {
            String token = EnterpriseWeChatUtils.getToken();
            String msg = EnterpriseWeChatUtils.makeTeamSendMsg(listPartyId, agentId, "hello world");
            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals("ok",errmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendSingleUserWeChat() {
        try {
            String token = EnterpriseWeChatUtils.getToken();
            String msg = EnterpriseWeChatUtils.makeUserSendMsg(listUserId.stream().findFirst().get(), agentId, "your meeting room has been booked and will be synced to the 'mailbox' later \n" +
                    ">**matter details** \n" +
                    ">matter：<font color='info'>meeting</font> <br>" +
                    ">organizer：@miglioguan \n" +
                    ">participant：@miglioguan、@kunliu、@jamdeezhou、@kanexiong、@kisonwang \n" +
                    "> \n" +
                    ">meeting room：<font color='info'>Guangzhou TIT 1st Floor 301</font> \n" +
                    ">date：<font color='warning'>May 18, 2018</font> \n" +
                    ">time：<font color='comment'>9:00-11:00 am</font> \n" +
                    "> \n" +
                    ">please attend the meeting on time\n" +
                    "> \n" +
                    ">to modify the meeting information, please click: [Modify Meeting Information](https://work.weixin.qq.com)\"");

            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals("ok",errmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMultiUserWeChat() {
        try {
            String token = EnterpriseWeChatUtils.getToken();

            String msg = EnterpriseWeChatUtils.makeUserSendMsg(listUserId, agentId, "hello world");
            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals("ok",errmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
