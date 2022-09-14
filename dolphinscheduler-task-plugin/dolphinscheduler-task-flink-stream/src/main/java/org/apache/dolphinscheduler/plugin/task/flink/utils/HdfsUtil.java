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

import org.apache.dolphinscheduler.plugin.task.flink.entity.CheckpointInfo;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class HdfsUtil {

    private static final Logger logger = LoggerFactory.getLogger(HdfsUtil.class);

    /**
     * find file path in dir
     *
     * @param fileSystem
     * @param path
     * @return
     */
    public static List<CheckpointInfo> listFiles(
                                                 FileSystem fileSystem, Path path, PathFilter filter) {
        List<CheckpointInfo> filePathsAndModifyTime = Lists.newArrayList();
        try {
            FileStatus[] fileIterator = fileSystem.listStatus(path, filter);
            /** file name order by ModificationTime desc */
            filePathsAndModifyTime =
                    Stream.of(fileIterator)
                            .filter(fileStatus -> isValidCheckpoint(fileSystem, fileStatus))
                            .sorted(
                                    (a, b) -> (int) (b.getModificationTime()
                                            - a.getModificationTime()))
                            .map(
                                    fileStatus -> new CheckpointInfo(
                                            fileStatus.getPath().toString(),
                                            fileStatus.getModificationTime()))
                            .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("list file error!", e);
        }
        return filePathsAndModifyTime;
    }

    private static boolean isValidCheckpoint(FileSystem fs, FileStatus fileStatus) {
        try {
            Path metadata = new Path(fileStatus.getPath(), "_metadata");
            FileStatus status = fs.getFileStatus(metadata);
            logger.info(
                    "Checkpoint dir {} has metadata, file length is:{}",
                    fileStatus.getPath(),
                    status.getLen());
            return true;
        } catch (Exception e) {
            logger.error(
                    "Cannot find metadata file in directory {}.Please try to load the checkpoint/savepoint directly from the metadata file instead of the directory.",
                    fileStatus.getPath());
            return false;
        }
    }
}
