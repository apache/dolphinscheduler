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

package org.apache.dolphinscheduler.remote.command;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;

/**
 * db task ack request command
 */
public class CacheExpireCommand implements Serializable {

    private CacheType cacheType;
    private String cacheKey;

    public CacheExpireCommand() {
        super();
    }

    public CacheExpireCommand(CacheType cacheType, String cacheKey) {
        this.cacheType = cacheType;
        this.cacheKey = cacheKey;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * package command
     *
     * @return command
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.CACHE_EXPIRE);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return String.format("CacheExpireCommand{CacheType=%s, cacheKey=%s}", cacheType, cacheKey);
    }
}
