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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;


/**
 * To enable the test case, the following params should be set in alert.properties.
 * dingtalk.isEnable=flase
 * dingtalk.webhook=https://oapi.dingtalk.com/robot/send?access_token=xxxx
 * dingtalk.keyword=
 * dingtalk.proxy=
 * dingtalk.port=80
 * dingtalk.user=
 * dingtalk.password=
 * dingtalk.isEnableProxy=false
 */
public class DingTalkUtilsTest {
    Logger logger = LoggerFactory.getLogger(DingTalkUtilsTest.class);

    @Test
    @Ignore
    public void testSendMsg() {
        try {
           String msgTosend = "msg to send";
           String rsp = DingTalkUtils.sendDingTalkMsg(msgTosend, Constants.UTF_8);
           logger.info("send msg result:{}",rsp);
            String errmsg = JSON.parseObject(rsp).getString("errmsg");
            Assert.assertEquals("ok", errmsg);
        }  catch (Exception e) {
            e.printStackTrace();
        }
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
        String expect = "{\"text\":{\"content\":\"this is test:中文\"},\"msgtype\":\"text\"}";
        Assert.assertEquals(expect, msg);
    }
}
