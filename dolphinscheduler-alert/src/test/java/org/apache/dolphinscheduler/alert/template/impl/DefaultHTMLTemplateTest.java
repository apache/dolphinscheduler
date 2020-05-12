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
package org.apache.dolphinscheduler.alert.template.impl;

import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * test class for DefaultHTMLTemplate
 */
public class DefaultHTMLTemplateTest{

    private static final Logger logger = LoggerFactory.getLogger(DefaultHTMLTemplateTest.class);

    /**
     * only need test method GetMessageFromTemplate
     */
    @Test
    public void testGetMessageFromTemplate(){

        DefaultHTMLTemplate template = new DefaultHTMLTemplate();

        String tableTypeMessage = template.getMessageFromTemplate(list2String(), ShowType.TABLE,true);

        assertEquals(tableTypeMessage,generateMockTableTypeResultByHand());

        String textTypeMessage = template.getMessageFromTemplate(list2String(), ShowType.TEXT,true);

        assertEquals(textTypeMessage,generateMockTextTypeResultByHand());
    }

    /**
     * generate some simulation data
     */
    private String list2String(){

        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name","mysql200");
        map1.put("mysql address","192.168.xx.xx");
        map1.put("port","3306");
        map1.put("no index of number","80");
        map1.put("database client connections","190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name","mysql210");
        map2.put("mysql address","192.168.xx.xx");
        map2.put("port","3306");
        map2.put("no index of number","10");
        map2.put("database client connections","90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0,map1);
        maps.add(1,map2);
        String mapjson = JSONUtils.toJsonString(maps);
        logger.info(mapjson);

        return mapjson;
    }

    private String generateMockTableTypeResultByHand(){

        return Constants.HTML_HEADER_PREFIX +
                "<thead><tr><th>mysql service name</th><th>mysql address</th><th>port</th><th>no index of number</th><th>database client connections</th></tr></thead>\n" +
                "<tr><td>mysql200</td><td>192.168.xx.xx</td><td>3306</td><td>80</td><td>190</td></tr><tr><td>mysql210</td><td>192.168.xx.xx</td><td>3306</td><td>10</td><td>90</td></tr>" + Constants.TABLE_BODY_HTML_TAIL;

    }

    private String generateMockTextTypeResultByHand(){

        return Constants.HTML_HEADER_PREFIX + "<tr><td>{\"mysql service name\":\"mysql200\",\"mysql address\":\"192.168.xx.xx\",\"database client connections\":\"190\",\"port\":\"3306\",\"no index of number\":\"80\"}</td></tr><tr><td>{\"mysql service name\":\"mysql210\",\"mysql address\":\"192.168.xx.xx\",\"database client connections\":\"90\",\"port\":\"3306\",\"no index of number\":\"10\"}</td></tr>" + Constants.TABLE_BODY_HTML_TAIL;
    }
}
