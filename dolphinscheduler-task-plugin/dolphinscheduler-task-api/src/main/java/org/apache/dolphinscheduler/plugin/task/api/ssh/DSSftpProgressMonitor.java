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

package org.apache.dolphinscheduler.plugin.task.api.ssh;

import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Sftp progress monitor schedule thread
 */
public class DSSftpProgressMonitor implements SftpProgressMonitor, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DSSftpProgressMonitor.class);

    private static final int KB_UNIT = 1024;

    private final long totalSize;

    private boolean isScheduled = false;

    private long startTime;

    private long uploaded;

    private ScheduledExecutorService executorService;

    private final String fileName;

    public DSSftpProgressMonitor(long totalSize, String fileName) {
        this.totalSize = totalSize;
        this.fileName = fileName;
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        logger.info("Start to upload file:{} to remote:{}, total size:{}KB", fileName, dest, totalSize / KB_UNIT);
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean count(long count) {
        if (!isScheduled) {
            generateScheduleThread();
        }
        uploaded += count;
        logger.info("Upload {}KB, has uploaded {}KB, progress rate:{}", count / KB_UNIT, uploaded / KB_UNIT,
                getUploadProgress());
        return count > 0;
    }

    @Override
    public void end() {
    }

    @Override
    public void run() {
        logger.info("Has uploaded {} {}KB, upload progress rate: {}", fileName, uploaded / KB_UNIT,
                getUploadProgress());
        if (uploaded == totalSize) {
            destroyThread();
            long endTime = System.currentTimeMillis();
            logger.info("Upload {} finished. Take time:{} ms", fileName, endTime - startTime);
        }
    }

    private String getUploadProgress() {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(uploaded / totalSize);
    }

    private void generateScheduleThread() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(this, 1, 5, TimeUnit.SECONDS);
        isScheduled = true;
    }

    private void destroyThread() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
