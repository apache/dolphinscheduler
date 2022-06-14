/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.platform.commons.util.StringUtils.isBlank;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.jupiter.api.extension.ExtensionContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class TestDescription implements org.testcontainers.lifecycle.TestDescription {
    private static final String UNKNOWN_NAME = "unknown";

    private final ExtensionContext context;

    @Override
    public String getTestId() {
        return context.getUniqueId();
    }

    @Override
    public String getFilesystemFriendlyName() {
        final String contextId = context.getUniqueId();
        try {
            return (isBlank(contextId))
                    ? UNKNOWN_NAME
                    : URLEncoder.encode(contextId, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return UNKNOWN_NAME;
        }
    }
}
