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
 * Pong return after ping
 */
public class Pong implements Serializable {

    /**
     *  pong body
     */
    protected static final ByteBuf EMPTY_BODY = Unpooled.EMPTY_BUFFER;

    /**
     *  pong command body
     */
    private static final byte[] EMPTY_BODY_ARRAY = new byte[0];

    /**
     *  ping byte buffer
     */
    private static final ByteBuf PONG_BUF;

    static {
        ByteBuf ping = Unpooled.buffer();
        ping.writeByte(Command.MAGIC);
        ping.writeByte(CommandType.PONG.ordinal());
        ping.writeLong(0);
        ping.writeInt(0);
        ping.writeBytes(EMPTY_BODY);
        PONG_BUF = Unpooled.unreleasableBuffer(ping).asReadOnly();
    }

    /**
     *  ping content
     * @return result
     */
    public static ByteBuf pingContent(){
        return PONG_BUF.duplicate();
    }

    /**
     * package pong command
     *
     * @param opaque request unique identification
     * @return command
     */
    public static Command create(long opaque){
        Command command = new Command(opaque);
        command.setType(CommandType.PONG);
        command.setBody(EMPTY_BODY_ARRAY);
        return command;
    }
}
