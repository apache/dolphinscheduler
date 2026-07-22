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

package org.apache.dolphinscheduler.plugin.task.api.model;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Contains some test case related to Property.
 */
public class JSONUtilsTest {

    @Test
    public void createArrayNodeTest() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str =
                "[{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"},{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}]";
        JsonNode jsonNode = JSONUtils.toJsonNode(property);

        ArrayNode arrayNode = JSONUtils.createArrayNode();
        ArrayList<JsonNode> objects = new ArrayList<>();
        objects.add(jsonNode);
        objects.add(jsonNode);

        ArrayNode jsonNodes = arrayNode.addAll(objects);
        String s = JSONUtils.toJsonString(jsonNodes);
        Assertions.assertEquals(s, str);

    }

    @Test
    public void toJsonNodeTest() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str = "{\"prop\":\"ds\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}";

        JsonNode jsonNodes = JSONUtils.toJsonNode(property);
        String s = JSONUtils.toJsonString(jsonNodes);
        Assertions.assertEquals(s, str);

    }

    @Test
    public void convert2Property() {
        Property property = new Property();
        property.setProp("ds");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("sssssss");
        String str = "{\"direct\":\"IN\",\"prop\":\"ds\",\"type\":\"VARCHAR\",\"value\":\"sssssss\"}";
        Property property1 = JSONUtils.parseObject(str, Property.class);
        Direct direct = property1.getDirect();
        Assertions.assertEquals(Direct.IN, direct);
    }
}
