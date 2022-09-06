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

public final class EmailConstants {

    public static final String XLS_FILE_PATH = "xls.file.path";

    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    public static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";

    public static final int NUMBER_1000 = 1000;

    public static final String TR = "<tr>";

    public static final String TD = "<td>";

    public static final String TD_END = "</td>";

    public static final String TR_END = "</tr>";

    public static final String TH = "<th>";

    public static final String TH_END = "</th>";

    public static final String HTML_HEADER_PREFIX =
            "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>"
                    + "<html>"
                    + "<head>"
                    + "<title>dolphinscheduler</title>"
                    + "<meta name='Keywords' content=''>"
                    + "<meta name='Description' content=''>"
                    + "<style type=\"text/css\">"
                    + "table {margin-top:0px;padding-top:0px;border:1px solid;font-size: 14px;color: #333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}"
                    + "table th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;text-align: left;}"
                    + "table td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;text-align: left;}"
                    + "</style>"
                    + "</head>"
                    + "<body style=\"margin:0;padding:0\"><table border=\"1px\" cellpadding=\"5px\" cellspacing=\"-10px\"> ";

    public static final String TABLE_BODY_HTML_TAIL = "</table></body></html>";

    public static final String UTF_8 = "UTF-8";

    public static final String EXCEL_SUFFIX_XLSX = ".xlsx";

    public static final String SINGLE_SLASH = "/";

    private EmailConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
