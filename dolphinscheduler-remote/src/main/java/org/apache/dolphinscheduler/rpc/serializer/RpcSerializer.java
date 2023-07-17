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

package org.apache.dolphinscheduler.rpc.serializer;

import java.util.HashMap;

public enum RpcSerializer {

    PROTOSTUFF((byte) 1, new ProtoStuffSerializer());

    byte type;

    Serializer serializer;

    RpcSerializer(byte type, Serializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public byte getType() {
        return type;
    }

    private static HashMap<Byte, Serializer> SERIALIZERS_MAP = new HashMap<>();

    static {
        for (RpcSerializer rpcSerializer : RpcSerializer.values()) {
            SERIALIZERS_MAP.put(rpcSerializer.type, rpcSerializer.serializer);
        }
    }

    public static Serializer getSerializerByType(byte type) {
        return SERIALIZERS_MAP.get(type);
    }
}
