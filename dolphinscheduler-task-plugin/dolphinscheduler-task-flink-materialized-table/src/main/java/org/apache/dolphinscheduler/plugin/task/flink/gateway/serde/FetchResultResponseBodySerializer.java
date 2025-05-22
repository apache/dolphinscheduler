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

package org.apache.dolphinscheduler.plugin.task.flink.gateway.serde;

import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBodyImpl;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.NotReadyFetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.Row;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FetchResultResponseBodySerializer extends JsonSerializer<FetchResultResponseBody> {

    @Override
    public void serialize(FetchResultResponseBody value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (value instanceof NotReadyFetchResultResponseBody) {
            NotReadyFetchResultResponseBody notReady = (NotReadyFetchResultResponseBody) value;
            gen.writeStringField("resultType", "NOT_READY");
            gen.writeStringField("nextResultUri", notReady.getNextResultUri());
        } else if (value instanceof FetchResultResponseBodyImpl) {
            FetchResultResponseBodyImpl payload = (FetchResultResponseBodyImpl) value;
            gen.writeStringField("resultType", "PAYLOAD");
            gen.writeStringField("nextResultUri", payload.getNextResultUri());
            gen.writeStringField("jobID", payload.getJobId());

            gen.writeObjectFieldStart("results");
            gen.writeStringField("rowFormat", "JSON");
            gen.writeArrayFieldStart("data");
            for (Row row : payload.getResult()) {
                gen.writeStartObject();
                gen.writeArrayFieldStart("fields");
                for (String field : row.getValues()) {
                    gen.writeString(field);
                }
                gen.writeEndArray();
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        gen.writeEndObject();
    }
}
