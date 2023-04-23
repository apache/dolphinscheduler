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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleDao {

    /**
     * queryAllSchedule
     *
     * @param conn jdbc connection
     * @return Schedule List
     */
    public Map<Integer, Long> queryAllSchedule(Connection conn) {
        Map<Integer, Long> scheduleMap = new HashMap<>();
        String sql = "SELECT id,process_definition_code FROM t_ds_schedules";
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Integer id = rs.getInt(1);
                long processDefinitionCode = rs.getLong(2);
                scheduleMap.put(id, processDefinitionCode);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
        return scheduleMap;
    }

    /**
     * update schedule
     *
     * @param conn jdbc connection
     * @param scheduleMap scheduleMap
     * @param processIdCodeMap processIdCodeMap
     */
    public void updateScheduleCode(Connection conn, Map<Integer, Long> scheduleMap,
                                   Map<Integer, Long> processIdCodeMap) {
        String sql = "UPDATE t_ds_schedules SET process_definition_code=?,timezone_id=?,environment_code=-1 where id=?";
        try {
            Clock clock = Clock.systemDefaultZone();
            String timezoneId = clock.getZone().getId();
            for (Map.Entry<Integer, Long> entry : scheduleMap.entrySet()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    long projectDefinitionCode = entry.getValue();
                    if (String.valueOf(projectDefinitionCode).length() <= 10) {
                        Integer projectDefinitionId = Integer.parseInt(String.valueOf(projectDefinitionCode));
                        if (processIdCodeMap.containsKey(projectDefinitionId)) {
                            projectDefinitionCode = processIdCodeMap.get(projectDefinitionId);
                        }
                    }
                    pstmt.setLong(1, projectDefinitionCode);
                    pstmt.setString(2, timezoneId);
                    pstmt.setInt(3, entry.getKey());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
    }
}
