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
 * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.NoneReply}
 */
public final class NoneReply
        extends
            com.google.protobuf.GeneratedMessageV3
        implements
            // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.NoneReply)
            NoneReplyOrBuilder {

    private static final long serialVersionUID = 0L;
    // Use NoneReply.newBuilder() to construct.
    private NoneReply(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private NoneReply() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
                                           UnusedPrivateParameter unused) {
        return new NoneReply();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    private NoneReply(
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
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.Builder.class);
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
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply)) {
            return super.equals(obj);
        }
        org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply other =
                (org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply) obj;

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
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             java.nio.ByteBuffer data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             com.google.protobuf.ByteString data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             byte[] data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             java.io.InputStream input,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseDelimitedFrom(
                                                                                                      java.io.InputStream input,
                                                                                                      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
                                                                                             com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parseFrom(
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
    public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply prototype) {
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
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.NoneReply}
     */
    public static final class Builder
            extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
                // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.NoneReply)
                org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReplyOrBuilder {

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.Builder.class);
        }

        // Construct using org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.newBuilder()
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
            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply getDefaultInstanceForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.getDefaultInstance();
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply build() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply buildPartial() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply result =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply(this);
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
            if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply) {
                return mergeFrom((org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply other) {
            if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.getDefaultInstance())
                return this;
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
            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage =
                        (org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
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

        // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.NoneReply)
    }

    // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.NoneReply)
    private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply();
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<NoneReply> PARSER =
            new com.google.protobuf.AbstractParser<NoneReply>() {

                @java.lang.Override
                public NoneReply parsePartialFrom(
                                                  com.google.protobuf.CodedInputStream input,
                                                  com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                    return new NoneReply(input, extensionRegistry);
                }
            };

    public static com.google.protobuf.Parser<NoneReply> parser() {
        return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<NoneReply> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}
