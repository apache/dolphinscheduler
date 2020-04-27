package org.apache.dolphinscheduler.alert.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DingTalkMsgFormatterTest {
    private static final Logger logger = LoggerFactory.getLogger(DingTalkMsgFormatter.class);
    @Test
    public void testDingTalkMsgFormatter() {
      DingTalkMsgFormatter obj = new DingTalkMsgFormatter("this is test");
      String msg = obj.toTextMsg();

      logger.info(msg);
      String expect = "{\"text\":{\"content\":\"this is test\"},\"msgtype\":\"text\"}";
      Assert.assertEquals(expect, msg);
    }

    @Test
    public void testDingTalkMsgFormatterUtf8() {
        DingTalkMsgFormatter obj = new DingTalkMsgFormatter("this is test:中文");
        String msg = obj.toTextMsg();

        logger.info("test support utf8, actual:" + msg);
        String expect = "{\"text\":{\"content\":\"this is test:中文\"},\"msgtype\":\"text\"}";
        Assert.assertEquals(expect, msg);
    }
}
