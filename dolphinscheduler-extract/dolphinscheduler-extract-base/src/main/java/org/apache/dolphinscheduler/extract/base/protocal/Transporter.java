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

package org.apache.dolphinscheduler.extract.base.protocal;

import org.apache.dolphinscheduler.extract.base.StandardRpcRequest;
import org.apache.dolphinscheduler.extract.base.StandardRpcResponse;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import java.io.Serializable;

import lombok.Data;
import lombok.NonNull;

@Data
public class Transporter implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final byte MAGIC = (byte) 0xbabe;
    public static final byte VERSION = 0;

    private TransporterHeader header;
    private byte[] body;

    public static Transporter of(@NonNull TransporterHeader header, StandardRpcResponse iRpcResponse) {
        return of(header, JsonSerializer.serialize(iRpcResponse));
    }

    public static Transporter of(@NonNull TransporterHeader header, StandardRpcRequest iRpcRequest) {
        return of(header, JsonSerializer.serialize(iRpcRequest));
    }

    public static Transporter of(@NonNull TransporterHeader header, byte[] body) {
        Transporter transporter = new Transporter();
        transporter.setHeader(header);
        transporter.setBody(body);
        return transporter;
    }

}
