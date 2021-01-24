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

package org.apache.dolphinscheduler.plugin.alert.email.template;

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.plugin.alert.email.EmailConstants;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * the default html alert message template
 */
public class DefaultHTMLTemplate implements AlertTemplate {

    public static final Logger logger = LoggerFactory.getLogger(DefaultHTMLTemplate.class);

    @Override
    public String getMessageFromTemplate(String content, ShowType showType, boolean showAll) {

        switch (showType) {
            case TABLE:
                return getTableTypeMessage(content, showAll);
            case TEXT:
                return getTextTypeMessage(content);
            default:
                throw new IllegalArgumentException(String.format("not support showType: %s in DefaultHTMLTemplate", showType));
        }
    }

    /**
     * get alert message which type is TABLE
     *
     * @param content message content
     * @param showAll weather to show all
     * @return alert message
     */
    private String getTableTypeMessage(String content, boolean showAll) {

        if (StringUtils.isNotEmpty(content)) {
            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);

            if (!showAll && mapItemsList.size() > EmailConstants.NUMBER_1000) {
                mapItemsList = mapItemsList.subList(0, EmailConstants.NUMBER_1000);
            }

            StringBuilder contents = new StringBuilder(200);

            boolean flag = true;

            String title = "";
            for (LinkedHashMap<String, Object> mapItems : mapItemsList) {

                Set<Map.Entry<String, Object>> entries = mapItems.entrySet();

                Iterator<Map.Entry<String, Object>> iterator = entries.iterator();

                StringBuilder t = new StringBuilder(EmailConstants.TR);
                StringBuilder cs = new StringBuilder(EmailConstants.TR);
                while (iterator.hasNext()) {

                    Map.Entry<String, Object> entry = iterator.next();
                    t.append(EmailConstants.TH).append(entry.getKey()).append(EmailConstants.TH_END);
                    cs.append(EmailConstants.TD).append(String.valueOf(entry.getValue())).append(EmailConstants.TD_END);

                }
                t.append(EmailConstants.TR_END);
                cs.append(EmailConstants.TR_END);
                if (flag) {
                    title = t.toString();
                }
                flag = false;
                contents.append(cs);
            }

            return getMessageFromHtmlTemplate(title, contents.toString());
        }

        return content;
    }

    /**
     * get alert message which type is TEXT
     *
     * @param content message content
     * @return alert message
     */
    private String getTextTypeMessage(String content) {

        if (StringUtils.isNotEmpty(content)) {
            ArrayNode list = JSONUtils.parseArray(content);
            StringBuilder contents = new StringBuilder(100);
            for (JsonNode jsonNode : list) {
                contents.append(EmailConstants.TR);
                contents.append(EmailConstants.TD).append(jsonNode.toString()).append(EmailConstants.TD_END);
                contents.append(EmailConstants.TR_END);
            }

            return getMessageFromHtmlTemplate(null, contents.toString());

        }

        return content;
    }

    /**
     * get alert message from a html template
     *
     * @param title message title
     * @param content message content
     * @return alert message which use html template
     */
    private String getMessageFromHtmlTemplate(String title, String content) {

        requireNonNull(content, "content must not null");
        String htmlTableThead = StringUtils.isEmpty(title) ? "" : String.format("<thead>%s</thead>%n", title);

        return EmailConstants.HTML_HEADER_PREFIX + htmlTableThead + content + EmailConstants.TABLE_BODY_HTML_TAIL;
    }

}
