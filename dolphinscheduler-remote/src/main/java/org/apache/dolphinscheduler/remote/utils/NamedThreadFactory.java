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
package org.apache.dolphinscheduler.remote.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  thread factory
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger increment = new AtomicInteger(1);

    /**
     *  name
     */
    private final String name;

    /**
     *  count
     */
    private final int count;

    public NamedThreadFactory(String name){
        this(name, 0);
    }

    public NamedThreadFactory(String name, int count){
        this.name = name;
        this.count = count;
    }

    /**
     *  create thread
     * @param r runnable
     * @return thread
     */
    @Override
    public Thread newThread(Runnable r) {
        final String threadName = count > 0 ? String.format("%s_%d_%d", name, count, increment.getAndIncrement())
                : String.format("%s_%d", name, increment.getAndIncrement());
        Thread t = new Thread(r, threadName);
        t.setDaemon(true);
        return t;
    }
}
