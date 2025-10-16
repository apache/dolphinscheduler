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
 * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.OneofType}
 */
public final class OneofType
        extends
            com.google.protobuf.GeneratedMessageV3
        implements
            // @@protoc_insertion_point(message_implements:org.apache.dolphinscheduler.task.grpc.proto.OneofType)
            OneofTypeOrBuilder {

    private static final long serialVersionUID = 0L;
    // Use OneofType.newBuilder() to construct.
    private OneofType(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private OneofType() {
        name_ = "";
        bio_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
                                           UnusedPrivateParameter unused) {
        return new OneofType();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    private OneofType(
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
                        java.lang.String s = input.readStringRequireUtf8();

                        name_ = s;
                        break;
                    }
                    case 18: {
                        java.lang.String s = input.readStringRequireUtf8();
                        bitField0_ |= 0x00000001;
                        bio_ = s;
                        break;
                    }
                    case 24: {
                        bitField0_ |= 0x00000002;
                        age_ = input.readInt32();
                        break;
                    }
                    case 34: {
                        java.lang.String s = input.readStringRequireUtf8();
                        contactCase_ = 4;
                        contact_ = s;
                        break;
                    }
                    case 42: {
                        java.lang.String s = input.readStringRequireUtf8();
                        contactCase_ = 5;
                        contact_ = s;
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
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.class,
                        org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.Builder.class);
    }

    private int bitField0_;
    private int contactCase_ = 0;
    private java.lang.Object contact_;
    public enum ContactCase
            implements
                com.google.protobuf.Internal.EnumLite,
                com.google.protobuf.AbstractMessage.InternalOneOfEnum {

        PHONE(4),
        EMAIL(5),
        CONTACT_NOT_SET(0);
        private final int value;
        private ContactCase(int value) {
            this.value = value;
        }
        /**
         * @param value The number of the enum to look for.
         * @return The enum associated with the given number.
         * @deprecated Use {@link #forNumber(int)} instead.
         */
        @java.lang.Deprecated
        public static ContactCase valueOf(int value) {
            return forNumber(value);
        }

        public static ContactCase forNumber(int value) {
            switch (value) {
                case 4:
                    return PHONE;
                case 5:
                    return EMAIL;
                case 0:
                    return CONTACT_NOT_SET;
                default:
                    return null;
            }
        }
        public int getNumber() {
            return this.value;
        }
    };

    public ContactCase getContactCase() {
        return ContactCase.forNumber(
                contactCase_);
    }

    public static final int NAME_FIELD_NUMBER = 1;
    private volatile java.lang.Object name_;
    /**
     * <code>string name = 1;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
        java.lang.Object ref = name_;
        if (ref instanceof java.lang.String) {
            return (java.lang.String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            java.lang.String s = bs.toStringUtf8();
            name_ = s;
            return s;
        }
    }
    /**
     * <code>string name = 1;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getNameBytes() {
        java.lang.Object ref = name_;
        if (ref instanceof java.lang.String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (java.lang.String) ref);
            name_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public static final int BIO_FIELD_NUMBER = 2;
    private volatile java.lang.Object bio_;
    /**
     * <code>optional string bio = 2;</code>
     * @return Whether the bio field is set.
     */
    @java.lang.Override
    public boolean hasBio() {
        return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional string bio = 2;</code>
     * @return The bio.
     */
    @java.lang.Override
    public java.lang.String getBio() {
        java.lang.Object ref = bio_;
        if (ref instanceof java.lang.String) {
            return (java.lang.String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            java.lang.String s = bs.toStringUtf8();
            bio_ = s;
            return s;
        }
    }
    /**
     * <code>optional string bio = 2;</code>
     * @return The bytes for bio.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBioBytes() {
        java.lang.Object ref = bio_;
        if (ref instanceof java.lang.String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (java.lang.String) ref);
            bio_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public static final int AGE_FIELD_NUMBER = 3;
    private int age_;
    /**
     * <code>optional int32 age = 3;</code>
     * @return Whether the age field is set.
     */
    @java.lang.Override
    public boolean hasAge() {
        return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional int32 age = 3;</code>
     * @return The age.
     */
    @java.lang.Override
    public int getAge() {
        return age_;
    }

    public static final int PHONE_FIELD_NUMBER = 4;
    /**
     * <code>string phone = 4;</code>
     * @return Whether the phone field is set.
     */
    public boolean hasPhone() {
        return contactCase_ == 4;
    }
    /**
     * <code>string phone = 4;</code>
     * @return The phone.
     */
    public java.lang.String getPhone() {
        java.lang.Object ref = "";
        if (contactCase_ == 4) {
            ref = contact_;
        }
        if (ref instanceof java.lang.String) {
            return (java.lang.String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            java.lang.String s = bs.toStringUtf8();
            if (contactCase_ == 4) {
                contact_ = s;
            }
            return s;
        }
    }
    /**
     * <code>string phone = 4;</code>
     * @return The bytes for phone.
     */
    public com.google.protobuf.ByteString getPhoneBytes() {
        java.lang.Object ref = "";
        if (contactCase_ == 4) {
            ref = contact_;
        }
        if (ref instanceof java.lang.String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (java.lang.String) ref);
            if (contactCase_ == 4) {
                contact_ = b;
            }
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public static final int EMAIL_FIELD_NUMBER = 5;
    /**
     * <code>string email = 5;</code>
     * @return Whether the email field is set.
     */
    public boolean hasEmail() {
        return contactCase_ == 5;
    }
    /**
     * <code>string email = 5;</code>
     * @return The email.
     */
    public java.lang.String getEmail() {
        java.lang.Object ref = "";
        if (contactCase_ == 5) {
            ref = contact_;
        }
        if (ref instanceof java.lang.String) {
            return (java.lang.String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            java.lang.String s = bs.toStringUtf8();
            if (contactCase_ == 5) {
                contact_ = s;
            }
            return s;
        }
    }
    /**
     * <code>string email = 5;</code>
     * @return The bytes for email.
     */
    public com.google.protobuf.ByteString getEmailBytes() {
        java.lang.Object ref = "";
        if (contactCase_ == 5) {
            ref = contact_;
        }
        if (ref instanceof java.lang.String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (java.lang.String) ref);
            if (contactCase_ == 5) {
                contact_ = b;
            }
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
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
        if (!getNameBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 1, name_);
        }
        if (((bitField0_ & 0x00000001) != 0)) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 2, bio_);
        }
        if (((bitField0_ & 0x00000002) != 0)) {
            output.writeInt32(3, age_);
        }
        if (contactCase_ == 4) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 4, contact_);
        }
        if (contactCase_ == 5) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 5, contact_);
        }
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        if (!getNameBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, name_);
        }
        if (((bitField0_ & 0x00000001) != 0)) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, bio_);
        }
        if (((bitField0_ & 0x00000002) != 0)) {
            size += com.google.protobuf.CodedOutputStream
                    .computeInt32Size(3, age_);
        }
        if (contactCase_ == 4) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, contact_);
        }
        if (contactCase_ == 5) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, contact_);
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
        if (!(obj instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType)) {
            return super.equals(obj);
        }
        org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType other =
                (org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType) obj;

        if (!getName()
                .equals(other.getName()))
            return false;
        if (hasBio() != other.hasBio())
            return false;
        if (hasBio()) {
            if (!getBio()
                    .equals(other.getBio()))
                return false;
        }
        if (hasAge() != other.hasAge())
            return false;
        if (hasAge()) {
            if (getAge() != other.getAge())
                return false;
        }
        if (!getContactCase().equals(other.getContactCase()))
            return false;
        switch (contactCase_) {
            case 4:
                if (!getPhone()
                        .equals(other.getPhone()))
                    return false;
                break;
            case 5:
                if (!getEmail()
                        .equals(other.getEmail()))
                    return false;
                break;
            case 0:
            default:
        }
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
        hash = (37 * hash) + NAME_FIELD_NUMBER;
        hash = (53 * hash) + getName().hashCode();
        if (hasBio()) {
            hash = (37 * hash) + BIO_FIELD_NUMBER;
            hash = (53 * hash) + getBio().hashCode();
        }
        if (hasAge()) {
            hash = (37 * hash) + AGE_FIELD_NUMBER;
            hash = (53 * hash) + getAge();
        }
        switch (contactCase_) {
            case 4:
                hash = (37 * hash) + PHONE_FIELD_NUMBER;
                hash = (53 * hash) + getPhone().hashCode();
                break;
            case 5:
                hash = (37 * hash) + EMAIL_FIELD_NUMBER;
                hash = (53 * hash) + getEmail().hashCode();
                break;
            case 0:
            default:
        }
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             java.nio.ByteBuffer data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             com.google.protobuf.ByteString data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             byte[] data,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             java.io.InputStream input,
                                                                                             com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseDelimitedFrom(
                                                                                                      java.io.InputStream input,
                                                                                                      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
                                                                                             com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }
    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parseFrom(
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
    public static Builder newBuilder(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType prototype) {
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
     * Protobuf type {@code org.apache.dolphinscheduler.task.grpc.proto.OneofType}
     */
    public static final class Builder
            extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
                // @@protoc_insertion_point(builder_implements:org.apache.dolphinscheduler.task.grpc.proto.OneofType)
                org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofTypeOrBuilder {

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.class,
                            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.Builder.class);
        }

        // Construct using org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.newBuilder()
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
            name_ = "";

            bio_ = "";
            bitField0_ = (bitField0_ & ~0x00000001);
            age_ = 0;
            bitField0_ = (bitField0_ & ~0x00000002);
            contactCase_ = 0;
            contact_ = null;
            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType getDefaultInstanceForType() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.getDefaultInstance();
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType build() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType buildPartial() {
            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType result =
                    new org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType(this);
            int from_bitField0_ = bitField0_;
            int to_bitField0_ = 0;
            result.name_ = name_;
            if (((from_bitField0_ & 0x00000001) != 0)) {
                to_bitField0_ |= 0x00000001;
            }
            result.bio_ = bio_;
            if (((from_bitField0_ & 0x00000002) != 0)) {
                result.age_ = age_;
                to_bitField0_ |= 0x00000002;
            }
            if (contactCase_ == 4) {
                result.contact_ = contact_;
            }
            if (contactCase_ == 5) {
                result.contact_ = contact_;
            }
            result.bitField0_ = to_bitField0_;
            result.contactCase_ = contactCase_;
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
            if (other instanceof org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType) {
                return mergeFrom((org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType other) {
            if (other == org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.getDefaultInstance())
                return this;
            if (!other.getName().isEmpty()) {
                name_ = other.name_;
                onChanged();
            }
            if (other.hasBio()) {
                bitField0_ |= 0x00000001;
                bio_ = other.bio_;
                onChanged();
            }
            if (other.hasAge()) {
                setAge(other.getAge());
            }
            switch (other.getContactCase()) {
                case PHONE: {
                    contactCase_ = 4;
                    contact_ = other.contact_;
                    onChanged();
                    break;
                }
                case EMAIL: {
                    contactCase_ = 5;
                    contact_ = other.contact_;
                    onChanged();
                    break;
                }
                case CONTACT_NOT_SET: {
                    break;
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
            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage =
                        (org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }
        private int contactCase_ = 0;
        private java.lang.Object contact_;
        public ContactCase getContactCase() {
            return ContactCase.forNumber(
                    contactCase_);
        }

        public Builder clearContact() {
            contactCase_ = 0;
            contact_ = null;
            onChanged();
            return this;
        }

        private int bitField0_;

        private java.lang.Object name_ = "";
        /**
         * <code>string name = 1;</code>
         * @return The name.
         */
        public java.lang.String getName() {
            java.lang.Object ref = name_;
            if (!(ref instanceof java.lang.String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                name_ = s;
                return s;
            } else {
                return (java.lang.String) ref;
            }
        }
        /**
         * <code>string name = 1;</code>
         * @return The bytes for name.
         */
        public com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        /**
         * <code>string name = 1;</code>
         * @param value The name to set.
         * @return This builder for chaining.
         */
        public Builder setName(
                               java.lang.String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            name_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>string name = 1;</code>
         * @return This builder for chaining.
         */
        public Builder clearName() {

            name_ = getDefaultInstance().getName();
            onChanged();
            return this;
        }
        /**
         * <code>string name = 1;</code>
         * @param value The bytes for name to set.
         * @return This builder for chaining.
         */
        public Builder setNameBytes(
                                    com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);

            name_ = value;
            onChanged();
            return this;
        }

        private java.lang.Object bio_ = "";
        /**
         * <code>optional string bio = 2;</code>
         * @return Whether the bio field is set.
         */
        public boolean hasBio() {
            return ((bitField0_ & 0x00000001) != 0);
        }
        /**
         * <code>optional string bio = 2;</code>
         * @return The bio.
         */
        public java.lang.String getBio() {
            java.lang.Object ref = bio_;
            if (!(ref instanceof java.lang.String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                bio_ = s;
                return s;
            } else {
                return (java.lang.String) ref;
            }
        }
        /**
         * <code>optional string bio = 2;</code>
         * @return The bytes for bio.
         */
        public com.google.protobuf.ByteString getBioBytes() {
            java.lang.Object ref = bio_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                bio_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        /**
         * <code>optional string bio = 2;</code>
         * @param value The bio to set.
         * @return This builder for chaining.
         */
        public Builder setBio(
                              java.lang.String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            bitField0_ |= 0x00000001;
            bio_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>optional string bio = 2;</code>
         * @return This builder for chaining.
         */
        public Builder clearBio() {
            bitField0_ = (bitField0_ & ~0x00000001);
            bio_ = getDefaultInstance().getBio();
            onChanged();
            return this;
        }
        /**
         * <code>optional string bio = 2;</code>
         * @param value The bytes for bio to set.
         * @return This builder for chaining.
         */
        public Builder setBioBytes(
                                   com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);
            bitField0_ |= 0x00000001;
            bio_ = value;
            onChanged();
            return this;
        }

        private int age_;
        /**
         * <code>optional int32 age = 3;</code>
         * @return Whether the age field is set.
         */
        @java.lang.Override
        public boolean hasAge() {
            return ((bitField0_ & 0x00000002) != 0);
        }
        /**
         * <code>optional int32 age = 3;</code>
         * @return The age.
         */
        @java.lang.Override
        public int getAge() {
            return age_;
        }
        /**
         * <code>optional int32 age = 3;</code>
         * @param value The age to set.
         * @return This builder for chaining.
         */
        public Builder setAge(int value) {
            bitField0_ |= 0x00000002;
            age_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>optional int32 age = 3;</code>
         * @return This builder for chaining.
         */
        public Builder clearAge() {
            bitField0_ = (bitField0_ & ~0x00000002);
            age_ = 0;
            onChanged();
            return this;
        }

        /**
         * <code>string phone = 4;</code>
         * @return Whether the phone field is set.
         */
        @java.lang.Override
        public boolean hasPhone() {
            return contactCase_ == 4;
        }
        /**
         * <code>string phone = 4;</code>
         * @return The phone.
         */
        @java.lang.Override
        public java.lang.String getPhone() {
            java.lang.Object ref = "";
            if (contactCase_ == 4) {
                ref = contact_;
            }
            if (!(ref instanceof java.lang.String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (contactCase_ == 4) {
                    contact_ = s;
                }
                return s;
            } else {
                return (java.lang.String) ref;
            }
        }
        /**
         * <code>string phone = 4;</code>
         * @return The bytes for phone.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString getPhoneBytes() {
            java.lang.Object ref = "";
            if (contactCase_ == 4) {
                ref = contact_;
            }
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                if (contactCase_ == 4) {
                    contact_ = b;
                }
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        /**
         * <code>string phone = 4;</code>
         * @param value The phone to set.
         * @return This builder for chaining.
         */
        public Builder setPhone(
                                java.lang.String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            contactCase_ = 4;
            contact_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>string phone = 4;</code>
         * @return This builder for chaining.
         */
        public Builder clearPhone() {
            if (contactCase_ == 4) {
                contactCase_ = 0;
                contact_ = null;
                onChanged();
            }
            return this;
        }
        /**
         * <code>string phone = 4;</code>
         * @param value The bytes for phone to set.
         * @return This builder for chaining.
         */
        public Builder setPhoneBytes(
                                     com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);
            contactCase_ = 4;
            contact_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>string email = 5;</code>
         * @return Whether the email field is set.
         */
        @java.lang.Override
        public boolean hasEmail() {
            return contactCase_ == 5;
        }
        /**
         * <code>string email = 5;</code>
         * @return The email.
         */
        @java.lang.Override
        public java.lang.String getEmail() {
            java.lang.Object ref = "";
            if (contactCase_ == 5) {
                ref = contact_;
            }
            if (!(ref instanceof java.lang.String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (contactCase_ == 5) {
                    contact_ = s;
                }
                return s;
            } else {
                return (java.lang.String) ref;
            }
        }
        /**
         * <code>string email = 5;</code>
         * @return The bytes for email.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString getEmailBytes() {
            java.lang.Object ref = "";
            if (contactCase_ == 5) {
                ref = contact_;
            }
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                if (contactCase_ == 5) {
                    contact_ = b;
                }
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        /**
         * <code>string email = 5;</code>
         * @param value The email to set.
         * @return This builder for chaining.
         */
        public Builder setEmail(
                                java.lang.String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            contactCase_ = 5;
            contact_ = value;
            onChanged();
            return this;
        }
        /**
         * <code>string email = 5;</code>
         * @return This builder for chaining.
         */
        public Builder clearEmail() {
            if (contactCase_ == 5) {
                contactCase_ = 0;
                contact_ = null;
                onChanged();
            }
            return this;
        }
        /**
         * <code>string email = 5;</code>
         * @param value The bytes for email to set.
         * @return This builder for chaining.
         */
        public Builder setEmailBytes(
                                     com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);
            contactCase_ = 5;
            contact_ = value;
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

        // @@protoc_insertion_point(builder_scope:org.apache.dolphinscheduler.task.grpc.proto.OneofType)
    }

    // @@protoc_insertion_point(class_scope:org.apache.dolphinscheduler.task.grpc.proto.OneofType)
    private static final org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType();
    }

    public static org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<OneofType> PARSER =
            new com.google.protobuf.AbstractParser<OneofType>() {

                @java.lang.Override
                public OneofType parsePartialFrom(
                                                  com.google.protobuf.CodedInputStream input,
                                                  com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                    return new OneofType(input, extensionRegistry);
                }
            };

    public static com.google.protobuf.Parser<OneofType> parser() {
        return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<OneofType> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}
