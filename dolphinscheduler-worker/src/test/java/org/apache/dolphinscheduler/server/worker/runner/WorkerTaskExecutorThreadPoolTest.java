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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.TaskExecuteThreadsFullPolicy;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkerTaskExecutorThreadPoolTest {

    @Test
    public void testIsOverload() {
        WorkerConfig workerConfig = new WorkerConfig();
        workerConfig.setExecThreads(1);
        workerConfig.setTaskExecuteThreadsFullPolicy(TaskExecuteThreadsFullPolicy.CONTINUE);
        WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool = new WorkerTaskExecutorThreadPool(workerConfig);
        // submit 100 task, the thread pool size is 1
        // assert the overload should be true
        // assert the submitQueue should be 99
        for (int i = 0; i < 100; i++) {
            boolean submitResult =
                    workerTaskExecutorThreadPool.submitWorkerTaskExecutor(new MockWorkerTaskExecutor(() -> {
                        try {
                            Thread.sleep(10_000L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Assertions.assertTrue(submitResult);
        }
        Assertions.assertTrue(workerTaskExecutorThreadPool.isOverload());
        Assertions.assertEquals(99, workerTaskExecutorThreadPool.getWaitingTaskExecutorSize());
        Assertions.assertEquals(1, workerTaskExecutorThreadPool.getRunningTaskExecutorSize());
    }

    static class MockWorkerTaskExecutor extends WorkerTaskExecutor {

        private final Runnable runnable;

        protected MockWorkerTaskExecutor(Runnable runnable) {
            super(TaskExecutionContext.builder().taskInstanceId((int) System.nanoTime()).build(), new WorkerConfig(),
                    new WorkerMessageSender(), new StorageOperate() {

                        @Override
                        public void createTenantDirIfNotExists(String tenantCode) {

                        }

                        @Override
                        public String getResDir(String tenantCode) {
                            return null;
                        }

                        @Override
                        public String getUdfDir(String tenantCode) {
                            return null;
                        }

                        @Override
                        public boolean mkdir(String tenantCode, String path) throws IOException {
                            return false;
                        }

                        @Override
                        public String getResourceFullName(String tenantCode, String fileName) {
                            return null;
                        }

                        @Override
                        public String getResourceFileName(String tenantCode, String fullName) {
                            return null;
                        }

                        @Override
                        public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
                            return null;
                        }

                        @Override
                        public boolean exists(String fullName) {
                            return false;
                        }

                        @Override
                        public boolean delete(String filePath, boolean recursive) {
                            return false;
                        }

                        @Override
                        public boolean delete(String filePath, List<String> childrenPathArray,
                                              boolean recursive) {
                            return false;
                        }

                        @Override
                        public boolean copy(String srcPath, String dstPath, boolean deleteSource,
                                            boolean overwrite) {
                            return false;
                        }

                        @Override
                        public String getDir(ResourceType resourceType, String tenantCode) {
                            return null;
                        }

                        @Override
                        public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                                              boolean overwrite) {
                            return false;
                        }

                        @Override
                        public void download(String srcFilePath, String dstFile, boolean overwrite) {

                        }

                        @Override
                        public List<String> vimFile(String tenantCode, String filePath, int skipLineNums,
                                                    int limit) {
                            return null;
                        }

                        @Override
                        public void deleteTenant(String tenantCode) {

                        }

                        @Override
                        public ResUploadType returnStorageType() {
                            return null;
                        }

                        @Override
                        public List<StorageEntity> listFilesStatusRecursively(String path, String defaultPath,
                                                                              String tenantCode, ResourceType type) {
                            return null;
                        }

                        @Override
                        public List<StorageEntity> listFilesStatus(String path, String defaultPath, String tenantCode,
                                                                   ResourceType type) throws Exception {
                            return null;
                        }

                        @Override
                        public StorageEntity getFileStatus(String path, String defaultPath, String tenantCode,
                                                           ResourceType type) throws Exception {
                            return null;
                        }
                    }, new WorkerRegistryClient());
            this.runnable = runnable;
        }

        @Override
        public void run() {
            executeTask(new TaskCallbackImpl(null, null));
        }

        @Override
        protected void executeTask(TaskCallBack taskCallBack) {
            runnable.run();
        }
    }

}
