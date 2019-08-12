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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import cn.escheduler.plugin.api.impl.ErrorMessage;

import java.io.IOException;
import java.util.Map;

public class ErrorMessageDeserializer extends JsonDeserializer<ErrorMessage> {

    @Override
    public ErrorMessage deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ErrorMessage errorMessage = null;
        Map map = jp.readValueAs(Map.class);
        if (map != null) {
            errorMessage = new ErrorMessage((String) map.get("errorCode"), (String) map.get("nonLocalized"),
                    (long) map.get("timestamp"));
        }
        return errorMessage;
    }
}
