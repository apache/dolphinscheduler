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

public interface MapTypeOrBuilder
        extends
            // @@protoc_insertion_point(interface_extends:org.apache.dolphinscheduler.task.grpc.proto.MapType)
            com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    int getBoughtCount();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    boolean containsBought(
                           java.lang.String key);
    /**
     * Use {@link #getBoughtMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBought();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBoughtMap();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrDefault(
                                                                                     java.lang.String key,
                                                                                     org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits defaultValue);
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrThrow(
                                                                                   java.lang.String key);
    /**
     * Use {@link #getBoughtValueMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.Integer> getBoughtValue();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    java.util.Map<java.lang.String, java.lang.Integer> getBoughtValueMap();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */

    int getBoughtValueOrDefault(
                                java.lang.String key,
                                int defaultValue);
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */

    int getBoughtValueOrThrow(
                              java.lang.String key);

    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    int getCashCount();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    boolean containsCash(
                         java.lang.String key);
    /**
     * Use {@link #getCashMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCash();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCashMap();
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */

    org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrDefault(
                                                                                      java.lang.String key,
                                                                                      org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only defaultValue);
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */

    org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrThrow(
                                                                                    java.lang.String key);
}
