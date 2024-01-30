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

package org.apache.dolphinscheduler.plugin.datasource.sagemaker;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sagemaker.AmazonSageMaker;
import com.amazonaws.services.sagemaker.AmazonSageMakerClientBuilder;
import com.amazonaws.services.sagemaker.model.ListNotebookInstancesRequest;

@Slf4j
public class SagemakerClientWrapper implements AutoCloseable {

    private AmazonSageMaker amazonSageMaker;

    public SagemakerClientWrapper(String accessKey, String secretAccessKey, String region) {
        checkNotNull(accessKey, "sagemaker accessKey cannot be null");
        checkNotNull(secretAccessKey, "sagemaker secretAccessKey cannot be null");
        checkNotNull(region, "sagemaker region cannot be null");

        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
        final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
        // create a SageMaker client
        amazonSageMaker = AmazonSageMakerClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withRegion(region).build();
    }

    public boolean checkConnect() {
        try {
            // If listing notebook instances fails, an exception will be thrown directly
            ListNotebookInstancesRequest request = new ListNotebookInstancesRequest();
            amazonSageMaker.listNotebookInstances(request);
            log.info("sagemaker client connects to server successfully");
            return true;
        } catch (Exception e) {
            log.info("sagemaker client failed to connect to the server");
        }
        return false;
    }

    @Override
    public void close() {

    }
}
