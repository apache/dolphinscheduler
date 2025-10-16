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

/**
 * Protobuf enum {@code org.apache.dolphinscheduler.task.grpc.proto.Fruits}
 */
public enum Fruits
        implements
            com.google.protobuf.ProtocolMessageEnum {

    /**
     * <code>LINGO = 0;</code>
     */
    LINGO(0),
    /**
     * <code>ICHIGO = 1;</code>
     */
    ICHIGO(1),
    /**
     * <code>MOMO = 2;</code>
     */
    MOMO(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>LINGO = 0;</code>
     */
    public static final int LINGO_VALUE = 0;
    /**
     * <code>ICHIGO = 1;</code>
     */
    public static final int ICHIGO_VALUE = 1;
    /**
     * <code>MOMO = 2;</code>
     */
    public static final int MOMO_VALUE = 2;

    public final int getNumber() {
        if (this == UNRECOGNIZED) {
            throw new java.lang.IllegalArgumentException(
                    "Can't get the number of an unknown enum value.");
        }
        return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static Fruits valueOf(int value) {
        return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static Fruits forNumber(int value) {
        switch (value) {
            case 0:
                return LINGO;
            case 1:
                return ICHIGO;
            case 2:
                return MOMO;
            default:
                return null;
        }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Fruits> internalGetValueMap() {
        return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<Fruits> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<Fruits>() {

                public Fruits findValueByNumber(int number) {
                    return Fruits.forNumber(number);
                }
            };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
        if (this == UNRECOGNIZED) {
            throw new java.lang.IllegalStateException(
                    "Can't get the descriptor of an unrecognized enum value.");
        }
        return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
        return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.getDescriptor().getEnumTypes()
                .get(0);
    }

    private static final Fruits[] VALUES = values();

    public static Fruits valueOf(
                                 com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != getDescriptor()) {
            throw new java.lang.IllegalArgumentException(
                    "EnumValueDescriptor is not for this type.");
        }
        if (desc.getIndex() == -1) {
            return UNRECOGNIZED;
        }
        return VALUES[desc.getIndex()];
    }

    private final int value;

    private Fruits(int value) {
        this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:org.apache.dolphinscheduler.task.grpc.proto.Fruits)
}
