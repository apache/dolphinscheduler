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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.*;

/*
 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 */
/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Tool to run database scripts
 */
public class ScriptRunner {

	public static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

	private static final String DEFAULT_DELIMITER = ";";

	private Connection connection;

	private boolean stopOnError;
	private boolean autoCommit;

	private String delimiter = DEFAULT_DELIMITER;
	private boolean fullLineDelimiter = false;

	/**
	 * Default constructor
	 */
	public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
		this.connection = connection;
		this.autoCommit = autoCommit;
		this.stopOnError = stopOnError;
	}

	public static void main(String[] args) {
		String dbName = "db_mmu";
		String appKey = dbName.substring(dbName.lastIndexOf("_")+1, dbName.length());
		System.out.println(appKey);
	}

	public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
		this.delimiter = delimiter;
		this.fullLineDelimiter = fullLineDelimiter;
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter)
	 *
	 * @param reader
	 *            - the source of the script
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
		} catch (IOException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error running script.  Cause: " + e, e);
		}
	}

	public void runScript(Reader reader, String dbName) throws IOException, SQLException {
		try {
			boolean originalAutoCommit = connection.getAutoCommit();
			try {
				if (originalAutoCommit != this.autoCommit) {
					connection.setAutoCommit(this.autoCommit);
				}
				runScript(connection, reader, dbName);
			} finally {
				connection.setAutoCommit(originalAutoCommit);
			}
		} catch (IOException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error running script.  Cause: " + e, e);
		}
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter) using the connection
	 * passed in
	 *
	 * @param conn
	 *            - the connection to use for the script
	 * @param reader
	 *            - the source of the script
	 * @throws SQLException
	 *             if any SQL errors occur
	 * @throws IOException
	 *             if there is an error reading from the Reader
	 */
	@SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
		String sql = "";
		StringBuffer command = null;
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					logger.info(trimmedLine);
				} else if (trimmedLine.startsWith("delimiter")) {
					String newDelimiter = trimmedLine.split(" ")[1];
					this.setDelimiter(newDelimiter, fullLineDelimiter);

				} else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
					command.append(" ");
					Statement statement = conn.createStatement();

					// logger.info(command.toString());
					sql = command.toString();
					command = null;
					yieldWithLog(sql, statement);
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			logger.error("Error executing: " + (command == null ? "null" : command.toString()));
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: " +  (command == null ? "null" : command.toString()));
			throw e;
		}
	}
	@SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	private void runScript(Connection conn, Reader reader , String dbName) throws IOException, SQLException {
		StringBuffer command = null;
		String sql = "";
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					logger.info(trimmedLine);
				} else if (trimmedLine.startsWith("delimiter")) {
					String newDelimiter = trimmedLine.split(" ")[1];
					this.setDelimiter(newDelimiter, fullLineDelimiter);

				} else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
					command.append(" ");
					Statement statement = conn.createStatement();

					// logger.info(command.toString());

					sql = command.toString().replaceAll("\\{\\{APPDB\\}\\}", dbName);
					yieldWithLog(sql, statement);
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			logger.error("Error executing: " + sql);
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: " + sql);
			throw e;
		}
	}

	private void yieldWithLog(String sql, Statement statement) throws SQLException {
		boolean hasResults = false;
		logger.info("sql:" + sql);
		if (stopOnError) {
			hasResults = statement.execute(sql);
		} else {
			try {
				statement.execute(sql);
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
		}

		ResultSet rs = statement.getResultSet();
		if (hasResults && rs != null) {
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			for (int i = 0; i < cols; i++) {
				logger.info(md.getColumnLabel(i) + "\t");
			}
			logger.info("");
			while (rs.next()) {
				for (int i = 1; i <= cols; i++) {
					logger.info(rs.getString(i) + "\t");
				}
				logger.info("");
			}
		}

		try {
			statement.close();
		} catch (Exception e) {
			logger.warn("close failed");
			// Ignore to workaround a bug in Jakarta DBCP
		}
		Thread.yield();
	}

	private String getDelimiter() {
		return delimiter;
	}

}
