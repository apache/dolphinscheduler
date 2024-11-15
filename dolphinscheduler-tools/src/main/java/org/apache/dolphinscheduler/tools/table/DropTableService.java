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

package org.apache.dolphinscheduler.tools.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DropTableService {

    @Autowired
    private DataSource dataSource;

    public void dropTableOnce() {
        List<String> dropTableDDLList = getDropTableDDL();

        for (String dropTableDDL : dropTableDDLList) {
            try {
                try (Connection connection = dataSource.getConnection()) {
                    try (PreparedStatement pstmt = connection.prepareStatement(dropTableDDL)) {
                        pstmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                log.error("Failed to execute sql: {},", dropTableDDL, e);
            }
        }
    }

    private List<String> getDropTableDDL() {
        List<String> dropTableDDLList = new ArrayList<>();
        dropTableDDLList.add("DROP TABLE IF EXISTS t_ds_dq_comparison_type;");
        dropTableDDLList.add("DROP TABLE IF EXISTS t_ds_dq_rule_execute_sql;");
        dropTableDDLList.add("DROP TABLE IF EXISTS t_ds_dq_rule_input_entry;");
        dropTableDDLList.add("DROP TABLE IF EXISTS t_ds_dq_task_statistics_value;");
        return dropTableDDLList;
    }

}
