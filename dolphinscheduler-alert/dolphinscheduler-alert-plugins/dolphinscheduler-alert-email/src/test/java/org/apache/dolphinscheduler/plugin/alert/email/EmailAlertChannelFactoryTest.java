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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class EmailAlertChannelFactoryTest {
    @Test
    public void testGetParams() {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        List<PluginParams> params = emailAlertChannelFactory.params();
        Assert.assertEquals(12, params.size());
    }

    @Test
    public void testCreate() {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        AlertChannel alertChannel = emailAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }

    @Test
    public void testVerifyParamsWithAuth() {
        Map<String, String> enableAuthConfig = new HashMap<>();
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_PROTOCOL, "smtp");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_HOST, "xxx.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_PORT, "25");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SENDER, "xxx1.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_USER, "xxx2.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_PASSWD, "111111");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "true");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, "true");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, "false");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, "false");
        enableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "qq@qq.com");
        enableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, "qq@qq.com");
        enableAuthConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        AlertResult result = EmailAlertChannel.verifyParams(enableAuthConfig);
        Assert.assertEquals(result.getStatus(), String.valueOf(Boolean.TRUE));
    }

    @Test
    public void testVerifyParamsWithoutAuth() {
        Map<String, String> disableAuthConfig = new HashMap<>();
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_PROTOCOL, "smtp");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_HOST, "xxx.xxx.com");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_PORT, "25");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SENDER, "xxx1.xxx.com");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "false");
        disableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "qq@qq.com");
        disableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, "qq@qq.com");
        disableAuthConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        AlertResult result = EmailAlertChannel.verifyParams(disableAuthConfig);
        Assert.assertEquals(result.getStatus(), String.valueOf(Boolean.TRUE));
    }

    @Test
    public void testVerifyParams() {
        AlertResult result = EmailAlertChannel.verifyParams(null);
        Assert.assertEquals(result.getStatus(), String.valueOf(Boolean.FALSE));
    }


}
