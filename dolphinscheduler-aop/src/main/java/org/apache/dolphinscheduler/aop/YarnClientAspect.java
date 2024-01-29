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

package org.apache.dolphinscheduler.aop;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class YarnClientAspect {

    /**
     * The current application report when application submitted successfully
     */
    private ApplicationReport currentApplicationReport = null;

    private final String appInfoFilePath;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public YarnClientAspect() {
        appInfoFilePath = String.format("%s/%s", System.getProperty("user.dir"), "appInfo.log");
    }

    /**
     * Trigger submitApplication when invoking YarnClientImpl.submitApplication
     *
     * @param appContext     application context when invoking YarnClientImpl.submitApplication
     * @param submittedAppId the submitted application id returned by YarnClientImpl.submitApplication
     * @throws Throwable exceptions
     */
    @AfterReturning(pointcut = "execution(ApplicationId org.apache.hadoop.yarn.client.api.impl.YarnClientImpl." +
            "submitApplication(ApplicationSubmissionContext)) && args(appContext)", returning = "submittedAppId", argNames = "appContext,submittedAppId")
    public void registerApplicationInfo(ApplicationSubmissionContext appContext, ApplicationId submittedAppId) {
        if (appInfoFilePath != null) {
            try {
                Files.write(Paths.get(appInfoFilePath),
                        Collections.singletonList(submittedAppId.toString()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND);
            } catch (IOException ioException) {
                logger.error(
                        "YarnClientAspect[registerAppInfo]: can't output current application information, because {}",
                        ioException.getMessage());
            }
        }
        logger.info("YarnClientAspect[submitApplication]: current application context {}", appContext);
        logger.info("YarnClientAspect[submitApplication]: submitted application id {}", submittedAppId);
        logger.info(
                "YarnClientAspect[submitApplication]: current application report {}", currentApplicationReport);
    }

    /**
     * Trigger getAppReport only when invoking getApplicationReport within submitApplication
     * This method will invoke many times, however, the last ApplicationReport instance assigned to currentApplicationReport
     *
     * @param appReport current application report when invoking getApplicationReport within submitApplication
     * @param appId     current application id, which is the parameter of getApplicationReport
     */
    @AfterReturning(pointcut = "cflow(execution(ApplicationId org.apache.hadoop.yarn.client.api.impl.YarnClientImpl.submitApplication(ApplicationSubmissionContext))) "
            +
            "&& !within(YarnClientAspect) && execution(ApplicationReport org.apache.hadoop.yarn.client.api.impl.YarnClientImpl.getApplicationReport(ApplicationId)) && args(appId)", returning = "appReport", argNames = "appReport,appId")
    public void registerApplicationReport(ApplicationReport appReport, ApplicationId appId) {
        currentApplicationReport = appReport;
    }
}
