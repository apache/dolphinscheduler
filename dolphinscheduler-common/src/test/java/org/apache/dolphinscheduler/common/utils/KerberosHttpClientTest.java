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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KerberosHttpClient  test
 */
public class KerberosHttpClientTest {
    public static final Logger logger = LoggerFactory.getLogger(KerberosHttpClientTest.class);
    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();

    @Test
    public void get() {
        try {
            String applicationUrl = hadoopUtils.getApplicationUrl("application_1542010131334_0029");
            String responseContent;
            KerberosHttpClient kerberosHttpClient = new KerberosHttpClient(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH), PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH), true);
            responseContent = kerberosHttpClient.get(applicationUrl,
                    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME));
            Assert.assertNull(responseContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}