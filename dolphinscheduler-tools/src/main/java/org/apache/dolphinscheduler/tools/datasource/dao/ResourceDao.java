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

package org.apache.dolphinscheduler.tools.datasource.dao;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * resource dao
 */
@Slf4j
public class ResourceDao {

    /**
     * list all resources by the type
     *
     * @param conn connection
     * @return map that key is full_name and value is the folder's size
     */
    private Map<String, Long> listAllResourcesByFileType(Connection conn, int type) {
        Map<String, Long> resourceSizeMap = new HashMap<>();

        String sql =
                String.format("SELECT full_name, type, size, is_directory FROM t_ds_resources where type = %d", type);
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("full_name");
                boolean isDirectory = rs.getBoolean("is_directory");
                long fileSize = rs.getLong("size");

                if (StringUtils.isNotBlank(fullName) && !isDirectory) {
                    String[] splits = fullName.split("/");
                    for (int i = 1; i < splits.length; i++) {
                        String parentFullName = Joiner.on("/").join(Arrays.copyOfRange(splits, 0, splits.length - i));
                        if (!Strings.isNullOrEmpty(parentFullName)) {
                            long size = resourceSizeMap.getOrDefault(parentFullName, 0L);
                            resourceSizeMap.put(parentFullName, size + fileSize);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
        return resourceSizeMap;
    }

    /**
     * update the folder's size
     *
     * @param conn connection
     */
    public void updateResourceFolderSizeByFileType(Connection conn, int type) {
        Map<String, Long> resourceSizeMap = listAllResourcesByFileType(conn, type);

        String sql = "UPDATE t_ds_resources SET size=? where type=? and full_name=? and is_directory = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Long> entry : resourceSizeMap.entrySet()) {
                pstmt.setLong(1, entry.getValue());
                pstmt.setInt(2, type);
                pstmt.setString(3, entry.getKey());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
    }

}
