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

package org.apache.dolphinscheduler.plugin.alert.ewechat;

public final class EWeChatAlertConstants {
    private EWeChatAlertConstants() {
        throw new IllegalStateException(EWeChatAlertConstants.class.getName());
    }

    static final String COLON = "：";

    static final String SUCCESS_FLAG = "success";

    static final String MARKDOWN_QUOTE = ">";

    static final String MARKDOWN_ENTER = "\n";

    static final String CHARSET = "UTF-8";

    static final String FIRST_DASH = "-----------------------------------------------------------------\n";

    static final String DASH_BREAK = "-----------------------------------------------------------------\n";

    static final String BOLD_PATTERN = "**%s**";

    static final String INFO_PATTERN = "# <font color=\"info\">%s</font>%n";

    static final String ERROR_PATTERN = "# `%s`%n";

    static final String COMMENT_PATTERN = "<font color=\"comment\">%s</font>%n";

}
