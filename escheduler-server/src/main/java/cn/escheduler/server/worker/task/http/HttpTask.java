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


import cn.escheduler.common.enums.Direct;
import cn.escheduler.common.enums.HttpMethod;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.http.HttpParameters;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import com.google.common.xml.XmlEscapers;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

/**
 * api task
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
    protected static final int MS_TO_S_UNIT = 1000;

    /**
     * https prefix
     */
    protected static final String HTTPS = "https";

    protected static final String CONTENT_TYPE = "Content-Type";

    protected static final String APPLICATION_JSON = "application/json";

    protected static HttpsTrustManager httpsTrustManager = new HttpsTrustManager();

    protected String output;

    /**
     * cookie保存
     */
    CookieStore cookieStore = new BasicCookieStore();

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
        boolean status = true;
        try {
            CloseableHttpResponse response = sendRequest();
            statusCode = String.valueOf(getStatusCode(response));
            body = getResponseBody(response);
            response.close();
            exitStatusCode = validResponse(body, statusCode);
            long costTime = System.currentTimeMillis() - startTime;
            status = StringUtils.isEmpty(output);
            logger.info("startTime: {}, httpUrl: {}, httpMethod: {}, status : {}, costTime : {}Millisecond, statusCode : {}, body : {}, log : {}",
                    DateUtils.format2Readable(startTime), httpParameters.getUrl(),httpParameters.getHttpMethod(), status, costTime, statusCode, body, output);
        } catch (Exception e) {
            appendMessage(e.toString());
            exitStatusCode = -1;
            logger.error("httpUrl[" + httpParameters.getUrl() + "] connection failed："+output, e);
        }
    }

    protected CloseableHttpResponse sendRequest() throws ClientProtocolException, IOException {
        RequestBuilder builder = createRequestBuilder();
        ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

        Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                taskProps.getDefinedParams(),
                httpParameters.getLocalParametersMap(),
                processInstance.getCmdTypeIfComplement(),
                processInstance.getScheduleTime());

        addRequestParams(builder,paramsMap);
        HttpUriRequest request = builder.setUri(httpParameters.getUrl()).build();
        setHeaders(request,paramsMap);
        CloseableHttpClient client = createHttpClient();
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
        String webPage = EntityUtils.toString(entity, "UTF-8");
        return webPage;
    }

    protected int getStatusCode(CloseableHttpResponse httpResponse) {
        int status = httpResponse.getStatusLine().getStatusCode();
        return status;
    }

    protected int validResponse(String body, String statusCode) throws Exception {
        int exitStatusCode = 0;
        switch (httpParameters.getHttpCheckCondition()) {
            case CONTAINS:
                if (StringUtils.isEmpty(body) || !body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " doesn contain "
                            + XmlEscapers.xmlContentEscaper().escape(httpParameters.getCondition()));
                    exitStatusCode = -1;
                }
                break;
            case DOESNT_CONTAIN:
                if (StringUtils.isEmpty(body) || body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " contains "
                            + XmlEscapers.xmlContentEscaper().escape(httpParameters.getCondition()));
                    exitStatusCode = -1;
                }
                break;
            case STATUSCODE:
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

    protected void addRequestParams(RequestBuilder builder,Map<String, Property> paramsMap) {
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();
            if (property.getValue() != null && property.getValue().length() > 0){
                if (property.getDirect().equals(Direct.IN)){
                    builder.addParameter(property.getProp(), property.getValue());
                }else{
                    if(CONTENT_TYPE.equals(property.getProp()) && property.getValue().contains(APPLICATION_JSON)){
                        StringEntity postingString = null;
                        try {
                            postingString = new StringEntity("{}");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        builder.setEntity(postingString);
                    }
                }
            }
        }
    }

    protected void setHeaders(HttpUriRequest request,Map<String, Property> paramsMap) {
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();
            if (property.getValue() != null && property.getValue().length() > 0){
                if (property.getDirect().equals(Direct.OUT)){
                    request.addHeader(property.getProp(), property.getValue());
                }
            }
        }
    }

    protected CloseableHttpClient createHttpClient() {
        final RequestConfig requestConfig = requestConfig();
        HttpClientBuilder httpClientBuilder;
        if (isHttps()) {
            // Support SSL
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(createSSLContext());
            httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslConnectionSocketFactory);
        } else {
            httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        return httpClientBuilder.build();
    }

    private RequestConfig requestConfig() {
        final int maxConnMillSeconds = httpParameters.getMaxConnectionSeconds() * MS_TO_S_UNIT;
        return RequestConfig.custom().setSocketTimeout(maxConnMillSeconds).setConnectTimeout(maxConnMillSeconds).build();
    }

    private SSLContext createSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new HttpsTrustManager[]{httpsTrustManager}, null);
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Create SSLContext error", e);
        }
    }

    protected boolean isHttps() {
        return httpParameters.getUrl().toLowerCase().startsWith(HTTPS);
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

    /**
     * Default X509TrustManager implement
     */
    private static class HttpsTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // ignore
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // ignore
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static String xmlReplaceChar(String xml) {
        return xml.replaceAll("&nbsp;", "&amp;nbsp;").replaceAll("&raquo;", "&amp;raquo;");
    }

    @Override
    public AbstractParameters getParameters() {
        return this.httpParameters;
    }
}
