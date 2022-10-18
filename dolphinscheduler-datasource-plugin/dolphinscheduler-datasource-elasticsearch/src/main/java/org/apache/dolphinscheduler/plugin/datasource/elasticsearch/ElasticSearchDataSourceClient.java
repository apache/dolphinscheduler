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

package org.apache.dolphinscheduler.plugin.datasource.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.cluster.Health;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;

public class ElasticSearchDataSourceClient implements DataSourceClient {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchDataSourceClient.class);
    protected final BaseConnectionParam baseConnectionParam;
    public ElasticSearchDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        this.baseConnectionParam = baseConnectionParam;
        preInit();
        checkClient();
    }

    protected void preInit() {
        logger.info("preInit in ElasticSearchDataSourceClient");
    }
    @Override
    public void checkClient() {
        String endpoint = baseConnectionParam.getAddress();
        String user = baseConnectionParam.getUser();
        String passwd = baseConnectionParam.getPassword();

        JestClientFactory factory = new JestClientFactory();
        HttpClientConfig.Builder httpClientConfig = new HttpClientConfig
                .Builder(endpoint);
        if (!(StringUtils.isBlank(user) || StringUtils.isBlank(passwd))) {
            httpClientConfig.defaultCredentials(user, passwd);
        }
        factory.setHttpClientConfig(httpClientConfig.build());
        JestClient jestClient = factory.getObject();
        Health health = new Health.Builder().build();
        try {
            JestResult result = jestClient.execute(health);
            if (result.toString().contains("unable to authenticate")) {
                logger.error("ElasticSearch connection failed, wrong username or password");
                logger.error(result.toString());
                throw new RuntimeException("Wrong username or password for ElasticSearch");
            }
            logger.info("ElasticSearch connected");
        } catch (IOException e) {
            logger.error("ElasticSearch connection failed");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public Connection getConnection() {
        return null;
    }
}
