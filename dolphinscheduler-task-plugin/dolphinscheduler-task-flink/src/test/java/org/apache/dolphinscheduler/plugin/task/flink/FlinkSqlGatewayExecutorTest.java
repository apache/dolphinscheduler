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

package org.apache.dolphinscheduler.plugin.task.flink;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkSqlGatewayExecutorTest {

    @Test
    public void splitSqlShouldUseDefaultSemicolonWhenSeparatorIsBlank() {
        List<String> sqlList = FlinkSqlGatewayExecutor.splitSql("SET a=1;SELECT 2", " ");
        Assertions.assertEquals(2, sqlList.size());
        Assertions.assertEquals("SET a=1", sqlList.get(0));
        Assertions.assertEquals("SELECT 2", sqlList.get(1));
    }

    @Test
    public void splitSqlShouldSupportCustomSeparator() {
        List<String> sqlList = FlinkSqlGatewayExecutor.splitSql("SET a=1@@SELECT 2", "@@");
        Assertions.assertEquals(2, sqlList.size());
        Assertions.assertEquals("SET a=1", sqlList.get(0));
        Assertions.assertEquals("SELECT 2", sqlList.get(1));
    }

    @Test
    public void splitSqlShouldEscapeRegexSpecialCharactersInSeparator() {
        List<String> sqlList = FlinkSqlGatewayExecutor.splitSql("SET a=1|SELECT 2", "|");
        Assertions.assertEquals(2, sqlList.size());
        Assertions.assertEquals("SET a=1", sqlList.get(0));
        Assertions.assertEquals("SELECT 2", sqlList.get(1));
    }

    @Test
    public void stripLeadingCommentsAndBlanksShouldRemoveLeadingCommentAndBlankLines() {
        String sql = FlinkSqlGatewayExecutor.stripLeadingCommentsAndBlanks(
                "-- job comment\n\n-- another comment\nSET pipeline.name='demo'");
        Assertions.assertEquals("SET pipeline.name='demo'", sql);
    }

    @Test
    public void stripLeadingCommentsAndBlanksShouldKeepStatementWithoutComments() {
        String sql = FlinkSqlGatewayExecutor.stripLeadingCommentsAndBlanks("SELECT 1");
        Assertions.assertEquals("SELECT 1", sql);
    }

    @Test
    public void stripLeadingCommentsAndBlanksShouldReturnEmptyWhenOnlyComments() {
        Assertions.assertEquals("", FlinkSqlGatewayExecutor.stripLeadingCommentsAndBlanks("-- comment only\n\n"));
        Assertions.assertEquals("", FlinkSqlGatewayExecutor.stripLeadingCommentsAndBlanks(null));
    }

    @Test
    public void stripLeadingCommentsAndBlanksShouldKeepInlineCommentInsideStatement() {
        String sql = FlinkSqlGatewayExecutor.stripLeadingCommentsAndBlanks("SELECT 1 -- trailing comment");
        Assertions.assertEquals("SELECT 1 -- trailing comment", sql);
    }
}
