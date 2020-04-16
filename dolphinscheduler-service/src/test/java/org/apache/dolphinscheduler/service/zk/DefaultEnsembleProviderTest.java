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
package org.apache.dolphinscheduler.service.zk;

import org.apache.curator.ensemble.EnsembleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DefaultEnsembleProviderTest {
    private static final String DEFAULT_SERVER_LIST = "localhost:2181";

    @Test
    public void startAndClose() {
        EnsembleProvider ensembleProvider = new DefaultEnsembleProvider(DEFAULT_SERVER_LIST);
        try {
            ensembleProvider.start();
        } catch (Exception e) {
            Assert.fail("EnsembleProvider start error: " + e.getMessage());
        }
        try {
            ensembleProvider.close();
        } catch (IOException e) {
            Assert.fail("EnsembleProvider close error: " + e.getMessage());
        }
    }

    @Test
    public void getConnectionString() {
        EnsembleProvider ensembleProvider = new DefaultEnsembleProvider(DEFAULT_SERVER_LIST);
        Assert.assertEquals(DEFAULT_SERVER_LIST, ensembleProvider.getConnectionString());
    }

    @Test
    public void setConnectionString() {
        EnsembleProvider ensembleProvider = new DefaultEnsembleProvider(DEFAULT_SERVER_LIST);
        ensembleProvider.setConnectionString("otherHost:2181");
        Assert.assertEquals(DEFAULT_SERVER_LIST, ensembleProvider.getConnectionString());
    }

    @Test
    public void updateServerListEnabled() {
        EnsembleProvider ensembleProvider = new DefaultEnsembleProvider(DEFAULT_SERVER_LIST);
        Assert.assertFalse(ensembleProvider.updateServerListEnabled());
    }
}