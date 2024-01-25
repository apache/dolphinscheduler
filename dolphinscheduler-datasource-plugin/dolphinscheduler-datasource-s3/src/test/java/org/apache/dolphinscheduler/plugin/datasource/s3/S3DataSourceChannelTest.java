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

package org.apache.dolphinscheduler.plugin.datasource.s3;

import org.apache.dolphinscheduler.plugin.datasource.s3.param.S3ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class S3DataSourceChannelTest {

    @Test
    public void testCreateDataSourceClient() {
	  S3DataSourceChannel sourceChannel = Mockito.mock(S3DataSourceChannel.class);
	  S3PooledDataSourceClient dataSourceClient = Mockito.mock(S3PooledDataSourceClient.class);
	  Mockito.when(sourceChannel.createPooledDataSourceClient(Mockito.any(), Mockito.any())).thenReturn(dataSourceClient);
	  Assertions.assertNotNull(sourceChannel.createPooledDataSourceClient(new S3ConnectionParam(), DbType.S3));
    }
}
