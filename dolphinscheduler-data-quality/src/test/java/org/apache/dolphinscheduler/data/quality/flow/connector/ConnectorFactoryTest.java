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

package org.apache.dolphinscheduler.data.quality.flow.connector;

import org.apache.dolphinscheduler.data.quality.configuration.ConnectorParameter;
import org.apache.dolphinscheduler.data.quality.context.DataQualityContext;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.data.quality.Constants.*;
import static org.apache.dolphinscheduler.data.quality.Constants.DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.PASSWORD;

/**
 * ConnectorFactoryTest
 */
public class ConnectorFactoryTest {

    @Test
    public void testConnectorGenerate() {

        DataQualityContext context = new DataQualityContext();
        List<ConnectorParameter> connectorParameters = new ArrayList<>();
        ConnectorParameter connectorParameter = new ConnectorParameter();
        connectorParameter.setType("JDBC");
        Map<String,Object> config = new HashMap<>();
        config.put(DATABASE,"test");
        config.put(TABLE,"test1");
        config.put(URL,"jdbc:mysql://localhost:3306/test");
        config.put(USER,"test");
        config.put(PASSWORD,"123456");
        config.put(DRIVER,"com.mysql.jdbc.Driver");
        connectorParameter.setConfig(config);
        connectorParameter.setConfig(null);
        connectorParameters.add(connectorParameter);
        context.setConnectorParameterList(connectorParameters);

        int flag = 0;
        try {
            List<IConnector> connectors = ConnectorFactory.getInstance().getConnectors(context);
            if(connectors != null && connectors.size() >= 1){
                flag = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1,flag);
    }
}
