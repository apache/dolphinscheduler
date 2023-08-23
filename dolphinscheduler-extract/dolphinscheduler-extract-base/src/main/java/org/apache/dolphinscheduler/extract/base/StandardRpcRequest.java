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

package org.apache.dolphinscheduler.extract.base;

import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardRpcRequest implements IRpcRequest {

    private byte[][] args;

    private Class<?>[] argsTypes;

    public static StandardRpcRequest of(Object[] args) {
        if (args == null || args.length == 0) {
            return new StandardRpcRequest(null, null);
        }
        final byte[][] argsBytes = new byte[args.length][];
        final Class<?>[] argsTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argsBytes[i] = JsonSerializer.serialize(args[i]);
            argsTypes[i] = args[i].getClass();
        }
        return new StandardRpcRequest(argsBytes, argsTypes);
    }

}
