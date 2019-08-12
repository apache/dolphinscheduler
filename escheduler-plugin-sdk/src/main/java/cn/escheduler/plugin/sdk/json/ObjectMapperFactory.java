/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import cn.escheduler.plugin.sdk.credential.ClearCredentialValue;
import cn.escheduler.plugin.api.impl.ErrorMessage;

import java.time.ZonedDateTime;

public class ObjectMapperFactory {

    private static final ObjectMapper OBJECT_MAPPER = create(true);
    private static final ObjectMapper OBJECT_MAPPER_ONE_LINE = create(false);

    private ObjectMapperFactory() {}

    private static ObjectMapper create(boolean indent) {
        ObjectMapper objectMapper = MetricsObjectMapperFactory.create(indent);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ErrorMessage.class, new ErrorMessageDeserializer());
        module.addSerializer(ClearCredentialValue.class, new ClearCredentialValueSerializer());
        module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    public static ObjectMapper get() {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper getOneLine() {
        return OBJECT_MAPPER_ONE_LINE;
    }

}
