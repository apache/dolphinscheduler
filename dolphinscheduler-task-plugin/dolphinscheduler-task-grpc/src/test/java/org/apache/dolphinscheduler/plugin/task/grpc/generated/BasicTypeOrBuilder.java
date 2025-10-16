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

package org.apache.dolphinscheduler.plugin.task.grpc.generated;

public interface BasicTypeOrBuilder
        extends
            // @@protoc_insertion_point(interface_extends:org.apache.dolphinscheduler.task.grpc.proto.BasicType)
            com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string stringVal = 1;</code>
     * @return The stringVal.
     */
    java.lang.String getStringVal();
    /**
     * <code>string stringVal = 1;</code>
     * @return The bytes for stringVal.
     */
    com.google.protobuf.ByteString getStringValBytes();

    /**
     * <code>bool boolVal = 2;</code>
     * @return The boolVal.
     */
    boolean getBoolVal();

    /**
     * <code>float floatVal = 3;</code>
     * @return The floatVal.
     */
    float getFloatVal();

    /**
     * <code>double doubleVal = 4;</code>
     * @return The doubleVal.
     */
    double getDoubleVal();

    /**
     * <code>int32 int32Val = 5;</code>
     * @return The int32Val.
     */
    int getInt32Val();

    /**
     * <code>int64 int64Val = 6;</code>
     * @return The int64Val.
     */
    long getInt64Val();

    /**
     * <code>uint32 uint32Val = 7;</code>
     * @return The uint32Val.
     */
    int getUint32Val();

    /**
     * <code>uint64 uint64Val = 8;</code>
     * @return The uint64Val.
     */
    long getUint64Val();

    /**
     * <code>sint32 sint32Val = 9;</code>
     * @return The sint32Val.
     */
    int getSint32Val();

    /**
     * <code>sint64 sint64Val = 10;</code>
     * @return The sint64Val.
     */
    long getSint64Val();

    /**
     * <code>sfixed32 sfixed32 = 11;</code>
     * @return The sfixed32.
     */
    int getSfixed32();

    /**
     * <code>sfixed64 sfixed64 = 12;</code>
     * @return The sfixed64.
     */
    long getSfixed64();

    /**
     * <code>bytes bytesVal = 13;</code>
     * @return The bytesVal.
     */
    com.google.protobuf.ByteString getBytesVal();
}
