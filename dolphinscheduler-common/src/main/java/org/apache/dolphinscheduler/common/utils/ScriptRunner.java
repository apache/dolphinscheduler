/*
 * Copyright 2004-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Tool to run database scripts
 */
@Slf4j
public class ScriptRunner {

    private static final String DEFAULT_DELIMITER = ";";

    private final Connection connection;

    private final boolean stopOnError;
    private final boolean autoCommit;

    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

    public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader - the source of the script
     * @throws IOException errors
     * @throws SQLException errors
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException | SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the connection
     * passed in
     *
     * @param conn - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
        List<String> command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new ArrayList<>();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    log.info("\n{}", trimmedLine);
                } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.startsWith("delimiter")) {
                    String newDelimiter = trimmedLine.split(" ")[1];
                    this.setDelimiter(newDelimiter, fullLineDelimiter);

                } else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
                    command.add(line.substring(0, line.lastIndexOf(getDelimiter())));
                    log.info("\n{}", String.join("\n", command));

                    try (Statement statement = conn.createStatement()) {
                        statement.execute(String.join(" ", command));
                        try (ResultSet rs = statement.getResultSet()) {
                            if (stopOnError && rs != null) {
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                for (int i = 1; i < cols; i++) {
                                    String name = md.getColumnLabel(i);
                                    log.info("{} \t", name);
                                }
                                log.info("");
                                while (rs.next()) {
                                    for (int i = 1; i < cols; i++) {
                                        String value = rs.getString(i);
                                        log.info("{} \t", value);
                                    }
                                    log.info("");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                        throw e;
                    }

                    command = null;
                    Thread.yield();
                } else {
                    command.add(line);
                }
            }

        } catch (SQLException e) {
            log.error("Error executing: {}", command);
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            log.error("Error executing: {}", command);
            throw e;
        }
    }

    private String getDelimiter() {
        return delimiter;
    }

}
