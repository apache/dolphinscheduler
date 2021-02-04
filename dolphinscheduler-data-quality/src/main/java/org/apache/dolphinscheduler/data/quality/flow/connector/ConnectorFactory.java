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
import org.apache.dolphinscheduler.data.quality.enums.ConnectorType;

import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

/**
 * ConnectorFactory
 */
public class ConnectorFactory {

    private static class Singleton {
        static ConnectorFactory instance = new ConnectorFactory();
    }

    public static ConnectorFactory getInstance() {
        return Singleton.instance;
    }

    public List<IConnector> getConnectors(DataQualityContext context) throws Exception {

        List<IConnector> connectorList = new ArrayList<>();

        for (ConnectorParameter connectorParameter :context.getConnectorParameterList()) {
            IConnector connector = getConnector(context.getSparkSession(), connectorParameter);
            if (connector != null) {
                connectorList.add(connector);
            }
        }

        return connectorList;
    }

    private IConnector getConnector(SparkSession sparkSession,ConnectorParameter connectorParameter) throws Exception {
        ConnectorType connectorType = ConnectorType.getType(connectorParameter.getType());
        if (connectorType != null) {
            switch (connectorType) {
                case HIVE:
                    return new HiveConnector(sparkSession, connectorParameter);
                case JDBC:
                    return new JdbcConnector(sparkSession, connectorParameter);
                default:
                    throw new Exception("connector type ${connectorType} is not supported!");
            }
        }

        return null;
    }

}
