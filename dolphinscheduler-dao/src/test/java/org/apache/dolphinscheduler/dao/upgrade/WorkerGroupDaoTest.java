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
package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.dao.DaoConfiguration;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
@SpringBootTest(classes = DaoConfiguration.class)
@ExtendWith(MockitoExtension.class)
@SpringBootApplication(scanBasePackageClasses = DaoConfiguration.class)
public class WorkerGroupDaoTest {
    @Autowired
    protected DataSource dataSource;

    @Test
    public void testQueryQueryAllOldWorkerGroupException() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
                    WorkerGroupDao workerGroupDao = new WorkerGroupDao();
                    workerGroupDao.queryAllOldWorkerGroup(null);
                });

    }

}
