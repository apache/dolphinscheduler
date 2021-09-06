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

package org.apache.dolphinscheduler.spi.utils;

/**
 * constants
 */
public class Constants {
    private Constants() {
        throw new IllegalStateException("Constants class");
    }

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


    /** string true */
    public static final String STRING_TRUE = "true";
    /** string false */
    public static final String STRING_FALSE = "false";
    /** string yes */
    public static final String STRING_YES = "YES";
    /** string no */
    public static final String STRING_NO = "NO";

    /**
     * common properties path
     */
    public static final String COMMON_PROPERTIES_PATH = "/common.properties";

    /**
     * date format of yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * date format of yyyyMMddHHmmssSSS
     */
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

}
