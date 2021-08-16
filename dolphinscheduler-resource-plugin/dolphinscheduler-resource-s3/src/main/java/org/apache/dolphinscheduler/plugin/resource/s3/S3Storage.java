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

package org.apache.dolphinscheduler.plugin.resource.s3;

import org.apache.dolphinscheduler.spi.resource.ResourceStorage;
import org.apache.dolphinscheduler.spi.resource.ResourceStorageException;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

/**
 * S3 Resource storage,
 */
public class S3Storage implements ResourceStorage {

    private AmazonS3Client s3Client;

    private String bucketName;

    @Override
    public void init(Map<String, String> config) {
        AWSCredentialsProvider credentials = createAwsCredentialsProvider(config);
        bucketName = S3StorageConfiguration.S3_BUCKET_NAME.getParameterValue(config.get(S3StorageConfiguration.S3_BUCKET_NAME.getName()));
        String endpoint = config.get(S3StorageConfiguration.S3_ENDPOINT.getName());
        if (StringUtils.isBlank(endpoint)) {
            throw new ResourceStorageException("not config s3 endpoint");
        }
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(S3StorageConfiguration.S3_CONNECTION_TIME_OUT_MS.getParameterValue(config.get(S3StorageConfiguration.S3_CONNECTION_TIME_OUT_MS.getName())));
        clientConfiguration.setMaxConnections(S3StorageConfiguration.S3_MAX_CONNECTIONS.getParameterValue(config.get(S3StorageConfiguration.S3_MAX_CONNECTIONS.getName())));
        clientConfiguration.setSocketTimeout(S3StorageConfiguration.S3_SOCKET_TIME_OUT_MS.getParameterValue(config.get(S3StorageConfiguration.S3_SOCKET_TIME_OUT_MS.getName())));
        clientConfiguration.setMaxErrorRetry(S3StorageConfiguration.S3_MAX_ERROR_RETRY.getParameterValue(config.get(S3StorageConfiguration.S3_MAX_ERROR_RETRY.getName())));
        String protocolString = com.amazonaws.util.StringUtils.upperCase(config.get(S3StorageConfiguration.S3_PROTOCOL.getName()));
        Protocol protocol = Protocol.valueOf(protocolString);
        clientConfiguration.setProtocol(protocol);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentials).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("endpoint", endpoint))
                .withClientConfiguration(clientConfiguration).build();

    }

    private static AWSCredentialsProvider createAwsCredentialsProvider(Map<String, String> config) {
        String secretKey = config.get(S3StorageConfiguration.S3_SECRET_KEY.getName());
        String accessKey = config.get(S3StorageConfiguration.S3_ACCESS_KEY.getName());
        if (StringUtils.isNotBlank(secretKey) && StringUtils.isNotBlank(accessKey)) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                    accessKey, secretKey));
        }
        return new DefaultAWSCredentialsProviderChain();
    }

    @Override
    public byte[] catFile(String filePath) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, filePath);
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (IOException e) {
            throw new ResourceStorageException("cat S3 file %s error!", e, filePath);
        }
    }

    @Override
    public List<String> catFile(String filePath, int skipLineNums, int limit) {
        S3Object s3Object = s3Client.getObject(bucketName, filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8));
        Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
        return stream.collect(Collectors.toList());
    }

    @Override
    public boolean deleteFile(String filePath, Boolean recursive) {
        s3Client.deleteObject(bucketName, filePath);
        return true;
    }

    @Override
    public boolean exists(String filePath) {
        return s3Client.doesObjectExist(bucketName, filePath);
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        if (!s3Client.doesObjectExist(bucketName, oldPath)) {
            throw new ResourceStorageException(String.format("S3 rename file error,source file %s not exits", oldPath));
        }
        s3Client.copyObject(bucketName, oldPath, bucketName, newPath);
        s3Client.deleteObject(bucketName, oldPath);
        return true;
    }

    @Override
    public boolean uploadLocalFile(String localFileName, String resourceStorageName, boolean overwrite) {
        s3Client.putObject(bucketName, resourceStorageName, new File(localFileName));
        return true;
    }

    @Override
    public boolean downloadFileToLocal(String resourceFilePath, String localFilePath) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, resourceFilePath);
        s3Client.getObject(getObjectRequest, new File(localFilePath));
        return true;
    }

    @Override
    public boolean copyFile(String filePath, String targetFilePath, boolean overwrite,boolean deleteSource) {

        if (!s3Client.doesObjectExist(bucketName, filePath)) {
            throw new ResourceStorageException(String.format("S3 copy file error,source file %s not exits", filePath));
        }
        if (!overwrite && s3Client.doesObjectExist(bucketName, targetFilePath)) {
            throw new ResourceStorageException(String.format("S3 copy file error,target file %s already exits", targetFilePath));
        }
        s3Client.copyObject(bucketName, filePath, bucketName, filePath);
        s3Client.deleteObject(bucketName,filePath);
        return true;
    }
}
