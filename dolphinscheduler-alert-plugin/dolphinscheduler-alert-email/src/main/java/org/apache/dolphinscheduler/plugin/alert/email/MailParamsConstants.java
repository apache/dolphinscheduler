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

    public static final String MAIL_PROTOCOL = "transport.protocol";
    public static final String NAME_MAIL_PROTOCOL = "protocol";

    public static final String MAIL_SMTP_HOST = "smtp.host";
    public static final String NAME_MAIL_SMTP_HOST = "serverHost";

    public static final String MAIL_SMTP_PORT = "smtp.port";
    public static final String NAME_MAIL_SMTP_PORT = "serverPort";

    public static final String MAIL_SENDER = "sender";
    public static final String NAME_MAIL_SENDER = "sender";

    public static final String MAIL_SMTP_AUTH = "smtp.auth";
    public static final String NAME_MAIL_SMTP_AUTH = "enableSmtpAuth";

    public static final String MAIL_USER = "user";
    public static final String NAME_MAIL_USER = "user";

    public static final String MAIL_PASSWD = "passwd";
    public static final String NAME_MAIL_PASSWD = "passwd";

    public static final String MAIL_SMTP_STARTTLS_ENABLE = "smtp.starttls.enable";
    public static final String NAME_MAIL_SMTP_STARTTLS_ENABLE = "starttlsEnable";

    public static final String MAIL_SMTP_SSL_ENABLE = "smtp.ssl.enable";
    public static final String NAME_MAIL_SMTP_SSL_ENABLE = "sslEnable";

    public static final String MAIL_SMTP_SSL_TRUST = "smtp.ssl.trust";
    public static final String NAME_MAIL_SMTP_SSL_TRUST = "smtpSslTrust";

}
