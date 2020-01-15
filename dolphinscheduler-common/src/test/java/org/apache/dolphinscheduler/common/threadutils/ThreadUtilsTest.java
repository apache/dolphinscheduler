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
package org.apache.dolphinscheduler.common.threadutils;

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.*;

import static org.junit.Assert.*;


public class ThreadUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadUtilsTest.class);
    /**
     * create a naming thread
     */
    @Test
    public void testNewDaemonFixedThreadExecutor() {
        // create core size and max size are all 3
        ExecutorService testExec = ThreadUtils.newDaemonFixedThreadExecutor("test-exec-thread",10);

        for (int i = 0; i < 19; i++) {
            final int index = i;
            testExec.submit(() -> {
                System.out.println("do some work index " + index);
            });
        }
        assertFalse(testExec.isShutdown());
        testExec.shutdownNow();
        assertTrue(testExec.isShutdown());

    }

    /**
     * test schedulerThreadExecutor as for print time in scheduler
     * default check thread is 1
     */
    @Test
    public void testNewDaemonScheduleThreadExecutor() {

        ScheduledExecutorService scheduleService = ThreadUtils.newDaemonThreadScheduledExecutor("scheduler-thread", 1);
        Calendar start = Calendar.getInstance();
        Calendar globalTimer = Calendar.getInstance();
        globalTimer.set(2019, Calendar.DECEMBER, 1, 0, 0, 0);
        // current
        Calendar end = Calendar.getInstance();
        end.set(2019, Calendar.DECEMBER, 1, 0, 0, 3);
        Runnable schedulerTask = new Runnable() {
            @Override
            public void run() {
                start.set(2019, Calendar.DECEMBER, 1, 0, 0, 0);
                int index = 0;
                // send heart beat work
                while (start.getTime().getTime() <= end.getTime().getTime()) {
                    System.out.println("worker here");
                    System.out.println(index ++);
                    start.add(Calendar.SECOND,  1);
                    globalTimer.add(Calendar.SECOND, 1);
                }
                System.out.println("time is " + System.currentTimeMillis());
            }
        };
        scheduleService.scheduleAtFixedRate(schedulerTask, 2, 10, TimeUnit.SECONDS);
        assertFalse(scheduleService.isShutdown());
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduleService.shutdownNow();
        assertTrue(scheduleService.isShutdown());
    }

    /**
     * test stopper is working normal
     */
    @Test
    public void testStopper() {
        assertTrue(Stopper.isRunning());
        Stopper.stop();
        assertTrue(Stopper.isStopped());
    }

    /**
     * test threadPoolExecutors with 3 workers and current each 5 tasks
     * @throws InterruptedException
     */
    @Test
    public void testThreadInfo() throws InterruptedException {
        ThreadPoolExecutors workers = ThreadPoolExecutors.getInstance("worker", 3);
        for (int i = 0; i < 5; ++i ) {
            int index = i;
            workers.execute(() -> {
                for (int j = 0; j < 10; ++j) {
                    try {
                        Thread.sleep(1000);
                        System.out.printf("worker %d is doing the task", index);
                        System.out.println();
                        workers.printStatus();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
           });
            workers.submit(() -> {
                for (int j = 0; j < 10; ++j) {
                    try {
                        Thread.sleep(1000);
                        System.out.printf("worker_2 %d is doing the task", index);
                        System.out.println();
                        workers.printStatus();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        Thread.sleep(50001);
        workers.shutdown();
    }

    /**
     * test a single daemon thread pool
     */
    @Test
    public void  testNewDaemonSingleThreadExecutor() {
        ExecutorService threadTest = ThreadUtils.newDaemonSingleThreadExecutor("thread_test");
        threadTest.execute(() -> {
            for (int i = 0; i < 100; ++i) {
                System.out.println("daemon working ");
            }

        });
        assertFalse(threadTest.isShutdown());
        threadTest.shutdownNow();
        assertTrue(threadTest.isShutdown());
    }

    @Test
    public void testNewDaemonCachedThreadPool() {

        ThreadPoolExecutor threadPoolExecutor = ThreadUtils.newDaemonCachedThreadPool("threadTest-");
        Thread thread1 = threadPoolExecutor.getThreadFactory().newThread(() -> {
            for (int i = 0; i < 10; ++i) {
                System.out.println("this task is with index " + i );
            }
        });
        assertTrue(thread1.getName().startsWith("threadTest-"));
        assertFalse(threadPoolExecutor.isShutdown());
        threadPoolExecutor.shutdown();
        assertTrue(threadPoolExecutor.isShutdown());
    }

    @Test
    public void testNewDaemonCachedThreadPoolWithThreadNumber() {
        ThreadPoolExecutor threadPoolExecutor = ThreadUtils.newDaemonCachedThreadPool("threadTest--", 3, 10);
        for (int i = 0; i < 10; ++ i) {
            threadPoolExecutor.getThreadFactory().newThread(() -> {
                assertEquals(3, threadPoolExecutor.getActiveCount());
                System.out.println("this task is first work to do");
            });
        }
        assertFalse(threadPoolExecutor.isShutdown());
        threadPoolExecutor.shutdown();
        assertTrue(threadPoolExecutor.isShutdown());
    }



}
