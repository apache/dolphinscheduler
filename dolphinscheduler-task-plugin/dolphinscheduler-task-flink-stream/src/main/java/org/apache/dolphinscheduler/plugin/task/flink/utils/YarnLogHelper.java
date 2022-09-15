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

package org.apache.dolphinscheduler.plugin.task.flink.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.util.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.logaggregation.AggregatedLogFormat;
import org.apache.hadoop.yarn.logaggregation.LogAggregationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * reference for yarn LogCLIHelpers
 *
 */
public class YarnLogHelper {

    private static final Logger logger = LoggerFactory.getLogger(YarnLogHelper.class);

    public static String printAllContainersLogsReturnFilePath(
                                                              YarnConfiguration configuration, String finishedJobLogDir,
                                                              String applicationId) throws IOException {
        Path remoteRootLogDir =
                new Path(
                        configuration.get(
                                YarnConfiguration.NM_REMOTE_APP_LOG_DIR,
                                YarnConfiguration.DEFAULT_NM_REMOTE_APP_LOG_DIR));
        ApplicationId appId = ApplicationId.fromString(applicationId);
        // mkdir if not exist
        FileUtils.forceMkdir(new File(finishedJobLogDir));
        String logFilePath = finishedJobLogDir + "/" + applicationId + ".log";
        logger.info("finished job log file is:{} ", logFilePath);
        File localLogFile = new File(logFilePath);
        if (localLogFile.exists() && localLogFile.isFile() && localLogFile.length() > 0) {
            logger.info("yarn log exist in local, log file path is:{}", localLogFile);
            return logFilePath;
        }

        String hadoopUser = UserGroupInformation.getCurrentUser().getShortUserName();
        String logDirSuffix = LogAggregationUtils.getRemoteNodeLogDirSuffix(configuration);

        logger.info("Current Hadoop/Kerberos user: {}", hadoopUser);
        Path remoteAppLogDir =
                LogAggregationUtils.getRemoteAppLogDir(
                        remoteRootLogDir, appId, hadoopUser, logDirSuffix);

        long logFileSize = getLogFileSize(configuration, remoteAppLogDir.toString());
        Preconditions.checkArgument(logFileSize > 0, "log file size =0");

        // hdfs log file exist and create file and print stream
        FileUtils.touch(localLogFile);
        FileOutputStream fileOutputStream = new FileOutputStream(logFilePath);

        try (PrintStream printStream = new PrintStream(fileOutputStream, true)) {
            RemoteIterator<FileStatus> nodeFiles = null;
            try {
                Path qualifiedLogDir =
                        FileContext.getFileContext(configuration).makeQualified(remoteAppLogDir);
                nodeFiles =
                        FileContext.getFileContext(qualifiedLogDir.toUri(), configuration)
                                .listStatus(remoteAppLogDir);
            } catch (FileNotFoundException fnf) {
                logDirNotExist(remoteAppLogDir.toString(), printStream);
            }

            boolean foundAnyLogs = false;

            while (nodeFiles != null && nodeFiles.hasNext()) {
                FileStatus thisNodeFile = nodeFiles.next();
                if (!thisNodeFile
                        .getPath()
                        .getName()
                        .endsWith(LogAggregationUtils.TMP_FILE_SUFFIX)) {
                    AggregatedLogFormat.LogReader reader =
                            new AggregatedLogFormat.LogReader(
                                    configuration, thisNodeFile.getPath());
                    try {
                        DataInputStream valueStream;
                        AggregatedLogFormat.LogKey key = new AggregatedLogFormat.LogKey();
                        valueStream = reader.next(key);

                        while (valueStream != null) {
                            String containerString =
                                    "\n\nContainer: "
                                            + key
                                            + " on "
                                            + thisNodeFile.getPath().getName();
                            printStream.println(containerString);
                            printStream.println(StringUtils.repeat("=", containerString.length()));

                            while (true) {
                                try {
                                    AggregatedLogFormat.LogReader.readAContainerLogsForALogType(
                                            valueStream,
                                            printStream,
                                            thisNodeFile.getModificationTime());
                                    foundAnyLogs = true;
                                } catch (EOFException eof) {
                                    break;
                                }
                            }
                            // Next container
                            key = new AggregatedLogFormat.LogKey();
                            valueStream = reader.next(key);
                        }
                    } finally {
                        reader.close();
                    }
                }
            }
            if (!foundAnyLogs) {
                emptyLogDir(remoteAppLogDir.toString(), printStream);
            }
        }
        return logFilePath;
    }

    private static long getLogFileSize(Configuration yarnConfiguration, String tableLocation) throws IOException {
        Path inputPath = new Path(tableLocation);
        Configuration conf = new JobConf(yarnConfiguration);
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] fsStatus = fs.listStatus(inputPath);

        long fileSize = Arrays.stream(fsStatus).mapToLong(FileStatus::getLen).sum();
        logger.info("tableLocation:{} ,fileSize:{}", tableLocation, fileSize);
        return fileSize;
    }

    private static void logDirNotExist(String remoteAppLogDir, PrintStream printStream) {
        logger.info(remoteAppLogDir + " does not exist.");
        logger.info("Log aggregation has not completed or is not enabled.");

        printStream.println(remoteAppLogDir + " does not exist.");
        printStream.println("Log aggregation has not completed or is not enabled.");
    }

    private static void emptyLogDir(String remoteAppLogDir, PrintStream printStream) {
        logger.info(remoteAppLogDir + " does not have any log files.");
        printStream.println(remoteAppLogDir + " does not have any log files.");
    }
}
