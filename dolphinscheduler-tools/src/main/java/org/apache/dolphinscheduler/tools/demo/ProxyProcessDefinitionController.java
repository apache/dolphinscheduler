package org.apache.dolphinscheduler.tools.demo;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProxyProcessDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyProcessDefinitionController.class);
    public ProxyResult createProcessDefinition (String token,
                                                long projectCode,
                                                String name,
                                                String description,
                                                String globalParams,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson,
                                                String taskDefinitionJson,
                                                ProcessExecutionTypeEnum executionType){
        ProxyResult proxyResult = new ProxyResult();
        String ServerPort = "3000";
        String url = "http://localhost:" + ServerPort + "/dolphinscheduler/projects/" + projectCode + "/process-definition";
        String responseBody;
        Map<String, Object> requestBodyMap = new HashMap<>();

        requestBodyMap.put("name", name);
        requestBodyMap.put("description", description);
        requestBodyMap.put("globalParams", globalParams);
        requestBodyMap.put("locations", locations);
        requestBodyMap.put("timeout", timeout);
        requestBodyMap.put("tenantCode", tenantCode);
        requestBodyMap.put("taskRelationJson", taskRelationJson);
        requestBodyMap.put("taskDefinitionJson", taskDefinitionJson);
        requestBodyMap.put("executionType", executionType);

        try {
            responseBody = OkHttpUtils.demoPost(url, token, requestBodyMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        proxyResult = JSONUtils.parseObject(responseBody, ProxyResult.class);

        return proxyResult;

    }
}
