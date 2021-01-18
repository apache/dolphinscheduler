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

import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread Pool Executor Test
 */
public class ThreadPoolExecutorsTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutors.class);


    @Test
    public void testThreadPoolExecutors() throws InterruptedException {

        Thread2[] threadArr = new Thread2[10];
        for (int i = 0; i < threadArr.length; i++) {

            threadArr[i] = new Thread2();
            threadArr[i].setDaemon(false);
            threadArr[i].start();
        }

        Thread.currentThread().join(40000l);
    }


    //test thread
    class Thread2 extends Thread {
        @Override
        public void run() {
            logger.info("ThreadPoolExecutors instance's hashcode is: {} ",ThreadPoolExecutors.getInstance("a",2).hashCode());
        }
    }


}
