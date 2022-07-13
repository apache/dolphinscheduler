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

package org.apache.dolphinscheduler.server.logger;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.server.log.LoggerServer;

import javax.annotation.PostConstruct;

/**
 * logger server adapter
 */
public class AdaptedLoggerServer implements IStoppable {

    private final LoggerServer loggerServer;

    public AdaptedLoggerServer() {
        this.loggerServer = new LoggerServer();
    }

    @PostConstruct
    public void start() {
        this.loggerServer.start();
    }


    @Override
    public void stop(String cause) {
        this.loggerServer.stop();
    }
}
