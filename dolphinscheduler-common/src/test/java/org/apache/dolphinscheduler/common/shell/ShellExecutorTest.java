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
package org.apache.dolphinscheduler.common.shell;

import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * SHELL Taks Test
 */
public class ShellExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(ShellExecutorTest.class);

    @Test
    public void execCommand() throws InterruptedException {

        ThreadPoolExecutors executors = ThreadPoolExecutors.getInstance();
        CountDownLatch latch = new CountDownLatch(200);

        executors.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    int i =0;
                    while(i++ <= 100){
                        String res = ShellExecutor.execCommand("groups");
                        logger.info("time:" + i + ",thread id:" + Thread.currentThread().getId() + ", result:" + res.substring(0,5));
                        Thread.sleep(100l);
                        latch.countDown();
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executors.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    int i =0;
                    while(i++ <= 100){
                        String res = ShellExecutor.execCommand("whoami");
                        logger.info("time:" + i + ",thread id:" + Thread.currentThread().getId() + ", result2:" + res);
                        Thread.sleep(100l);
                        latch.countDown();
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        latch.await();
    }
}