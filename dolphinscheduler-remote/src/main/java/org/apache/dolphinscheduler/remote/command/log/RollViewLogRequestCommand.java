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

/**
 *  roll view log request command
 */
public class RollViewLogRequestCommand implements Serializable {

    /**
     *  log path
     */
    private String path;

    /**
     *  skip line number
     */
    private int skipLineNum;

    /**
     *  query line number
     */
    private int limit;

    public RollViewLogRequestCommand() {
    }

    public RollViewLogRequestCommand(String path, int skipLineNum, int limit) {
        this.path = path;
        this.skipLineNum = skipLineNum;
        this.limit = limit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSkipLineNum() {
        return skipLineNum;
    }

    public void setSkipLineNum(int skipLineNum) {
        this.skipLineNum = skipLineNum;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * package request command
     *
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        command.setType(CommandType.ROLL_VIEW_LOG_REQUEST);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
