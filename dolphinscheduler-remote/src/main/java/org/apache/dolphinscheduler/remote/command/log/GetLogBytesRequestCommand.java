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

package org.apache.dolphinscheduler.remote.command.log;

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  get log bytes request command
 */
public class GetLogBytesRequestCommand implements Serializable {

    private static final AtomicLong REQUEST = new AtomicLong(1);

    private String path;

    public GetLogBytesRequestCommand() {
    }

    public GetLogBytesRequestCommand(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return
     */
    public Command convert2Command(){
        Command command = new Command(REQUEST.getAndIncrement());
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
