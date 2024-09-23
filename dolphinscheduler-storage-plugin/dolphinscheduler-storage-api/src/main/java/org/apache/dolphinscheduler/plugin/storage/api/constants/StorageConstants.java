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

package org.apache.dolphinscheduler.plugin.storage.api.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class StorageConstants {

    public static final String RESOURCE_TYPE_FILE = "resources";

    /**
     * resource.hdfs.fs.defaultFS
     */
    public static final String FS_DEFAULT_FS = "resource.hdfs.fs.defaultFS";

    /**
     * hdfs defaultFS property name. Should be consistent with the property name in hdfs-site.xml
     */
    public static final String HDFS_DEFAULT_FS = "fs.defaultFS";

    /**
     * hdfs configuration
     * resource.hdfs.root.user
     */
    public static final String HDFS_ROOT_USER = "resource.hdfs.root.user";

    /**
     * hdfs/s3 configuration
     * resource.storage.upload.base.path
     */
    public static final String RESOURCE_UPLOAD_PATH = "resource.storage.upload.base.path";

    /**
     * resource storage type
     */
    public static final String RESOURCE_STORAGE_TYPE = "resource.storage.type";

    public static final String AWS_S3_BUCKET_NAME = "aws.s3.bucket.name";

    public static final String ALIBABA_CLOUD_OSS_BUCKET_NAME = "resource.alibaba.cloud.oss.bucket.name";
    public static final String ALIBABA_CLOUD_OSS_END_POINT = "resource.alibaba.cloud.oss.endpoint";

    public static final String GOOGLE_CLOUD_STORAGE_BUCKET_NAME = "resource.google.cloud.storage.bucket.name";

    public static final String GOOGLE_CLOUD_STORAGE_CREDENTIAL = "resource.google.cloud.storage.credential";

    public static final String AZURE_BLOB_STORAGE_CONNECTION_STRING = "resource.azure.blob.storage.connection.string";

    public static final String AZURE_BLOB_STORAGE_CONTAINER_NAME = "resource.azure.blob.storage.container.name";

    public static final String AZURE_BLOB_STORAGE_ACCOUNT_NAME = "resource.azure.blob.storage.account.name";

    public static final String HUAWEI_CLOUD_ACCESS_KEY_ID = "resource.huawei.cloud.access.key.id";
    public static final String HUAWEI_CLOUD_ACCESS_KEY_SECRET = "resource.huawei.cloud.access.key.secret";
    public static final String HUAWEI_CLOUD_OBS_BUCKET_NAME = "resource.huawei.cloud.obs.bucket.name";
    public static final String HUAWEI_CLOUD_OBS_END_POINT = "resource.huawei.cloud.obs.endpoint";

}
