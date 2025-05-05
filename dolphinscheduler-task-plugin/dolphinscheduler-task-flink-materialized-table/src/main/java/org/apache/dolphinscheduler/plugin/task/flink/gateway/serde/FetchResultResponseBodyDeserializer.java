package org.apache.dolphinscheduler.plugin.task.flink.gateway.serde;

import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBodyImpl;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.NotReadyFetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.Row;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for FetchResultResponseBody objects.
 */
public class FetchResultResponseBodyDeserializer extends StdDeserializer<FetchResultResponseBody> {

    private static final Logger log = LoggerFactory.getLogger(FetchResultResponseBodyDeserializer.class);

    protected FetchResultResponseBodyDeserializer() {
        super(FetchResultResponseBody.class);
    }

    @Override
    public FetchResultResponseBody deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        try {
            String resultType = node.get("resultType").asText();
            String nextResultUri = node.get("nextResultUri").asText();

            if ("NOT_READY".equals(resultType)) {
                return new NotReadyFetchResultResponseBody(nextResultUri, resultType);
            }

            if ("PAYLOAD".equals(resultType)) {
                JsonNode resultsNode = node.get("results");
                if (resultsNode != null && resultsNode.has("data")) {
                    JsonNode dataNode = resultsNode.get("data");
                    List<Row> rows = new ArrayList<>();

                    if (dataNode.isArray()) {
                        for (JsonNode rowNode : dataNode) {
                            JsonNode fieldsNode = rowNode.get("fields");
                            if (fieldsNode.isArray()) {
                                List<String> values = new ArrayList<>();
                                for (JsonNode fieldNode : fieldsNode) {
                                    values.add(fieldNode.asText());
                                }
                                rows.add(new FetchResultResponseBodyImpl.RowImpl(values));
                            }
                        }
                    }

                    String jobId = node.has("jobID") ? node.get("jobID").asText() : null;

                    return new FetchResultResponseBodyImpl(resultType, nextResultUri, rows, jobId);
                }
            }

            throw new IOException("Invalid result type or missing required fields");
        } catch (Exception e) {
            log.error("Failed to deserialize FetchResultResponseBody", e);
            throw new IOException("Failed to deserialize FetchResultResponseBody", e);
        }
    }
}
