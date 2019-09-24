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
package cn.escheduler.server.worker.task.http;


import cn.escheduler.common.enums.HttpMethod;
import cn.escheduler.common.enums.HttpParametersType;
import cn.escheduler.common.process.HttpProperty;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.http.HttpParameters;
import cn.escheduler.common.utils.Bytes;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http task
 */
public class HttpTask extends AbstractTask {

    private HttpParameters httpParameters;

    /**
     *  process database access
     */
    private ProcessDao processDao;

    /**
     * Convert mill seconds to second unit
     */
    protected static final int MAX_CONNECTION_MILLISECONDS = 60000;

    protected static final String APPLICATION_JSON = "application/json";

    protected String output;


    public HttpTask(TaskProps props, Logger logger) {
        super(props, logger);
        this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
    }

    @Override
    public void init() {
        logger.info("http task params {}", taskProps.getTaskParams());
        this.httpParameters = JSONObject.parseObject(taskProps.getTaskParams(), HttpParameters.class);

        if (!httpParameters.checkParameters()) {
            throw new RuntimeException("http task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        long startTime = System.currentTimeMillis();
        String statusCode = null;
        String body = null;
        try(CloseableHttpClient client = createHttpClient()) {
            try(CloseableHttpResponse response = sendRequest(client)) {
                statusCode = String.valueOf(getStatusCode(response));
                body = getResponseBody(response);
                exitStatusCode = validResponse(body, statusCode);
                long costTime = System.currentTimeMillis() - startTime;
                logger.info("startTime: {}, httpUrl: {}, httpMethod: {}, costTime : {}Millisecond, statusCode : {}, body : {}, log : {}",
                        DateUtils.format2Readable(startTime), httpParameters.getUrl(),httpParameters.getHttpMethod(), costTime, statusCode, body, output);
            }catch (Exception e) {
                appendMessage(e.toString());
                exitStatusCode = -1;
                logger.error("httpUrl[" + httpParameters.getUrl() + "] connection failed："+output, e);
            }
        } catch (Exception e) {
            appendMessage(e.toString());
            exitStatusCode = -1;
            logger.error("httpUrl[" + httpParameters.getUrl() + "] connection failed："+output, e);
        }
    }

    protected CloseableHttpResponse sendRequest(CloseableHttpClient client) throws IOException {
        RequestBuilder builder = createRequestBuilder();
        ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

        Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                taskProps.getDefinedParams(),
                httpParameters.getLocalParametersMap(),
                processInstance.getCmdTypeIfComplement(),
                processInstance.getScheduleTime());
        List<HttpProperty> httpPropertyList = new ArrayList<>();
        if(httpParameters.getHttpParams() != null && httpParameters.getHttpParams().size() > 0){
            for (HttpProperty httpProperty: httpParameters.getHttpParams()) {
                String jsonObject = JSONObject.toJSONString(httpProperty);
                String params = ParameterUtils.convertParameterPlaceholders(jsonObject,ParamUtils.convert(paramsMap));
                logger.info("http request params：{}",params);
                httpPropertyList.add(JSONObject.parseObject(params,HttpProperty.class));
            }
        }
        addRequestParams(builder,httpPropertyList);
        HttpUriRequest request = builder.setUri(httpParameters.getUrl()).build();
        setHeaders(request,httpPropertyList);
        return client.execute(request);
    }

    protected String getResponseBody(CloseableHttpResponse httpResponse) throws ParseException, IOException {
        if (httpResponse == null) {
            return null;
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return null;
        }
        String webPage = EntityUtils.toString(entity, Bytes.UTF8_ENCODING);
        return webPage;
    }

    protected int getStatusCode(CloseableHttpResponse httpResponse) {
        int status = httpResponse.getStatusLine().getStatusCode();
        return status;
    }

    protected int validResponse(String body, String statusCode){
        int exitStatusCode = 0;
        switch (httpParameters.getHttpCheckCondition()) {
            case BODY_CONTAINS:
                if (StringUtils.isEmpty(body) || !body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " doesn contain "
                            + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            case BODY_NOT_CONTAINS:
                if (StringUtils.isEmpty(body) || body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " contains "
                            + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            case STATUS_CODE_CUSTOM:
                if (!statusCode.equals(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " statuscode: " + statusCode + ", Must be: " + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            default:
                if (!"200".equals(statusCode)) {
                    appendMessage(httpParameters.getUrl() + " statuscode: " + statusCode + ", Must be: 200");
                    exitStatusCode = -1;
                }
                break;
        }
        return exitStatusCode;
    }

    public String getOutput() {
        return output;
    }

    protected void appendMessage(String message) {
        if (output == null) {
            output = "";
        }
        if (message != null && !message.trim().isEmpty()) {
            output += message;
        }
    }

    protected void addRequestParams(RequestBuilder builder,List<HttpProperty> httpPropertyList) {
        if(httpPropertyList != null && httpPropertyList.size() > 0){
            JSONObject jsonParam = new JSONObject();
            for (HttpProperty property: httpPropertyList){
                if(property.getHttpParametersType() != null){
                    if (property.getHttpParametersType().equals(HttpParametersType.PARAMETER)){
                        builder.addParameter(property.getProp(), property.getValue());
                    }else if(property.getHttpParametersType().equals(HttpParametersType.BODY)){
                        jsonParam.put(property.getProp(), property.getValue());
                    }
                }
            }
            StringEntity postingString = new StringEntity(jsonParam.toString(), Charsets.UTF_8);
            postingString.setContentEncoding(Bytes.UTF8_ENCODING);
            postingString.setContentType(APPLICATION_JSON);
            builder.setEntity(postingString);
        }
    }

    protected void setHeaders(HttpUriRequest request,List<HttpProperty> httpPropertyList) {
        if(httpPropertyList != null && httpPropertyList.size() > 0){
            for (HttpProperty property: httpPropertyList){
                if(property.getHttpParametersType() != null) {
                    if (property.getHttpParametersType().equals(HttpParametersType.HEADERS)) {
                        request.addHeader(property.getProp(), property.getValue());
                    }
                }
            }
        }
    }

    protected CloseableHttpClient createHttpClient() {
        final RequestConfig requestConfig = requestConfig();
        HttpClientBuilder httpClientBuilder;
        httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
        return httpClientBuilder.build();
    }

    private RequestConfig requestConfig() {
        return RequestConfig.custom().setSocketTimeout(MAX_CONNECTION_MILLISECONDS).setConnectTimeout(MAX_CONNECTION_MILLISECONDS).build();
    }

    protected RequestBuilder createRequestBuilder() {
        if (httpParameters.getHttpMethod().equals(HttpMethod.GET)) {
            return RequestBuilder.get();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.POST)) {
            return RequestBuilder.post();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.HEAD)) {
            return RequestBuilder.head();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.PUT)) {
            return RequestBuilder.put();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.DELETE)) {
            return RequestBuilder.delete();
        } else {
            return null;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return this.httpParameters;
    }
}
