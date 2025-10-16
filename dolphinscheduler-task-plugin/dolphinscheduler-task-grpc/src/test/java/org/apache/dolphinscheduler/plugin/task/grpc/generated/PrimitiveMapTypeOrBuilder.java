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

public interface PrimitiveMapTypeOrBuilder
        extends
            // @@protoc_insertion_point(interface_extends:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType)
            com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> getBoughtList();
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getBought(int index);
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    int getBoughtCount();
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    java.util.List<? extends org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder> getBoughtOrBuilderList();
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder getBoughtOrBuilder(
                                                                                                                        int index);
}
