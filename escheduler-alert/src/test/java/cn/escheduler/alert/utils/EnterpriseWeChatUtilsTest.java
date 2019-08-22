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
            Assert.assertEquals(errmsg, "ok");
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
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendSingleUserWeChat() {
        try {
            String token = EnterpriseWeChatUtils.getToken();
            String msg = EnterpriseWeChatUtils.makeUserSendMsg(listUserId.stream().findFirst().get(), agentId, "您的会议室已经预定，稍后会同步到`邮箱` \n" +
                    ">**事项详情** \n" +
                    ">事　项：<font color='info'>开会</font> <br>" +
                    ">组织者：@miglioguan \n" +
                    ">参与者：@miglioguan、@kunliu、@jamdeezhou、@kanexiong、@kisonwang \n" +
                    "> \n" +
                    ">会议室：<font color='info'>广州TIT 1楼 301</font> \n" +
                    ">日　期：<font color='warning'>2018年5月18日</font> \n" +
                    ">时　间：<font color='comment'>上午9:00-11:00</font> \n" +
                    "> \n" +
                    ">请准时参加会议。 \n" +
                    "> \n" +
                    ">如需修改会议信息，请点击：[修改会议信息](https://work.weixin.qq.com)\"");

            String resp = EnterpriseWeChatUtils.sendEnterpriseWeChat("utf-8", msg, token);

            String errmsg = JSON.parseObject(resp).getString("errmsg");
            Assert.assertEquals(errmsg, "ok");
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
            Assert.assertEquals(errmsg, "ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
