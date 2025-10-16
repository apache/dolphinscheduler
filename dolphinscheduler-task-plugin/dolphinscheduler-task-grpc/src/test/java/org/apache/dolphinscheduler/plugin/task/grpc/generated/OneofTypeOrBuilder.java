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

public interface OneofTypeOrBuilder
        extends
            // @@protoc_insertion_point(interface_extends:org.apache.dolphinscheduler.task.grpc.proto.OneofType)
            com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string name = 1;</code>
     * @return The name.
     */
    java.lang.String getName();
    /**
     * <code>string name = 1;</code>
     * @return The bytes for name.
     */
    com.google.protobuf.ByteString getNameBytes();

    /**
     * <code>optional string bio = 2;</code>
     * @return Whether the bio field is set.
     */
    boolean hasBio();
    /**
     * <code>optional string bio = 2;</code>
     * @return The bio.
     */
    java.lang.String getBio();
    /**
     * <code>optional string bio = 2;</code>
     * @return The bytes for bio.
     */
    com.google.protobuf.ByteString getBioBytes();

    /**
     * <code>optional int32 age = 3;</code>
     * @return Whether the age field is set.
     */
    boolean hasAge();
    /**
     * <code>optional int32 age = 3;</code>
     * @return The age.
     */
    int getAge();

    /**
     * <code>string phone = 4;</code>
     * @return Whether the phone field is set.
     */
    boolean hasPhone();
    /**
     * <code>string phone = 4;</code>
     * @return The phone.
     */
    java.lang.String getPhone();
    /**
     * <code>string phone = 4;</code>
     * @return The bytes for phone.
     */
    com.google.protobuf.ByteString getPhoneBytes();

    /**
     * <code>string email = 5;</code>
     * @return Whether the email field is set.
     */
    boolean hasEmail();
    /**
     * <code>string email = 5;</code>
     * @return The email.
     */
    java.lang.String getEmail();
    /**
     * <code>string email = 5;</code>
     * @return The bytes for email.
     */
    com.google.protobuf.ByteString getEmailBytes();

    public org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.ContactCase getContactCase();
}
