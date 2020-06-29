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

/**
 * constants
 */
public class Constants {
    private Constants() {
        throw new IllegalStateException("Constants class");
    }
    /**
     * alert properties path
     */
    public static final String ALERT_PROPERTIES_PATH = "/alert.properties";

    public static final String DATA_SOURCE_PROPERTIES_PATH = "/dao/data_source.properties";

    public static final String SINGLE_SLASH = "/";

    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";

    public static final String STATUS = "status";

    public static final String MESSAGE = "message";

    public static final String MAIL_PROTOCOL = "mail.protocol";

    public static final String MAIL_SERVER_HOST = "mail.server.host";

    public static final String MAIL_SERVER_PORT = "mail.server.port";

    public static final String MAIL_SENDER = "mail.sender";

    public static final String MAIL_USER = "mail.user";

    public static final String MAIL_PASSWD = "mail.passwd";

    public static final String XLS_FILE_PATH = "xls.file.path";

    public static final String MAIL_HOST = "mail.smtp.host";

    public static final String MAIL_PORT = "mail.smtp.port";

    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";

    public static final String MAIL_SMTP_SSL_TRUST="mail.smtp.ssl.trust";

    public static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";

    public static final String STRING_TRUE = "true";

    public static final String EXCEL_SUFFIX_XLS = ".xls";

    public static final int NUMBER_1000 = 1000;

    public static final String SPRING_DATASOURCE_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";

    public static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

    public static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";

    public static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    public static final String SPRING_DATASOURCE_VALIDATION_QUERY_TIMEOUT = "spring.datasource.validationQueryTimeout";

    public static final String SPRING_DATASOURCE_INITIAL_SIZE = "spring.datasource.initialSize";

    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";

    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";

    public static final String SPRING_DATASOURCE_MAX_WAIT = "spring.datasource.maxWait";

    public static final String SPRING_DATASOURCE_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "spring.datasource.timeBetweenEvictionRunsMillis";

    public static final String SPRING_DATASOURCE_MIN_EVICTABLE_IDLE_TIME_MILLIS = "spring.datasource.minEvictableIdleTimeMillis";

    public static final String SPRING_DATASOURCE_VALIDATION_QUERY = "spring.datasource.validationQuery";

    public static final String SPRING_DATASOURCE_TEST_WHILE_IDLE = "spring.datasource.testWhileIdle";

    public static final String SPRING_DATASOURCE_TEST_ON_BORROW = "spring.datasource.testOnBorrow";

    public static final String SPRING_DATASOURCE_TEST_ON_RETURN = "spring.datasource.testOnReturn";

    public static final String SPRING_DATASOURCE_POOL_PREPARED_STATEMENTS = "spring.datasource.poolPreparedStatements";

    public static final String SPRING_DATASOURCE_DEFAULT_AUTO_COMMIT = "spring.datasource.defaultAutoCommit";

    public static final String SPRING_DATASOURCE_KEEP_ALIVE = "spring.datasource.keepAlive";

    public static final String SPRING_DATASOURCE_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = "spring.datasource.maxPoolPreparedStatementPerConnectionSize";

    public static final String DEVELOPMENT = "development";

    public static final String TR = "<tr>";

    public static final String TD = "<td>";

    public static final String TD_END = "</td>";

    public static final String TR_END = "</tr>";

    public static final String TITLE = "title";

    public static final String CONTENT = "content";

    public static final String TH = "<th>";

    public static final String TH_END = "</th>";

    public static final int ALERT_SCAN_INTERVAL = 5000;

    public static final String MARKDOWN_QUOTE = ">";

    public static final String MARKDOWN_ENTER = "\n";

    public static final String ENTERPRISE_WECHAT_ENABLE = "enterprise.wechat.enable";

    public static final String ENTERPRISE_WECHAT_CORP_ID = "enterprise.wechat.corp.id";

    public static final String ENTERPRISE_WECHAT_SECRET = "enterprise.wechat.secret";

    public static final String ENTERPRISE_WECHAT_TOKEN_URL = "enterprise.wechat.token.url";

    public static final String ENTERPRISE_WECHAT_PUSH_URL = "enterprise.wechat.push.url";

    public static final String ENTERPRISE_WECHAT_TEAM_SEND_MSG = "enterprise.wechat.team.send.msg";

    public static final String ENTERPRISE_WECHAT_USER_SEND_MSG = "enterprise.wechat.user.send.msg";

    public static final String ENTERPRISE_WECHAT_AGENT_ID = "enterprise.wechat.agent.id";

    public static final String ENTERPRISE_WECHAT_USERS = "enterprise.wechat.users";

    public static final String HTML_HEADER_PREFIX = "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'><html><head><title>dolphinscheduler</title><meta name='Keywords' content=''><meta name='Description' content=''><style type=\"text/css\">table {margin-top:0px;padding-top:0px;border:1px solid;font-size: 14px;color: #333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}table th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;text-align: left;}table td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;text-align: left;}</style></head><body style=\"margin:0;padding:0\"><table border=\"1px\" cellpadding=\"5px\" cellspacing=\"-10px\"> ";

    public static final String TABLE_BODY_HTML_TAIL = "</table></body></html>";

    /**
     * plugin config
     */
    public static final String PLUGIN_DIR = "plugin.dir";

    public static final String PLUGIN_DEFAULT_EMAIL = "email";

    public static final String PLUGIN_DEFAULT_EMAIL_CH = "邮件";

    public static final String PLUGIN_DEFAULT_EMAIL_EN = "email";

    public static final String PLUGIN_DEFAULT_EMAIL_RECEIVERS = "receivers";

    public static final String PLUGIN_DEFAULT_EMAIL_RECEIVERCCS = "receiverCcs";

    public static final String RETMAP_MSG = "msg";
}
