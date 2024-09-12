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

package org.apache.dolphinscheduler.spi.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {

    public static final String COMMON_PROPERTIES_PATH = "/common.properties";

    public static final String REMOTE_LOGGING_YAML_PATH = "/remote-logging.yaml";
    public static final String AWS_YAML_PATH = "/aws.yaml";

    public static final String FORMAT_S_S = "%s/%s";

    public static final String FOLDER_SEPARATOR = "/";

    public static final String RESOURCE_TYPE_FILE = "resources";

    public static final String EMPTY_STRING = "";

    /**
     * sudo enable
     */
    public static final String SUDO_ENABLE = "sudo.enable";

    /**
     * comma ,
     */
    public static final String COMMA = ",";

    public static final String SLASH = "/";

    public static final String RESOURCE_STORAGE_TYPE = "resource.storage.type";

    public static final String DEVELOPMENT_STATE = "development.state";

    /**
     * COLON :
     */
    public static final String COLON = ":";

    /**
     * SPACE " "
     */
    public static final String SPACE = " ";

    /**
     * DOUBLE_SLASH //
     */
    public static final String DOUBLE_SLASH = "//";

    /**
     * http connect time out
     */
    public static final int HTTP_CONNECT_TIMEOUT = 60 * 1000;

    /**
     * sleep 1000ms
     */
    public static final long SLEEP_TIME_MILLIS = 1_000L;

    /**
     * underline  "_"
     */
    public static final String UNDERLINE = "_";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT = "{{";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT = "}}";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT_SPACE = "{ {";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT_SPACE = "} }";

    /**
     * system line separator
     */
    public static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * exec shell scripts
     */
    public static final String SH = "sh";

    /**
     * pstree, get pud and sub pid
     */
    public static final String PSTREE = "pstree";

    /**
     * spi constants
     */
    /** alert plugin param field string **/
    public static final String STRING_PLUGIN_PARAM_FIELD = "field";
    /** alert plugin param name string **/
    public static final String STRING_PLUGIN_PARAM_NAME = "name";
    /** alert plugin param props string **/
    public static final String STRING_PLUGIN_PARAM_PROPS = "props";
    /** alert plugin param type string **/
    public static final String STRING_PLUGIN_PARAM_TYPE = "type";
    /** alert plugin param title string **/
    public static final String STRING_PLUGIN_PARAM_TITLE = "title";
    /** alert plugin param value string **/
    public static final String STRING_PLUGIN_PARAM_VALUE = "value";
    /** alert plugin param validate string **/
    public static final String STRING_PLUGIN_PARAM_VALIDATE = "validate";
    /** alert plugin param options string **/
    public static final String STRING_PLUGIN_PARAM_OPTIONS = "options";
    /**plugin param emit string **/
    public static final String STRING_PLUGIN_PARAM_EMIT = "emit";

    /** string true */
    public static final String STRING_TRUE = "true";
    /** string false */
    public static final String STRING_FALSE = "false";
    /** string yes */
    public static final String STRING_YES = "YES";
    /** string no */
    public static final String STRING_NO = "NO";

    public static final String SMALL = "small";

    public static final String CHANGE = "change";

    public static final String AT_SIGN = "@";

    public static final String SEMICOLON = ";";

    public static final String FORMAT_S_S_COLON = "%s:%s";

    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    /**
     * java.security.krb5.conf.path
     */
    public static final String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE =
            "hadoop.security.authentication.startup.state";

    public static final String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";

    /**
     * loginUserFromKeytab path
     */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    public static final String KERBEROS = "kerberos";

}
