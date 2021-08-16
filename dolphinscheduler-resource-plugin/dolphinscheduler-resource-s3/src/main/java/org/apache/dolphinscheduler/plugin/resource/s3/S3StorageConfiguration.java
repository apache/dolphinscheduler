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

import java.util.function.Function;

public enum S3StorageConfiguration {
    S3_ACCESS_KEY ("aws.access.key.id",null, value -> value),
    S3_SECRET_KEY ( "aws.secret.key",null, value -> value),
    S3_ENDPOINT("s3.endpoint",null,value->value),
    S3_BUCKET_NAME("s3.bucket.name","dolphinscheduler",value->value),
    S3_MAX_ERROR_RETRY("s3.max.error.retry",3,value->value),
    S3_MAX_CONNECTIONS("s3.max.connections",50,value->value),
    S3_SOCKET_TIME_OUT_MS("s3.socket.time.out.ms",50000,value->value),
    S3_CONNECTION_TIME_OUT_MS("s3.connection.timeout",50000,value->value),
    S3_PROTOCOL("s3.protocol","https",value->value)
    ;

    <T> S3StorageConfiguration(String name, T defaultValue, Function<String, T> converter) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.converter = (Function<String, Object>) converter;
    }

    public <T> T getParameterValue(String param) {
        Object value = param != null ? converter.apply(param) : defaultValue;
        return (T) value;
    }


    private final String name;

    public String getName() {
        return name;
    }

    private final Object defaultValue;

    private final Function<String, Object> converter;
}
