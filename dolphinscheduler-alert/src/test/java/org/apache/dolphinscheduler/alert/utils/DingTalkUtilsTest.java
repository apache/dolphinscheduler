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
    public void testSendMsgByProxy() {
        try {
           String msgTosend = "msg to send by proxy";
           String rsp = DingTalkUtils.sendDingTalkMsg(msgTosend, Constants.UTF_8);
           logger.info("send msg result:{}",rsp);
            String errmsg = JSON.parseObject(rsp).getString("errmsg");
            Assert.assertEquals("ok", errmsg);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
}
