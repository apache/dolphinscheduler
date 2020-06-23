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
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.io.Serializable;

/**
 *  view log request command
 */
public class ViewLogRequestCommand implements Serializable {

    /**
     *  log path
     */
    private String path;

    public ViewLogRequestCommand() {
    }

    public ViewLogRequestCommand(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * package request command
     *
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        byte[] body = JsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
