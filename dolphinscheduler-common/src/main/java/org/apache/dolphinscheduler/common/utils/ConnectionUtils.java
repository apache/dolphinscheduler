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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ConnectionUtils {

	public static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

	private static ConnectionUtils instance;

	ConnectionUtils() {
	}

	public static ConnectionUtils getInstance() {
		if (null == instance) {
			syncInit();
		}
		return instance;
	}

	private static synchronized void syncInit() {
		if (instance == null) {
			instance = new ConnectionUtils();
		}
	}

	public void release(ResultSet rs, Statement stmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
				throw new RuntimeException(e);
			} finally {
				try {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				} catch (SQLException e) {
					logger.error(e.getMessage(),e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void releaseResource(ResultSet rs, PreparedStatement ps, Connection conn) {
		ConnectionUtils.getInstance().release(rs,ps,conn);
		if (null != rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
			}
		}

		if (null != ps) {
			try {
				ps.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
			}
		}

		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
}
