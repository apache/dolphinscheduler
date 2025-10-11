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
 * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType}
 */
public final class PrimitiveMapType
        extends
            com.google.protobuf.GeneratedMessageV3
        implements
            // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType)
            PrimitiveMapTypeOrBuilder {

    private static final long serialVersionUID = 0L;
    // Use PrimitiveMapType.newBuilder() to construct.
    private PrimitiveMapType(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private PrimitiveMapType() {
        bought_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
                                           UnusedPrivateParameter unused) {
        return new PrimitiveMapType();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    private PrimitiveMapType(
                             com.google.protobuf.CodedInputStream input,
                             com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                                                                                          throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new java.lang.NullPointerException();
        }
        int mutable_bitField0_ = 0;
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    case 10: {
                        if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                            bought_ =
                                    new java.util.ArrayList<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        bought_.add(
                                input.readMessage(
                                        org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought
                                                .parser(),
                                        extensionRegistry));
                        break;
                    }
                    default: {
                        if (!parseUnknownField(
                                input, unknownFields, extensionRegistry, tag)) {
                            done = true;
                        }
                        break;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(
                    e).setUnfinishedMessage(this);
        } finally {
            if (((mutable_bitField0_ & 0x00000001) != 0)) {
                bought_ = java.util.Collections.unmodifiableList(bought_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.class,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.Builder.class);
    }

    public interface MapEntry_boughtOrBuilder
            extends
                // @@protoc_insertion_point(interface_extends:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought)
                com.google.protobuf.MessageOrBuilder {

        /**
         * <code>string key = 1;</code>
         * @return The key.
         */
        java.lang.String getKey();
        /**
         * <code>string key = 1;</code>
         * @return The bytes for key.
         */
        com.google.protobuf.ByteString getKeyBytes();

        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
         * @return The enum numeric value on the wire for value.
         */
        int getValueValue();
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
         * @return The value.
         */
        org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getValue();
    }
    /**
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought}
     */
    public static final class MapEntry_bought
            extends
                com.google.protobuf.GeneratedMessageV3
            implements
                // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought)
                MapEntry_boughtOrBuilder {

        private static final long serialVersionUID = 0L;
        // Use MapEntry_bought.newBuilder() to construct.
        private MapEntry_bought(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }
        private MapEntry_bought() {
            key_ = "";
            value_ = 0;
        }

        @java.lang.Override
        @SuppressWarnings({"unused"})
        protected java.lang.Object newInstance(
                                               UnusedPrivateParameter unused) {
            return new MapEntry_bought();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }
        private MapEntry_bought(
                                com.google.protobuf.CodedInputStream input,
                                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                                                                                             throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new java.lang.NullPointerException();
            }
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 10: {
                            java.lang.String s = input.readStringRequireUtf8();

                            key_ = s;
                            break;
                        }
                        case 16: {
                            int rawValue = input.readEnum();

                            value_ = rawValue;
                            break;
                        }
                        default: {
                            if (!parseUnknownField(
                                    input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }
        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder.class);
        }

        public static final int KEY_FIELD_NUMBER = 1;
        private volatile java.lang.Object key_;
        /**
         * <code>string key = 1;</code>
         * @return The key.
         */
        @java.lang.Override
        public java.lang.String getKey() {
            java.lang.Object ref = key_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                key_ = s;
                return s;
            }
        }
        /**
         * <code>string key = 1;</code>
         * @return The bytes for key.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString getKeyBytes() {
            java.lang.Object ref = key_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                key_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int VALUE_FIELD_NUMBER = 2;
        private int value_;
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
         * @return The enum numeric value on the wire for value.
         */
        @java.lang.Override
        public int getValueValue() {
            return value_;
        }
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
         * @return The value.
         */
        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getValue() {
            @SuppressWarnings("deprecation")
            org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits result =
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.valueOf(value_);
            return result == null ? org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.UNRECOGNIZED : result;
        }

        private byte memoizedIsInitialized = -1;
        @java.lang.Override
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1)
                return true;
            if (isInitialized == 0)
                return false;

            memoizedIsInitialized = 1;
            return true;
        }

        @java.lang.Override
        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (!getKeyBytes().isEmpty()) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 1, key_);
            }
            if (value_ != org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.Lingo.getNumber()) {
                output.writeEnum(2, value_);
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (!getKeyBytes().isEmpty()) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, key_);
            }
            if (value_ != org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.Lingo.getNumber()) {
                size += com.google.protobuf.CodedOutputStream
                        .computeEnumSize(2, value_);
            }
            size += unknownFields.getSerializedSize();
            memoizedSize = size;
            return size;
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought)) {
                return super.equals(obj);
            }
            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought other =
                    (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought) obj;

            if (!getKey()
                    .equals(other.getKey()))
                return false;
            if (value_ != other.value_)
                return false;
            if (!unknownFields.equals(other.unknownFields))
                return false;
            return true;
        }

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptor().hashCode();
            hash = (37 * hash) + KEY_FIELD_NUMBER;
            hash = (53 * hash) + getKey().hashCode();
            hash = (37 * hash) + VALUE_FIELD_NUMBER;
            hash = (53 * hash) + value_;
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        java.nio.ByteBuffer data,
                                                                                                                        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        com.google.protobuf.ByteString data,
                                                                                                                        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        byte[] data,
                                                                                                                        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(java.io.InputStream input) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        java.io.InputStream input,
                                                                                                                        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseDelimitedFrom(
                                                                                                                                 java.io.InputStream input,
                                                                                                                                 com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }
        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parseFrom(
                                                                                                                        com.google.protobuf.CodedInputStream input,
                                                                                                                        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        @java.lang.Override
        public Builder newBuilderForType() {
            return newBuilder();
        }
        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }
        public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }
        @java.lang.Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE
                    ? new Builder()
                    : new Builder().mergeFrom(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(
                                            com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }
        /**
         * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought}
         */
        public static final class Builder
                extends
                    com.google.protobuf.GeneratedMessageV3.Builder<Builder>
                implements
                    // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought)
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.class,
                                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder.class);
            }

            // Construct using
            // org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                            com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }
            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
                }
            }
            @java.lang.Override
            public Builder clear() {
                super.clear();
                key_ = "";

                value_ = 0;

                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor;
            }

            @java.lang.Override
            public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getDefaultInstanceForType() {
                return org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought
                        .getDefaultInstance();
            }

            @java.lang.Override
            public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought build() {
                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought result =
                        buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought buildPartial() {
                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought result =
                        new org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought(
                                this);
                result.key_ = key_;
                result.value_ = value_;
                onBuilt();
                return result;
            }

            @java.lang.Override
            public Builder clone() {
                return super.clone();
            }
            @java.lang.Override
            public Builder setField(
                                    com.google.protobuf.Descriptors.FieldDescriptor field,
                                    java.lang.Object value) {
                return super.setField(field, value);
            }
            @java.lang.Override
            public Builder clearField(
                                      com.google.protobuf.Descriptors.FieldDescriptor field) {
                return super.clearField(field);
            }
            @java.lang.Override
            public Builder clearOneof(
                                      com.google.protobuf.Descriptors.OneofDescriptor oneof) {
                return super.clearOneof(oneof);
            }
            @java.lang.Override
            public Builder setRepeatedField(
                                            com.google.protobuf.Descriptors.FieldDescriptor field,
                                            int index, java.lang.Object value) {
                return super.setRepeatedField(field, index, value);
            }
            @java.lang.Override
            public Builder addRepeatedField(
                                            com.google.protobuf.Descriptors.FieldDescriptor field,
                                            java.lang.Object value) {
                return super.addRepeatedField(field, value);
            }
            @java.lang.Override
            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought) {
                    return mergeFrom(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought other) {
                if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought
                        .getDefaultInstance())
                    return this;
                if (!other.getKey().isEmpty()) {
                    key_ = other.key_;
                    onChanged();
                }
                if (other.value_ != 0) {
                    setValueValue(other.getValueValue());
                }
                this.mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            @java.lang.Override
            public final boolean isInitialized() {
                return true;
            }

            @java.lang.Override
            public Builder mergeFrom(
                                     com.google.protobuf.CodedInputStream input,
                                     com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought parsedMessage =
                        null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage =
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought) e
                                    .getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private java.lang.Object key_ = "";
            /**
             * <code>string key = 1;</code>
             * @return The key.
             */
            public java.lang.String getKey() {
                java.lang.Object ref = key_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    key_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>string key = 1;</code>
             * @return The bytes for key.
             */
            public com.google.protobuf.ByteString getKeyBytes() {
                java.lang.Object ref = key_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    key_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>string key = 1;</code>
             * @param value The key to set.
             * @return This builder for chaining.
             */
            public Builder setKey(
                                  java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                key_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>string key = 1;</code>
             * @return This builder for chaining.
             */
            public Builder clearKey() {

                key_ = getDefaultInstance().getKey();
                onChanged();
                return this;
            }
            /**
             * <code>string key = 1;</code>
             * @param value The bytes for key to set.
             * @return This builder for chaining.
             */
            public Builder setKeyBytes(
                                       com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);

                key_ = value;
                onChanged();
                return this;
            }

            private int value_ = 0;
            /**
             * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
             * @return The enum numeric value on the wire for value.
             */
            @java.lang.Override
            public int getValueValue() {
                return value_;
            }
            /**
             * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
             * @param value The enum numeric value on the wire for value to set.
             * @return This builder for chaining.
             */
            public Builder setValueValue(int value) {

                value_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
             * @return The value.
             */
            @java.lang.Override
            public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getValue() {
                @SuppressWarnings("deprecation")
                org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits result =
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.valueOf(value_);
                return result == null ? org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.UNRECOGNIZED
                        : result;
            }
            /**
             * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
             * @param value The value to set.
             * @return This builder for chaining.
             */
            public Builder setValue(org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                value_ = value.getNumber();
                onChanged();
                return this;
            }
            /**
             * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits value = 2;</code>
             * @return This builder for chaining.
             */
            public Builder clearValue() {

                value_ = 0;
                onChanged();
                return this;
            }
            @java.lang.Override
            public final Builder setUnknownFields(
                                                  final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.setUnknownFields(unknownFields);
            }

            @java.lang.Override
            public final Builder mergeUnknownFields(
                                                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.mergeUnknownFields(unknownFields);
            }

            // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought)
        }

        // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought)
        private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought();
        }

        public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<MapEntry_bought> PARSER =
                new com.google.protobuf.AbstractParser<MapEntry_bought>() {

                    @java.lang.Override
                    public MapEntry_bought parsePartialFrom(
                                                            com.google.protobuf.CodedInputStream input,
                                                            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                        return new MapEntry_bought(input, extensionRegistry);
                    }
                };

        public static com.google.protobuf.Parser<MapEntry_bought> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<MapEntry_bought> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public static final int BOUGHT_FIELD_NUMBER = 1;
    private java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> bought_;
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    @java.lang.Override
    public java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> getBoughtList() {
        return bought_;
    }
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    @java.lang.Override
    public java.util.List<? extends org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder> getBoughtOrBuilderList() {
        return bought_;
    }
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    @java.lang.Override
    public int getBoughtCount() {
        return bought_.size();
    }
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getBought(int index) {
        return bought_.get(index);
    }
    /**
     * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
     */
    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder getBoughtOrBuilder(
                                                                                                                               int index) {
        return bought_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1)
            return true;
        if (isInitialized == 0)
            return false;

        memoizedIsInitialized = 1;
        return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
        for (int i = 0; i < bought_.size(); i++) {
            output.writeMessage(1, bought_.get(i));
        }
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        for (int i = 0; i < bought_.size(); i++) {
            size += com.google.protobuf.CodedOutputStream
                    .computeMessageSize(1, bought_.get(i));
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType)) {
            return super.equals(obj);
        }
        org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType other =
                (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType) obj;

        if (!getBoughtList()
                .equals(other.getBoughtList()))
            return false;
        if (!unknownFields.equals(other.unknownFields))
            return false;
        return true;
    }

    @java.lang.Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        if (getBoughtCount() > 0) {
            hash = (37 * hash) + BOUGHT_FIELD_NUMBER;
            hash = (53 * hash) + getBoughtList().hashCode();
        }
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    java.nio.ByteBuffer data,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    com.google.protobuf.ByteString data,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    byte[] data,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    java.io.InputStream input,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseDelimitedFrom(
                                                                                                             java.io.InputStream input,
                                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parseFrom(
                                                                                                    com.google.protobuf.CodedInputStream input,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() {
        return newBuilder();
    }
    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
                ? new Builder()
                : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
                                        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
    }
    /**
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType}
     */
    public static final class Builder
            extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
                // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType)
                org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapTypeOrBuilder {

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.Builder.class);
        }

        // Construct using org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
                        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }
        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
                getBoughtFieldBuilder();
            }
        }
        @java.lang.Override
        public Builder clear() {
            super.clear();
            if (boughtBuilder_ == null) {
                bought_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
            } else {
                boughtBuilder_.clear();
            }
            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType getDefaultInstanceForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.getDefaultInstance();
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType build() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType buildPartial() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType result =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType(this);
            int from_bitField0_ = bitField0_;
            if (boughtBuilder_ == null) {
                if (((bitField0_ & 0x00000001) != 0)) {
                    bought_ = java.util.Collections.unmodifiableList(bought_);
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.bought_ = bought_;
            } else {
                result.bought_ = boughtBuilder_.build();
            }
            onBuilt();
            return result;
        }

        @java.lang.Override
        public Builder clone() {
            return super.clone();
        }
        @java.lang.Override
        public Builder setField(
                                com.google.protobuf.Descriptors.FieldDescriptor field,
                                java.lang.Object value) {
            return super.setField(field, value);
        }
        @java.lang.Override
        public Builder clearField(
                                  com.google.protobuf.Descriptors.FieldDescriptor field) {
            return super.clearField(field);
        }
        @java.lang.Override
        public Builder clearOneof(
                                  com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return super.clearOneof(oneof);
        }
        @java.lang.Override
        public Builder setRepeatedField(
                                        com.google.protobuf.Descriptors.FieldDescriptor field,
                                        int index, java.lang.Object value) {
            return super.setRepeatedField(field, index, value);
        }
        @java.lang.Override
        public Builder addRepeatedField(
                                        com.google.protobuf.Descriptors.FieldDescriptor field,
                                        java.lang.Object value) {
            return super.addRepeatedField(field, value);
        }
        @java.lang.Override
        public Builder mergeFrom(com.google.protobuf.Message other) {
            if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType) {
                return mergeFrom((org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType other) {
            if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.getDefaultInstance())
                return this;
            if (boughtBuilder_ == null) {
                if (!other.bought_.isEmpty()) {
                    if (bought_.isEmpty()) {
                        bought_ = other.bought_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureBoughtIsMutable();
                        bought_.addAll(other.bought_);
                    }
                    onChanged();
                }
            } else {
                if (!other.bought_.isEmpty()) {
                    if (boughtBuilder_.isEmpty()) {
                        boughtBuilder_.dispose();
                        boughtBuilder_ = null;
                        bought_ = other.bought_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                        boughtBuilder_ =
                                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ? getBoughtFieldBuilder()
                                        : null;
                    } else {
                        boughtBuilder_.addAllMessages(other.bought_);
                    }
                }
            }
            this.mergeUnknownFields(other.unknownFields);
            onChanged();
            return this;
        }

        @java.lang.Override
        public final boolean isInitialized() {
            return true;
        }

        @java.lang.Override
        public Builder mergeFrom(
                                 com.google.protobuf.CodedInputStream input,
                                 com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType) e
                        .getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }
        private int bitField0_;

        private java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> bought_ =
                java.util.Collections.emptyList();
        private void ensureBoughtIsMutable() {
            if (!((bitField0_ & 0x00000001) != 0)) {
                bought_ =
                        new java.util.ArrayList<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought>(
                                bought_);
                bitField0_ |= 0x00000001;
            }
        }

        private com.google.protobuf.RepeatedFieldBuilderV3<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder> boughtBuilder_;

        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> getBoughtList() {
            if (boughtBuilder_ == null) {
                return java.util.Collections.unmodifiableList(bought_);
            } else {
                return boughtBuilder_.getMessageList();
            }
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public int getBoughtCount() {
            if (boughtBuilder_ == null) {
                return bought_.size();
            } else {
                return boughtBuilder_.getCount();
            }
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought getBought(int index) {
            if (boughtBuilder_ == null) {
                return bought_.get(index);
            } else {
                return boughtBuilder_.getMessage(index);
            }
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder setBought(
                                 int index,
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought value) {
            if (boughtBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureBoughtIsMutable();
                bought_.set(index, value);
                onChanged();
            } else {
                boughtBuilder_.setMessage(index, value);
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder setBought(
                                 int index,
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder builderForValue) {
            if (boughtBuilder_ == null) {
                ensureBoughtIsMutable();
                bought_.set(index, builderForValue.build());
                onChanged();
            } else {
                boughtBuilder_.setMessage(index, builderForValue.build());
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder addBought(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought value) {
            if (boughtBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureBoughtIsMutable();
                bought_.add(value);
                onChanged();
            } else {
                boughtBuilder_.addMessage(value);
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder addBought(
                                 int index,
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought value) {
            if (boughtBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureBoughtIsMutable();
                bought_.add(index, value);
                onChanged();
            } else {
                boughtBuilder_.addMessage(index, value);
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder addBought(
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder builderForValue) {
            if (boughtBuilder_ == null) {
                ensureBoughtIsMutable();
                bought_.add(builderForValue.build());
                onChanged();
            } else {
                boughtBuilder_.addMessage(builderForValue.build());
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder addBought(
                                 int index,
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder builderForValue) {
            if (boughtBuilder_ == null) {
                ensureBoughtIsMutable();
                bought_.add(index, builderForValue.build());
                onChanged();
            } else {
                boughtBuilder_.addMessage(index, builderForValue.build());
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder addAllBought(
                                    java.lang.Iterable<? extends org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought> values) {
            if (boughtBuilder_ == null) {
                ensureBoughtIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, bought_);
                onChanged();
            } else {
                boughtBuilder_.addAllMessages(values);
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder clearBought() {
            if (boughtBuilder_ == null) {
                bought_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
            } else {
                boughtBuilder_.clear();
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public Builder removeBought(int index) {
            if (boughtBuilder_ == null) {
                ensureBoughtIsMutable();
                bought_.remove(index);
                onChanged();
            } else {
                boughtBuilder_.remove(index);
            }
            return this;
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder getBoughtBuilder(
                                                                                                                                int index) {
            return getBoughtFieldBuilder().getBuilder(index);
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder getBoughtOrBuilder(
                                                                                                                                   int index) {
            if (boughtBuilder_ == null) {
                return bought_.get(index);
            } else {
                return boughtBuilder_.getMessageOrBuilder(index);
            }
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public java.util.List<? extends org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder> getBoughtOrBuilderList() {
            if (boughtBuilder_ != null) {
                return boughtBuilder_.getMessageOrBuilderList();
            } else {
                return java.util.Collections.unmodifiableList(bought_);
            }
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder addBoughtBuilder() {
            return getBoughtFieldBuilder().addBuilder(
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought
                            .getDefaultInstance());
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder addBoughtBuilder(
                                                                                                                                int index) {
            return getBoughtFieldBuilder().addBuilder(
                    index, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought
                            .getDefaultInstance());
        }
        /**
         * <code>repeated .org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType.MapEntry_bought bought = 1;</code>
         */
        public java.util.List<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder> getBoughtBuilderList() {
            return getBoughtFieldBuilder().getBuilderList();
        }
        private com.google.protobuf.RepeatedFieldBuilderV3<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder> getBoughtFieldBuilder() {
            if (boughtBuilder_ == null) {
                boughtBuilder_ =
                        new com.google.protobuf.RepeatedFieldBuilderV3<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_bought.Builder, org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.MapEntry_boughtOrBuilder>(
                                bought_,
                                ((bitField0_ & 0x00000001) != 0),
                                getParentForChildren(),
                                isClean());
                bought_ = null;
            }
            return boughtBuilder_;
        }
        @java.lang.Override
        public final Builder setUnknownFields(
                                              final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFields(unknownFields);
        }

        @java.lang.Override
        public final Builder mergeUnknownFields(
                                                final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }

        // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType)
    }

    // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.PrimitiveMapType)
    private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType();
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<PrimitiveMapType> PARSER =
            new com.google.protobuf.AbstractParser<PrimitiveMapType>() {

                @java.lang.Override
                public PrimitiveMapType parsePartialFrom(
                                                         com.google.protobuf.CodedInputStream input,
                                                         com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                    return new PrimitiveMapType(input, extensionRegistry);
                }
            };

    public static com.google.protobuf.Parser<PrimitiveMapType> parser() {
        return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<PrimitiveMapType> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}
