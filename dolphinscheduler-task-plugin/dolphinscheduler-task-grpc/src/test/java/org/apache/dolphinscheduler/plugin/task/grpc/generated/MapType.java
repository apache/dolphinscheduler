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
 * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.MapType}
 */
public final class MapType
        extends
            com.google.protobuf.GeneratedMessageV3
        implements
            // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.MapType)
            MapTypeOrBuilder {

    private static final long serialVersionUID = 0L;
    // Use MapType.newBuilder() to construct.
    private MapType(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private MapType() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
                                           UnusedPrivateParameter unused) {
        return new MapType();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    private MapType(
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
                            bought_ = com.google.protobuf.MapField.newMapField(
                                    BoughtDefaultEntryHolder.defaultEntry);
                            mutable_bitField0_ |= 0x00000001;
                        }
                        com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> bought__ = input.readMessage(
                                BoughtDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
                        bought_.getMutableMap().put(
                                bought__.getKey(), bought__.getValue());
                        break;
                    }
                    case 18: {
                        if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                            cash_ = com.google.protobuf.MapField.newMapField(
                                    CashDefaultEntryHolder.defaultEntry);
                            mutable_bitField0_ |= 0x00000002;
                        }
                        com.google.protobuf.MapEntry<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> cash__ =
                                input.readMessage(
                                        CashDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
                        cash_.getMutableMap().put(
                                cash__.getKey(), cash__.getValue());
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
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    @java.lang.Override
    protected com.google.protobuf.MapField internalGetMapField(
                                                               int number) {
        switch (number) {
            case 1:
                return internalGetBought();
            case 2:
                return internalGetCash();
            default:
                throw new RuntimeException(
                        "Invalid map field number: " + number);
        }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.class,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.Builder.class);
    }

    public static final int BOUGHT_FIELD_NUMBER = 1;
    private static final class BoughtDefaultEntryHolder {

        static final com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> defaultEntry =
                com.google.protobuf.MapEntry.<java.lang.String, java.lang.Integer>newDefaultInstance(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.ENUM,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.LINGO.getNumber());
    }
    private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> bought_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> internalGetBought() {
        if (bought_ == null) {
            return com.google.protobuf.MapField.emptyMapField(
                    BoughtDefaultEntryHolder.defaultEntry);
        }
        return bought_;
    }
    private static final com.google.protobuf.Internal.MapAdapter.Converter<java.lang.Integer, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> boughtValueConverter =
            com.google.protobuf.Internal.MapAdapter.newEnumConverter(
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.internalGetValueMap(),
                    org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits.UNRECOGNIZED);
    private static final java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> internalGetAdaptedBoughtMap(
                                                                                                                                                    java.util.Map<java.lang.String, java.lang.Integer> map) {
        return new com.google.protobuf.Internal.MapAdapter<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits, java.lang.Integer>(
                map, boughtValueConverter);
    }

    public int getBoughtCount() {
        return internalGetBought().getMap().size();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */

    @java.lang.Override
    public boolean containsBought(
                                  java.lang.String key) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        return internalGetBought().getMap().containsKey(key);
    }
    /**
     * Use {@link #getBoughtMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBought() {
        return getBoughtMap();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBoughtMap() {
        return internalGetAdaptedBoughtMap(
                internalGetBought().getMap());
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrDefault(
                                                                                            java.lang.String key,
                                                                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits defaultValue) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, java.lang.Integer> map =
                internalGetBought().getMap();
        return map.containsKey(key)
                ? boughtValueConverter.doForward(map.get(key))
                : defaultValue;
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrThrow(
                                                                                          java.lang.String key) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, java.lang.Integer> map =
                internalGetBought().getMap();
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException();
        }
        return boughtValueConverter.doForward(map.get(key));
    }
    /**
     * Use {@link #getBoughtValueMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.Integer> getBoughtValue() {
        return getBoughtValueMap();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public java.util.Map<java.lang.String, java.lang.Integer> getBoughtValueMap() {
        return internalGetBought().getMap();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public int getBoughtValueOrDefault(
                                       java.lang.String key,
                                       int defaultValue) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, java.lang.Integer> map =
                internalGetBought().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
     */
    @java.lang.Override

    public int getBoughtValueOrThrow(
                                     java.lang.String key) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, java.lang.Integer> map =
                internalGetBought().getMap();
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
    }

    public static final int CASH_FIELD_NUMBER = 2;
    private static final class CashDefaultEntryHolder {

        static final com.google.protobuf.MapEntry<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> defaultEntry =
                com.google.protobuf.MapEntry.<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only>newDefaultInstance(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only.getDefaultInstance());
    }
    private com.google.protobuf.MapField<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> cash_;
    private com.google.protobuf.MapField<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> internalGetCash() {
        if (cash_ == null) {
            return com.google.protobuf.MapField.emptyMapField(
                    CashDefaultEntryHolder.defaultEntry);
        }
        return cash_;
    }

    public int getCashCount() {
        return internalGetCash().getMap().size();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */

    @java.lang.Override
    public boolean containsCash(
                                java.lang.String key) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        return internalGetCash().getMap().containsKey(key);
    }
    /**
     * Use {@link #getCashMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCash() {
        return getCashMap();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    @java.lang.Override

    public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCashMap() {
        return internalGetCash().getMap();
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    @java.lang.Override

    public org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrDefault(
                                                                                             java.lang.String key,
                                                                                             org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only defaultValue) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> map =
                internalGetCash().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
     */
    @java.lang.Override

    public org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrThrow(
                                                                                           java.lang.String key) {
        if (key == null) {
            throw new java.lang.NullPointerException();
        }
        java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> map =
                internalGetCash().getMap();
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
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
        com.google.protobuf.GeneratedMessageV3
                .serializeStringMapTo(
                        output,
                        internalGetBought(),
                        BoughtDefaultEntryHolder.defaultEntry,
                        1);
        com.google.protobuf.GeneratedMessageV3
                .serializeStringMapTo(
                        output,
                        internalGetCash(),
                        CashDefaultEntryHolder.defaultEntry,
                        2);
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        for (java.util.Map.Entry<java.lang.String, java.lang.Integer> entry : internalGetBought().getMap().entrySet()) {
            com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> bought__ =
                    BoughtDefaultEntryHolder.defaultEntry.newBuilderForType()
                            .setKey(entry.getKey())
                            .setValue(entry.getValue())
                            .build();
            size += com.google.protobuf.CodedOutputStream
                    .computeMessageSize(1, bought__);
        }
        for (java.util.Map.Entry<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> entry : internalGetCash()
                .getMap().entrySet()) {
            com.google.protobuf.MapEntry<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> cash__ =
                    CashDefaultEntryHolder.defaultEntry.newBuilderForType()
                            .setKey(entry.getKey())
                            .setValue(entry.getValue())
                            .build();
            size += com.google.protobuf.CodedOutputStream
                    .computeMessageSize(2, cash__);
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
        if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType)) {
            return super.equals(obj);
        }
        org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType other =
                (org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType) obj;

        if (!internalGetBought().equals(
                other.internalGetBought()))
            return false;
        if (!internalGetCash().equals(
                other.internalGetCash()))
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
        if (!internalGetBought().getMap().isEmpty()) {
            hash = (37 * hash) + BOUGHT_FIELD_NUMBER;
            hash = (53 * hash) + internalGetBought().hashCode();
        }
        if (!internalGetCash().getMap().isEmpty()) {
            hash = (37 * hash) + CASH_FIELD_NUMBER;
            hash = (53 * hash) + internalGetCash().hashCode();
        }
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           java.nio.ByteBuffer data,
                                                                                           com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           com.google.protobuf.ByteString data,
                                                                                           com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           byte[] data,
                                                                                           com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           java.io.InputStream input,
                                                                                           com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseDelimitedFrom(
                                                                                                    java.io.InputStream input,
                                                                                                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
                                                                                           com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parseFrom(
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
    public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType prototype) {
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
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.MapType}
     */
    public static final class Builder
            extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
                // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.MapType)
                org.apache.dolphinscheduler.plugin.task.grpc.generated.MapTypeOrBuilder {

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor;
        }

        @SuppressWarnings({"rawtypes"})
        protected com.google.protobuf.MapField internalGetMapField(
                                                                   int number) {
            switch (number) {
                case 1:
                    return internalGetBought();
                case 2:
                    return internalGetCash();
                default:
                    throw new RuntimeException(
                            "Invalid map field number: " + number);
            }
        }
        @SuppressWarnings({"rawtypes"})
        protected com.google.protobuf.MapField internalGetMutableMapField(
                                                                          int number) {
            switch (number) {
                case 1:
                    return internalGetMutableBought();
                case 2:
                    return internalGetMutableCash();
                default:
                    throw new RuntimeException(
                            "Invalid map field number: " + number);
            }
        }
        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.Builder.class);
        }

        // Construct using org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.newBuilder()
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
            internalGetMutableBought().clear();
            internalGetMutableCash().clear();
            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType getDefaultInstanceForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.getDefaultInstance();
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType build() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType buildPartial() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType result =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType(this);
            int from_bitField0_ = bitField0_;
            result.bought_ = internalGetBought();
            result.bought_.makeImmutable();
            result.cash_ = internalGetCash();
            result.cash_.makeImmutable();
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
            if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType) {
                return mergeFrom((org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType other) {
            if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.getDefaultInstance())
                return this;
            internalGetMutableBought().mergeFrom(
                    other.internalGetBought());
            internalGetMutableCash().mergeFrom(
                    other.internalGetCash());
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
            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage =
                        (org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }
        private int bitField0_;

        private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> bought_;
        private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> internalGetBought() {
            if (bought_ == null) {
                return com.google.protobuf.MapField.emptyMapField(
                        BoughtDefaultEntryHolder.defaultEntry);
            }
            return bought_;
        }
        private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> internalGetMutableBought() {
            onChanged();;
            if (bought_ == null) {
                bought_ = com.google.protobuf.MapField.newMapField(
                        BoughtDefaultEntryHolder.defaultEntry);
            }
            if (!bought_.isMutable()) {
                bought_ = bought_.copy();
            }
            return bought_;
        }

        public int getBoughtCount() {
            return internalGetBought().getMap().size();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */

        @java.lang.Override
        public boolean containsBought(
                                      java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            return internalGetBought().getMap().containsKey(key);
        }
        /**
         * Use {@link #getBoughtMap()} instead.
         */
        @java.lang.Override
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBought() {
            return getBoughtMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getBoughtMap() {
            return internalGetAdaptedBoughtMap(
                    internalGetBought().getMap());
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrDefault(
                                                                                                java.lang.String key,
                                                                                                org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits defaultValue) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, java.lang.Integer> map =
                    internalGetBought().getMap();
            return map.containsKey(key)
                    ? boughtValueConverter.doForward(map.get(key))
                    : defaultValue;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits getBoughtOrThrow(
                                                                                              java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, java.lang.Integer> map =
                    internalGetBought().getMap();
            if (!map.containsKey(key)) {
                throw new java.lang.IllegalArgumentException();
            }
            return boughtValueConverter.doForward(map.get(key));
        }
        /**
         * Use {@link #getBoughtValueMap()} instead.
         */
        @java.lang.Override
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, java.lang.Integer> getBoughtValue() {
            return getBoughtValueMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public java.util.Map<java.lang.String, java.lang.Integer> getBoughtValueMap() {
            return internalGetBought().getMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public int getBoughtValueOrDefault(
                                           java.lang.String key,
                                           int defaultValue) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, java.lang.Integer> map =
                    internalGetBought().getMap();
            return map.containsKey(key) ? map.get(key) : defaultValue;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        @java.lang.Override

        public int getBoughtValueOrThrow(
                                         java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, java.lang.Integer> map =
                    internalGetBought().getMap();
            if (!map.containsKey(key)) {
                throw new java.lang.IllegalArgumentException();
            }
            return map.get(key);
        }

        public Builder clearBought() {
            internalGetMutableBought().getMutableMap()
                    .clear();
            return this;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */

        public Builder removeBought(
                                    java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            internalGetMutableBought().getMutableMap()
                    .remove(key);
            return this;
        }
        /**
         * Use alternate mutation accessors instead.
         */
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> getMutableBought() {
            return internalGetAdaptedBoughtMap(
                    internalGetMutableBought().getMutableMap());
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        public Builder putBought(
                                 java.lang.String key,
                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits value) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            if (value == null) {
                throw new java.lang.NullPointerException();
            }
            internalGetMutableBought().getMutableMap()
                    .put(key, boughtValueConverter.doBackward(value));
            return this;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        public Builder putAllBought(
                                    java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Fruits> values) {
            internalGetAdaptedBoughtMap(
                    internalGetMutableBought().getMutableMap())
                            .putAll(values);
            return this;
        }
        /**
         * Use alternate mutation accessors instead.
         */
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, java.lang.Integer> getMutableBoughtValue() {
            return internalGetMutableBought().getMutableMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        public Builder putBoughtValue(
                                      java.lang.String key,
                                      int value) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            internalGetMutableBought().getMutableMap()
                    .put(key, value);
            return this;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Fruits&gt; bought = 1;</code>
         */
        public Builder putAllBoughtValue(
                                         java.util.Map<java.lang.String, java.lang.Integer> values) {
            internalGetMutableBought().getMutableMap()
                    .putAll(values);
            return this;
        }

        private com.google.protobuf.MapField<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> cash_;
        private com.google.protobuf.MapField<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> internalGetCash() {
            if (cash_ == null) {
                return com.google.protobuf.MapField.emptyMapField(
                        CashDefaultEntryHolder.defaultEntry);
            }
            return cash_;
        }
        private com.google.protobuf.MapField<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> internalGetMutableCash() {
            onChanged();;
            if (cash_ == null) {
                cash_ = com.google.protobuf.MapField.newMapField(
                        CashDefaultEntryHolder.defaultEntry);
            }
            if (!cash_.isMutable()) {
                cash_ = cash_.copy();
            }
            return cash_;
        }

        public int getCashCount() {
            return internalGetCash().getMap().size();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */

        @java.lang.Override
        public boolean containsCash(
                                    java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            return internalGetCash().getMap().containsKey(key);
        }
        /**
         * Use {@link #getCashMap()} instead.
         */
        @java.lang.Override
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCash() {
            return getCashMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */
        @java.lang.Override

        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getCashMap() {
            return internalGetCash().getMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */
        @java.lang.Override

        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrDefault(
                                                                                                 java.lang.String key,
                                                                                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only defaultValue) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> map =
                    internalGetCash().getMap();
            return map.containsKey(key) ? map.get(key) : defaultValue;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */
        @java.lang.Override

        public org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only getCashOrThrow(
                                                                                               java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> map =
                    internalGetCash().getMap();
            if (!map.containsKey(key)) {
                throw new java.lang.IllegalArgumentException();
            }
            return map.get(key);
        }

        public Builder clearCash() {
            internalGetMutableCash().getMutableMap()
                    .clear();
            return this;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */

        public Builder removeCash(
                                  java.lang.String key) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            internalGetMutableCash().getMutableMap()
                    .remove(key);
            return this;
        }
        /**
         * Use alternate mutation accessors instead.
         */
        @java.lang.Deprecated
        public java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> getMutableCash() {
            return internalGetMutableCash().getMutableMap();
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */
        public Builder putCash(
                               java.lang.String key,
                               org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only value) {
            if (key == null) {
                throw new java.lang.NullPointerException();
            }
            if (value == null) {
                throw new java.lang.NullPointerException();
            }
            internalGetMutableCash().getMutableMap()
                    .put(key, value);
            return this;
        }
        /**
         * <code>map&lt;string, .org.apache.dolphinscheduler.task.grpc.proto.Int32Only&gt; cash = 2;</code>
         */

        public Builder putAllCash(
                                  java.util.Map<java.lang.String, org.apache.dolphinscheduler.plugin.task.grpc.generated.Int32Only> values) {
            internalGetMutableCash().getMutableMap()
                    .putAll(values);
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

        // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.MapType)
    }

    // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.MapType)
    private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType();
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<MapType> PARSER =
            new com.google.protobuf.AbstractParser<MapType>() {

                @java.lang.Override
                public MapType parsePartialFrom(
                                                com.google.protobuf.CodedInputStream input,
                                                com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                    return new MapType(input, extensionRegistry);
                }
            };

    public static com.google.protobuf.Parser<MapType> parser() {
        return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MapType> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}
