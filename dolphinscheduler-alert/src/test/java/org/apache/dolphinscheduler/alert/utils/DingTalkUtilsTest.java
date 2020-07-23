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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

@PrepareForTest(PropertyUtils.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class DingTalkUtilsTest {
    Logger logger = LoggerFactory.getLogger(DingTalkUtilsTest.class);

    private static final String mockUrl = "https://oapi.dingtalk.com/robot/send?access_token=test";
    private static final String mockKeyWords = "onway";
    private static final String msg = "ding talk test";

    @Before
    public void init(){
        PowerMockito.mockStatic(PropertyUtils.class);
        Mockito.when(PropertyUtils.getString(Constants.DINGTALK_WEBHOOK)).thenReturn(mockUrl);
        Mockito.when(PropertyUtils.getString(Constants.DINGTALK_KEYWORD)).thenReturn(mockKeyWords);
        Mockito.when(PropertyUtils.getBoolean(Constants.DINGTALK_PROXY_ENABLE)).thenReturn(true);
        Mockito.when(PropertyUtils.getString(Constants.DINGTALK_PROXY)).thenReturn("proxy.com.cn");
        Mockito.when(PropertyUtils.getString(Constants.DINGTALK_USER)).thenReturn("user");
        Mockito.when(PropertyUtils.getString(Constants.DINGTALK_PASSWORD)).thenReturn("pswd");
        Mockito.when(PropertyUtils.getInt(Constants.DINGTALK_PORT)).thenReturn(80);
    }

//    @Test
//    @Ignore
//    public void testSendMsg() {
//        try {
//           String msgTosend = "msg to send";
//            logger.info(PropertyUtils.getString(Constants.DINGTALK_WEBHOOK));
//           String rsp = DingTalkUtils.sendDingTalkMsg(msgTosend, Constants.UTF_8);
//           logger.info("send msg result:{}",rsp);
//            String errmsg = JSONUtils.parseObject(rsp).getString("errmsg");
//            Assert.assertEquals("ok", errmsg);
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testCreateDefaultClient() {
        CloseableHttpClient client = DingTalkUtils.getDefaultClient();;
        try {
            Assert.assertNotNull(client);
            client.close();
        } catch (IOException ex) {
            logger.info("close exception",ex.getMessage());
            new Throwable();
        }
    }
    @Test
    public void testCreateProxyClient() {
        CloseableHttpClient client = DingTalkUtils.getProxyClient();
        try {
            Assert.assertNotNull(client);
            client.close();
        } catch (IOException ex) {
            logger.info("close exception",ex.getMessage());
            new Throwable();
        }

    }
    @Test
    public void testProxyConfig() {
        RequestConfig rc = DingTalkUtils.getProxyConfig();
        Assert.assertEquals(rc.getProxy().getPort(), 80);
        Assert.assertEquals(rc.getProxy().getHostName(), "proxy.com.cn");
    }

    @Test
    public void testDingTalkMsgToJson() {
        String jsonString = DingTalkUtils.textToJsonString("this is test");

        logger.info(jsonString);
        String expect = "{\"text\":{\"content\":\"this is test\"},\"msgtype\":\"text\"}";
        Assert.assertEquals(expect, jsonString);
    }
    @Test
    public void testDingTalkMsgUtf8() {
        String msg = DingTalkUtils.textToJsonString("this is test:中文");

        logger.info("test support utf8, actual:" + msg);
        logger.info("test support utf8, actual:" + DingTalkUtils.isEnableDingTalk);
        String expect = "{\"text\":{\"content\":\"this is test:中文\"},\"msgtype\":\"text\"}";
        Assert.assertEquals(expect, msg);
    }

}
