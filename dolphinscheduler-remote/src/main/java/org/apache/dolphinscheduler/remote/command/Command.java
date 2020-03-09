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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  receive task log request command and content fill
 *  for netty data serializable transfer
 */
public class Command implements Serializable {

    private static final AtomicLong REQUEST_ID = new AtomicLong(1);

    public static final byte MAGIC = (byte) 0xbabe;

    public Command(){
        this.opaque = REQUEST_ID.getAndIncrement();
    }

    public Command(long opaque){
        this.opaque = opaque;
    }

    /**
     * command type
     */
    private CommandType type;

    /**
     *  request unique identification
     */
    private long opaque;

    /**
     *  data body
     */
    private byte[] body;

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (opaque ^ (opaque >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Command other = (Command) obj;
        return opaque == other.opaque;
    }

    @Override
    public String toString() {
        return "Command [type=" + type + ", opaque=" + opaque + ", bodyLen=" + (body == null ? 0 : body.length) + "]";
    }

}
