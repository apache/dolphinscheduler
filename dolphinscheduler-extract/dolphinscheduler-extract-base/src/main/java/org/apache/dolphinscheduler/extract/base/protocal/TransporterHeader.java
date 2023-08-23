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

import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

@Data
public class TransporterHeader implements Serializable {

    private static final long serialVersionUID = -1L;

    private static final AtomicLong REQUEST_ID = new AtomicLong(1);

    private String methodIdentifier;
    private long opaque;

    // Used for JsonDeSerializer
    public TransporterHeader() {

    }

    public TransporterHeader(String methodIdentifier) {
        this(REQUEST_ID.getAndIncrement(), methodIdentifier);
    }

    public TransporterHeader(long opaque, String methodIdentifier) {
        this.opaque = opaque;
        this.methodIdentifier = methodIdentifier;
    }

    public static TransporterHeader of(String methodIdentifier) {
        return new TransporterHeader(methodIdentifier);
    }

    public static TransporterHeader of(long opaque, String methodIdentifier) {
        return new TransporterHeader(opaque, methodIdentifier);
    }

    public byte[] toBytes() {
        return JsonSerializer.serialize(this);
    }

}
