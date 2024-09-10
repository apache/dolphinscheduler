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

package org.apache.dolphinscheduler.authentication.aws;

import static org.apache.dolphinscheduler.authentication.aws.AwsConfigurationKeys.AWS_AUTHENTICATION_TYPE;

import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;

@Slf4j
@UtilityClass
public class AWSCredentialsProviderFactor {

    public static AWSCredentialsProvider credentialsProvider(Map<String, String> awsProperties) {
        String awsAuthenticationType = awsProperties.getOrDefault(
                AWS_AUTHENTICATION_TYPE, AWSCredentialsProviderType.STATIC_CREDENTIALS_PROVIDER.getName());
        AWSCredentialsProviderType awsCredentialsProviderType =
                AWSCredentialsProviderType.of(awsAuthenticationType).orElse(null);
        if (awsCredentialsProviderType == null) {
            throw new IllegalArgumentException(
                    "The aws.credentials.provider.type: " + awsAuthenticationType + " is invalidated");
        }
        switch (awsCredentialsProviderType) {
            case STATIC_CREDENTIALS_PROVIDER:
                return createAWSStaticCredentialsProvider(awsProperties);
            case INSTANCE_PROFILE_CREDENTIALS_PROVIDER:
                return createInstanceProfileCredentialsProvider();
            default:
                throw new IllegalArgumentException(
                        "The aws.credentials.provider.type: " + awsAuthenticationType + " is invalidated");
        }

    }

    private static AWSCredentialsProvider createAWSStaticCredentialsProvider(Map<String, String> awsProperties) {
        String awsAccessKeyId = awsProperties.get(AwsConfigurationKeys.AWS_ACCESS_KEY_ID);
        String awsSecretAccessKey = awsProperties.get(AwsConfigurationKeys.AWS_SECRET);
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
        AWSStaticCredentialsProvider awsStaticCredentialsProvider =
                new AWSStaticCredentialsProvider(basicAWSCredentials);
        log.info("AWSStaticCredentialsProvider created successfully");
        return awsStaticCredentialsProvider;
    }

    private static AWSCredentialsProvider createInstanceProfileCredentialsProvider() {
        InstanceProfileCredentialsProvider instanceProfileCredentialsProvider =
                InstanceProfileCredentialsProvider.getInstance();
        log.info("InstanceProfileCredentialsProvider created successfully");
        return instanceProfileCredentialsProvider;
    }

}
