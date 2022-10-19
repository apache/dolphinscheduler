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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
public class ProcessDefinitionDaoTest {

    @Autowired
    private DataSource dataSource;
    final ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();

    @Test
    public void testQueryAllProcessDefinition() {
        // Map<Integer, String> processDefinitionJsonMap =
        // processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());
        // assertThat(processDefinitionJsonMap.size(),greaterThanOrEqualTo(0));
    }

    @Test
    public void testUpdateProcessDefinitionJson() {
        Map<Integer, String> processDefinitionJsonMap = new HashMap<>();
        processDefinitionJsonMap.put(1, "test");
        // processDefinitionDao.updateProcessDefinitionJson(dataSource.getConnection(),processDefinitionJsonMap);
    }

    @Test
    public void testQueryAllProcessDefinitionException() {
        // processDefinitionDao.queryAllProcessDefinition(null);
    }

    @Test
    public void testUpdateProcessDefinitionJsonException() {
        Assertions.assertThrows(Exception.class, () -> processDefinitionDao.updateProcessDefinitionJson(null, null));
    }
}
