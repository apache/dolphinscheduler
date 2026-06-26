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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.s3.AmazonS3;

class AmazonS3ClientFactoryTest {

    @Test
    void createAmazonS3ClientShouldAllowCustomRegionWhenEndpointConfigured() {
        Map<String, String> awsProperties = createAwsProperties("internal-region", "http://127.0.0.1:9000");

        AmazonS3 amazonS3 = assertDoesNotThrow(() -> AmazonS3ClientFactory.createAmazonS3Client(awsProperties));

        amazonS3.shutdown();
    }

    @Test
    void createAmazonS3ClientShouldValidateRegionWhenEndpointNotConfigured() {
        Map<String, String> awsProperties = createAwsProperties("internal-region", "");

        assertThrows(IllegalArgumentException.class, () -> AmazonS3ClientFactory.createAmazonS3Client(awsProperties));
    }

    @Test
    void createAmazonS3ClientShouldRejectEmptyRegionWhenEndpointConfigured() {
        assertInvalidRegionWithEndpoint(null);
        assertInvalidRegionWithEndpoint("");
    }

    private void assertInvalidRegionWithEndpoint(String region) {
        Map<String, String> awsProperties = createAwsProperties(region, "http://127.0.0.1:9000");

        assertThrows(IllegalArgumentException.class, () -> AmazonS3ClientFactory.createAmazonS3Client(awsProperties));
    }

    private Map<String, String> createAwsProperties(String region, String endpoint) {
        Map<String, String> awsProperties = new HashMap<>();
        awsProperties.put(AwsConfigurationKeys.AWS_AUTHENTICATION_TYPE,
                AWSCredentialsProviderType.STATIC_CREDENTIALS_PROVIDER.getName());
        awsProperties.put(AwsConfigurationKeys.AWS_ACCESS_KEY_ID, "access-key");
        awsProperties.put(AwsConfigurationKeys.AWS_SECRET, "secret-key");
        awsProperties.put(AwsConfigurationKeys.AWS_REGION, region);
        awsProperties.put(AwsConfigurationKeys.AWS_ENDPOINT, endpoint);
        return awsProperties;
    }
}
