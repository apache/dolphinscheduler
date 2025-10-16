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
 * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.EnumType}
 */
public final class EnumType
        extends
            com.google.protobuf.GeneratedMessageV3
        implements
            // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.EnumType)
            EnumTypeOrBuilder {

    private static final long serialVersionUID = 0L;
    // Use EnumType.newBuilder() to construct.
    private EnumType(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private EnumType() {
        what2Eat_ = 0;
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
                                           UnusedPrivateParameter unused) {
        return new EnumType();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    private EnumType(
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
                    case 8: {
                        int rawValue = input.readEnum();

                        what2Eat_ = rawValue;
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
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.class,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.Builder.class);
    }

    public static final int WHAT2EAT_FIELD_NUMBER = 1;
    private int what2Eat_;
    /**
     * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
     * @return The enum numeric value on the wire for what2eat.
     */
    @java.lang.Override
    public int getWhat2EatValue() {
        return what2Eat_;
    }
    /**
     * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
     * @return The what2eat.
     */
    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getWhat2Eat() {
        @SuppressWarnings("deprecation")
        org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits result =
                org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.valueOf(what2Eat_);
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
        if (what2Eat_ != org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.LINGO.getNumber()) {
            output.writeEnum(1, what2Eat_);
        }
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        if (what2Eat_ != org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.LINGO.getNumber()) {
            size += com.google.protobuf.CodedOutputStream
                    .computeEnumSize(1, what2Eat_);
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
        if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType)) {
            return super.equals(obj);
        }
        org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType other =
                (org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType) obj;

        if (what2Eat_ != other.what2Eat_)
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
        hash = (37 * hash) + WHAT2EAT_FIELD_NUMBER;
        hash = (53 * hash) + what2Eat_;
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            java.nio.ByteBuffer data,
                                                                                            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            com.google.protobuf.ByteString data,
                                                                                            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            byte[] data,
                                                                                            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            java.io.InputStream input,
                                                                                            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseDelimitedFrom(
                                                                                                     java.io.InputStream input,
                                                                                                     com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
                                                                                            com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parseFrom(
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
    public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType prototype) {
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
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.EnumType}
     */
    public static final class Builder
            extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
                // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.EnumType)
                org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumTypeOrBuilder {

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.Builder.class);
        }

        // Construct using org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.newBuilder()
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
            what2Eat_ = 0;

            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType getDefaultInstanceForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.getDefaultInstance();
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType build() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType buildPartial() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType result =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType(this);
            result.what2Eat_ = what2Eat_;
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
            if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType) {
                return mergeFrom((org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType other) {
            if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.getDefaultInstance())
                return this;
            if (other.what2Eat_ != 0) {
                setWhat2EatValue(other.getWhat2EatValue());
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
            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage =
                        (org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private int what2Eat_ = 0;
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
         * @return The enum numeric value on the wire for what2eat.
         */
        @java.lang.Override
        public int getWhat2EatValue() {
            return what2Eat_;
        }
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
         * @param value The enum numeric value on the wire for what2eat to set.
         * @return This builder for chaining.
         */
        public Builder setWhat2EatValue(int value) {

            what2Eat_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
         * @return The what2eat.
         */
        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getWhat2Eat() {
            @SuppressWarnings("deprecation")
            org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits result =
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.valueOf(what2Eat_);
            return result == null ? org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.UNRECOGNIZED : result;
        }
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
         * @param value The what2eat to set.
         * @return This builder for chaining.
         */
        public Builder setWhat2Eat(org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits value) {
            if (value == null) {
                throw new NullPointerException();
            }

            what2Eat_ = value.getNumber();
            onChanged();
            return this;
        }
        /**
         * <code>.org.apache.dolphinscheduler.task.grpc.proto.Fruits what2eat = 1;</code>
         * @return This builder for chaining.
         */
        public Builder clearWhat2Eat() {

            what2Eat_ = 0;
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

        // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.EnumType)
    }

    // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.EnumType)
    private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType();
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<EnumType> PARSER =
            new com.google.protobuf.AbstractParser<EnumType>() {

                @java.lang.Override
                public EnumType parsePartialFrom(
                                                 com.google.protobuf.CodedInputStream input,
                                                 com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                    return new EnumType(input, extensionRegistry);
                }
            };

    public static com.google.protobuf.Parser<EnumType> parser() {
        return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<EnumType> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}
