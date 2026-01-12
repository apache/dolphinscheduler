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

import static com.google.common.truth.Truth.assertThat;
import static org.apache.dolphinscheduler.authentication.aws.AwsConfigurationKeys.AWS_ACCESS_KEY_ID;
import static org.apache.dolphinscheduler.authentication.aws.AwsConfigurationKeys.AWS_AUTHENTICATION_TYPE;
import static org.apache.dolphinscheduler.authentication.aws.AwsConfigurationKeys.AWS_SECRET;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;

class AWSCredentialsProviderFactorTest {

    @Test
    void testCreateAWSStaticCredentialsProvider() {
        Map<String, String> awsProperties = new HashMap<>();
        awsProperties.put(AWS_AUTHENTICATION_TYPE, "AWSStaticCredentialsProvider");
        awsProperties.put(AWS_ACCESS_KEY_ID, "test-access-key");
        awsProperties.put(AWS_SECRET, "test-secret-key");

        AWSCredentialsProvider provider = AWSCredentialsProviderFactor.credentialsProvider(awsProperties);

        assertThat(provider).isInstanceOf(AWSStaticCredentialsProvider.class);
    }

    @Test
    void testCreateInstanceProfileCredentialsProvider() {
        Map<String, String> awsProperties = new HashMap<>();
        awsProperties.put(AWS_AUTHENTICATION_TYPE, "InstanceProfileCredentialsProvider");

        AWSCredentialsProvider provider = AWSCredentialsProviderFactor.credentialsProvider(awsProperties);

        assertThat(provider).isInstanceOf(InstanceProfileCredentialsProvider.class);
    }

    @Test
    void testCreateWebIdentityTokenCredentialsProvider() {
        Map<String, String> awsProperties = new HashMap<>();
        awsProperties.put(AWS_AUTHENTICATION_TYPE, "WebIdentityTokenCredentialsProvider");

        AWSCredentialsProvider provider = AWSCredentialsProviderFactor.credentialsProvider(awsProperties);

        assertThat(provider).isInstanceOf(WebIdentityTokenCredentialsProvider.class);
        assertThat(provider).isNotNull();
    }

    @Test
    void testInvalidAuthenticationType() {
        Map<String, String> awsProperties = new HashMap<>();
        awsProperties.put(AWS_AUTHENTICATION_TYPE, "INVALID_TYPE");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> AWSCredentialsProviderFactor.credentialsProvider(awsProperties));

        assertThat(exception.getMessage()).contains("The aws.credentials.provider.type: INVALID_TYPE is invalidated");
    }
}
