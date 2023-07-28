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

package org.apache.dolphinscheduler.plugin.datasource.spark;

import org.apache.dolphinscheduler.plugin.datasource.hive.HivePooledDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

<<<<<<< HEAD:dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-spark/src/main/java/org/apache/dolphinscheduler/plugin/datasource/spark/SparkDataSourceClient.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkDataSourceClient extends HiveDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(SparkDataSourceClient.class);

    public SparkDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
=======
public class SparkPooledDataSourceClient extends HivePooledDataSourceClient {

    public SparkPooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
>>>>>>> 4aab0b234 (Use AdHoc datasource client in sqlTask (#14631)):dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-spark/src/main/java/org/apache/dolphinscheduler/plugin/datasource/spark/SparkPooledDataSourceClient.java
        super(baseConnectionParam, dbType);
    }

}
