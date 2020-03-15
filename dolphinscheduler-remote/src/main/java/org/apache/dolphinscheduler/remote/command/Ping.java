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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

/**
 *  ping machine
 */
public class Ping implements Serializable {

    /**
     *  ping body
     */
    protected static final ByteBuf EMPTY_BODY = Unpooled.EMPTY_BUFFER;

    /**
     *  request command body
     */
    private static final byte[] EMPTY_BODY_ARRAY = new byte[0];

    private static final ByteBuf PING_BUF;

    static {
        ByteBuf ping = Unpooled.buffer();
        ping.writeByte(Command.MAGIC);
        ping.writeByte(CommandType.PING.ordinal());
        ping.writeLong(0);
        ping.writeInt(0);
        ping.writeBytes(EMPTY_BODY);
        PING_BUF = Unpooled.unreleasableBuffer(ping).asReadOnly();
    }

    /**
     *  ping content
     * @return result
     */
    public static ByteBuf pingContent(){
        return PING_BUF.duplicate();
    }

    /**
     *  create ping command
     *
     * @return command
     */
    public static Command create(){
        Command command = new Command();
        command.setType(CommandType.PING);
        command.setBody(EMPTY_BODY_ARRAY);
        return command;
    }
}
