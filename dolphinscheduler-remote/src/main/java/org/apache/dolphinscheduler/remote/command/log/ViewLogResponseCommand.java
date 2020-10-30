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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;

import java.io.Serializable;

/**
 *  view log response command
 */
public class ViewLogResponseCommand implements Serializable {

    /**
     *  response data
     */
    private String msg;

    public ViewLogResponseCommand() {
    }

    public ViewLogResponseCommand(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * package response command
     *
     * @param opaque request unique identification
     * @return command
     */
    public Command convert2Command(long opaque) {
        Command command = new Command(opaque);
        command.setType(CommandType.VIEW_WHOLE_LOG_RESPONSE);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }
}
