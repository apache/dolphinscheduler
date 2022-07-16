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
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmailAlertChannel implements AlertChannel {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlertChannel.class);
    private static final String mustNotNull = " must not be null";

    @Override
    public AlertResult process(AlertInfo info) {

        AlertData alert = info.getAlertData();
        Map<String, String> paramsMap = info.getAlertParams();
        //verify input params
        AlertResult verifyResult = verifyParams(paramsMap);
        if (String.valueOf(Boolean.FALSE).equalsIgnoreCase(verifyResult.getStatus())) {
            return verifyResult;
        }
        MailSender mailSender = new MailSender(paramsMap);
        AlertResult alertResult = mailSender.sendMails(alert.getTitle(), alert.getContent());

        boolean flag;

        if (alertResult == null) {
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("alert send error.");
            logger.info("alert send error : {}", alertResult.getMessage());
            return alertResult;
        }

        flag = Boolean.parseBoolean(String.valueOf(alertResult.getStatus()));

        if (flag) {
            logger.info("alert send success");
            alertResult.setMessage("email send success.");
        } else {
            alertResult.setMessage("alert send error.");
            logger.info("alert send error : {}", alertResult.getMessage());
        }

        return alertResult;
    }

    public static AlertResult verifyParams(Map<String, String> params) {
        if (null == params) {
            return new AlertResult(String.valueOf(Boolean.FALSE), "mail params is null");
        }
        if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS))) {
            return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS + mustNotNull);
        }
        if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SMTP_HOST))) {
            return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SMTP_HOST + mustNotNull);
        }
        if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SMTP_PORT))) {
            return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SMTP_PORT + mustNotNull);
        }
        if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SENDER))) {
            return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SENDER + mustNotNull);
        }

        if (Boolean.TRUE.toString().equalsIgnoreCase(params.get(MailParamsConstants.NAME_MAIL_SMTP_AUTH))) {
            if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_USER))) {
                return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_USER + mustNotNull);
            }
            if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_PASSWD))) {
                return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_PASSWD + mustNotNull);
            }
            if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE))) {
                return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE + mustNotNull);
            }
            if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE))) {
                return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE + mustNotNull);
            }
            if (StringUtils.isEmpty(params.get(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST))) {
                return new AlertResult(String.valueOf(Boolean.FALSE), MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST + mustNotNull);
            }
        }
        if (StringUtils.isEmpty(params.get(AlertConstants.NAME_SHOW_TYPE))) {
            return new AlertResult(String.valueOf(Boolean.FALSE), AlertConstants.NAME_SHOW_TYPE + mustNotNull);
        }
        return new AlertResult(String.valueOf(Boolean.TRUE), null);
    }
}
