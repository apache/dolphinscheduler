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

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

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
 */
public class EnterpriseWeChatUtilsTest {

    // Please change
    private String agentId = "1000002"; // app id
    private String partyId = "2";
    private Collection<String> listPartyId = Arrays.asList("2","4");
    private String userId = "test1";
    private Collection<String> listUserId = Arrays.asList("test1","test2");

    @Test
    public void testSendSingleTeamWeChat() {
        EnterpriseWeChatUtils wx = new EnterpriseWeChatUtils();

        try {
            String token = wx.getToken();
            String msg = wx.makeTeamSendMsg(partyId, agentId, "hello world");
            String resp = wx.sendQiyeWeixin("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMultiTeamWeChat() {
        EnterpriseWeChatUtils wx = new EnterpriseWeChatUtils();

        try {
            String token = wx.getToken();
            String msg = wx.makeTeamSendMsg(listPartyId, agentId, "hello world");
            String resp = wx.sendQiyeWeixin("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendSingleUserWeChat() {
        EnterpriseWeChatUtils wx = new EnterpriseWeChatUtils();

        try {
            String token = wx.getToken();
            String msg = wx.makeUserSendMsg(userId, agentId, "hello world");
            String resp = wx.sendQiyeWeixin("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMultiUserWeChat() {
        EnterpriseWeChatUtils wx = new EnterpriseWeChatUtils();

        try {
            String token = wx.getToken();
            String msg = wx.makeUserSendMsg(listUserId, agentId, "hello world");
            String resp = wx.sendQiyeWeixin("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
