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

/**
 * mail plugin params json use
 */
public class MailParamsConstants {

    private MailParamsConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PLUGIN_DEFAULT_EMAIL_RECEIVERS = "$t('receivers')";
    public static final String NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS = "receivers";

    public static final String PLUGIN_DEFAULT_EMAIL_RECEIVERCCS = "$t('receiverCcs')";
    public static final String NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS = "receiverCcs";

    public static final String MAIL_PROTOCOL = "$t('transportProtocol')";
    public static final String NAME_MAIL_PROTOCOL = "mail.protocol";

    public static final String MAIL_SMTP_HOST = "$t('mailSmtpHost')";
    public static final String NAME_MAIL_SMTP_HOST = "serverHost";

    public static final String MAIL_SMTP_PORT = "$t('mailSmtpPort')";
    public static final String NAME_MAIL_SMTP_PORT = "serverPort";

    public static final String MAIL_SENDER = "$t('mailSender')";
    public static final String NAME_MAIL_SENDER = "sender";

    public static final String MAIL_SMTP_AUTH = "$t('mailSmtpAuth')";
    public static final String NAME_MAIL_SMTP_AUTH = "enableSmtpAuth";

    public static final String MAIL_USER = "$t('mailUser')";
    public static final String NAME_MAIL_USER = "User";

    public static final String MAIL_PASSWD = "$t('mailPasswd')";
    public static final String NAME_MAIL_PASSWD = "Password";

    public static final String MAIL_SMTP_STARTTLS_ENABLE = "$t('mailSmtpStarttlsEnable')";
    public static final String NAME_MAIL_SMTP_STARTTLS_ENABLE = "starttlsEnable";

    public static final String MAIL_SMTP_SSL_ENABLE = "$t('mailSmtpSslEnable')";
    public static final String NAME_MAIL_SMTP_SSL_ENABLE = "sslEnable";

    public static final String MAIL_SMTP_SSL_TRUST = "$t('mailSmtpSslTrust')";
    public static final String NAME_MAIL_SMTP_SSL_TRUST = "smtpSslTrust";

}
