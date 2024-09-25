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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import lombok.extern.slf4j.Slf4j;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Whitelist constructor implementation for YAML snake.
 * Copied from Apache ShardingSphere and Apache Skywalking.
 */
@Slf4j
public final class ClassFilterConstructor extends Constructor {

    private final Class<?>[] acceptClasses;

    public ClassFilterConstructor(final Class<?>[] acceptClasses) {
        super(new LoaderOptions());
        this.acceptClasses = acceptClasses;
    }

    @Override
    protected Class<?> getClassForName(final String name) throws ClassNotFoundException {
        for (Class<? extends Object> each : acceptClasses) {
            if (name.equals(each.getName())) {
                log.info("name - {} : class - {}", name, super.getClassForName(name));
                return super.getClassForName(name);
            }
        }
        throw new IllegalArgumentException(String.format("Class is not accepted: %s", name));
    }
}
