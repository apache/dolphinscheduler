
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
package cn.escheduler.server.worker.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 *  custom task logger
 */
public class TaskLogger implements Logger {

  private static Logger logger = LoggerFactory.getLogger(TaskLogger.class);

  private String taskAppId;

  public TaskLogger(String taskAppId) {
    this.taskAppId = taskAppId;
  }

  private String addJobId(String msg) {
    return String.format("[taskAppId=%s] %s", taskAppId, msg);
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    logger.trace(addJobId(msg));
  }

  @Override
  public void trace(String format, Object arg) {
    logger.trace(addJobId(format), arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    logger.trace(addJobId(format), arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    logger.trace(addJobId(format), arguments);
  }

  @Override
  public void trace(String msg, Throwable t) {
    logger.trace(addJobId(msg), t);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logger.isTraceEnabled(marker);
  }

  @Override
  public void trace(Marker marker, String msg) {
    logger.trace(marker, addJobId(msg));
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    logger.trace(marker, addJobId(format), arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    logger.trace(marker, addJobId(format), arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    logger.trace(marker, addJobId(format), argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    logger.trace(marker, addJobId(msg), t);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(String msg) {
    logger.debug(addJobId(msg));
  }

  @Override
  public void debug(String format, Object arg) {
    logger.debug(addJobId(format), arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    logger.debug(addJobId(format), arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    logger.debug(addJobId(format), arguments);
  }

  @Override
  public void debug(String msg, Throwable t) {
    logger.debug(addJobId(msg), t);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(Marker marker, String msg) {
    logger.debug(marker, addJobId(msg));
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    logger.debug(marker, addJobId(format), arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    logger.debug(marker, addJobId(format), arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    logger.debug(marker, addJobId(format), arguments);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    logger.debug(marker, addJobId(msg), t);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    logger.info(addJobId(msg));
  }

  @Override
  public void info(String format, Object arg) {
    logger.info(addJobId(format), arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    logger.info(addJobId(format), arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    logger.info(addJobId(format), arguments);
  }

  @Override
  public void info(String msg, Throwable t) {
    logger.info(addJobId(msg), t);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(Marker marker, String msg) {
    logger.info(marker, addJobId(msg));
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    logger.info(marker, addJobId(format), arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    logger.info(marker, addJobId(format), arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    logger.info(marker, addJobId(format), arguments);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    logger.info(marker, addJobId(msg), t);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String msg) {
    logger.warn(addJobId(msg));
  }

  @Override
  public void warn(String format, Object arg) {
    logger.warn(addJobId(format), arg);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    logger.warn(addJobId(format), arg1, arg2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    logger.warn(addJobId(format), arguments);
  }

  @Override
  public void warn(String msg, Throwable t) {
    logger.warn(addJobId(msg), t);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(Marker marker, String msg) {
    logger.warn(marker, addJobId(msg));
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    logger.warn(marker, addJobId(format), arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    logger.warn(marker, addJobId(format), arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    logger.warn(marker, addJobId(format), arguments);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    logger.warn(marker, addJobId(msg), t);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    logger.error(addJobId(msg));
  }

  @Override
  public void error(String format, Object arg) {
    logger.error(addJobId(format), arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    logger.error(addJobId(format), arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    logger.error(addJobId(format), arguments);
  }

  @Override
  public void error(String msg, Throwable t) {
    logger.error(addJobId(msg), t);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(Marker marker, String msg) {
    logger.error(marker, addJobId(msg));
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    logger.error(marker, addJobId(format), arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    logger.error(marker, addJobId(format), arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    logger.error(marker, addJobId(format), arguments);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    logger.error(marker, addJobId(msg), t);
  }
}
