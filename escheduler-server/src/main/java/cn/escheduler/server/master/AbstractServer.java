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
package cn.escheduler.server.master;

import cn.escheduler.common.IStoppable;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

/**
 *   master server
 */
@ComponentScan("cn.escheduler")
public abstract class AbstractServer implements CommandLineRunner, IStoppable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

    /**
     *  conf
     */
    protected static Configuration conf;

    /**
     *  object lock
     */
    protected final Object lock = new Object();

    /**
     * whether or not to close the state
     */
    protected boolean terminated = false;


    /**
     *  heartbeat interval, unit second
     */
    protected int heartBeatInterval;



    /**
     *  blocking implement
     * @throws InterruptedException
     */
    public void awaitTermination() throws InterruptedException {
        synchronized (lock) {
            while (!terminated) {
                lock.wait();
            }
        }
    }


    /**
     * Callback used to run the bean.
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public abstract void run(String... args) throws Exception;

    /**
     * gracefully stop
     * @param cause why stopping
     */
    @Override
    public abstract void stop(String cause);
}

