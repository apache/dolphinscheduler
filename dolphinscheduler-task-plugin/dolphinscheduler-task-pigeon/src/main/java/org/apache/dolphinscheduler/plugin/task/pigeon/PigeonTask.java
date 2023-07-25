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

package org.apache.dolphinscheduler.plugin.task.pigeon;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * TIS DataX Task
 **/
public class PigeonTask extends AbstractRemoteTask {

    public static final String KEY_POOL_VAR_PIGEON_HOST = "p_host";
    private final TaskExecutionContext taskExecutionContext;

    private PigeonParameters parameters;
    private BizResult triggerResult;
    private final PigeonConfig config;

    public PigeonTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
        this.config = PigeonConfig.getInstance();
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() throws TaskException {
        super.init();
        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), PigeonParameters.class);
        log.info("Initialize PIGEON task params {}", JSONUtils.toPrettyJsonString(parameters));
        if (parameters == null || !parameters.checkParameters()) {
            throw new TaskException("datax task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        // Trigger PIGEON DataX pipeline
        log.info("start execute PIGEON task");
        long startTime = System.currentTimeMillis();
        String targetJobName = this.parameters.getTargetJobName();
        String host = getHost();
        try {
            final String triggerUrl = getTriggerUrl();
            final String getStatusUrl = config.getJobStatusUrl(host);
            HttpPost post = new HttpPost(triggerUrl);
            post.addHeader("appname", targetJobName);
            addFormUrlencoded(post);
            StringEntity entity = new StringEntity(config.getJobTriggerPostBody(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            ExecResult execState = null;
            int taskId;
            WebSocketClient webSocket = null;
            try (
                    CloseableHttpClient client = HttpClients.createDefault();
                    // trigger to start PIGEON dataX task
                    CloseableHttpResponse response = client.execute(post)) {
                triggerResult = processResponse(triggerUrl, response, BizResult.class);
                if (!triggerResult.isSuccess()) {
                    List<String> errormsg = triggerResult.getErrormsg();
                    StringBuffer errs = new StringBuffer();
                    if (CollectionUtils.isNotEmpty(errormsg)) {
                        errs.append(",errs:").append(errormsg.stream().collect(Collectors.joining(",")));
                    }
                    throw new Exception("trigger PIGEON job faild taskName:" + targetJobName + errs.toString());
                }
                taskId = triggerResult.getBizresult().getTaskid();

                webSocket = receiveRealtimeLog(host, targetJobName, taskId);

                setAppIds(String.valueOf(taskId));

                CloseableHttpResponse status = null;

                while (true) {
                    try {
                        post = new HttpPost(getStatusUrl);
                        entity = new StringEntity("{\n taskid: " + taskId + "\n, log: false }", StandardCharsets.UTF_8);
                        post.setEntity(entity);
                        status = client.execute(post);
                        StatusResult execStatus = processResponse(getStatusUrl, status, StatusResult.class);
                        Map bizresult = execStatus.getBizresult();
                        Map s = (Map) bizresult.get("status");
                        execState = ExecResult.parse((Integer) s.get("state"));
                        if (execState == ExecResult.SUCCESS || execState == ExecResult.FAILD) {
                            break;
                        }
                        Thread.sleep(3000);
                    } finally {
                        status.close();
                    }
                }
            } finally {
                if (webSocket != null) {
                    Thread.sleep(4000);
                    try {
                        webSocket.close();
                    } catch (Throwable e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }

            long costTime = System.currentTimeMillis() - startTime;
            log.info("PIGEON task: {},taskId:{} costTime : {} milliseconds, statusCode : {}",
                    targetJobName, taskId, costTime, (execState == ExecResult.SUCCESS) ? "'success'" : "'failure'");
            setExitStatusCode((execState == ExecResult.SUCCESS) ? TaskConstants.EXIT_CODE_SUCCESS
                    : TaskConstants.EXIT_CODE_FAILURE);
        } catch (Exception e) {
            log.error("execute PIGEON dataX faild,PIGEON task name:" + targetJobName, e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new TaskException("Execute pigeon task failed", e);
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    private void addFormUrlencoded(HttpPost post) {
        post.addHeader("content-type", "application/x-www-form-urlencoded");
    }

    @Override
    public void cancelApplication() throws TaskException {
        log.info("start to cancelApplication");
        Objects.requireNonNull(triggerResult, "triggerResult can not be null");
        log.info("start to cancelApplication taskId:{}", triggerResult.getTaskId());
        final String triggerUrl = getTriggerUrl();

        StringEntity entity =
                new StringEntity(config.getJobCancelPostBody(triggerResult.getTaskId()), StandardCharsets.UTF_8);

        CancelResult cancelResult = null;
        HttpPost post = new HttpPost(triggerUrl);
        addFormUrlencoded(post);
        post.setEntity(entity);
        try (
                CloseableHttpClient client = HttpClients.createDefault();
                // trigger to start TIS dataX task
                CloseableHttpResponse response = client.execute(post)) {
            cancelResult = processResponse(triggerUrl, response, CancelResult.class);
            if (!cancelResult.isSuccess()) {
                List<String> errormsg = triggerResult.getErrormsg();
                StringBuffer errs = new StringBuffer();
                if (CollectionUtils.isNotEmpty(errormsg)) {
                    errs.append(",errs:").append(errormsg.stream().collect(Collectors.joining(",")));
                }
                throw new TaskException("cancel PIGEON job faild taskId:" + triggerResult.getTaskId() + errs);
            }
        } catch (ClientProtocolException e) {
            throw new TaskException("client protocol error", e);
        } catch (Exception e) {
            throw new TaskException("pigeon execute error", e);
        }
    }

    private String getTriggerUrl() {
        final String tisHost = getHost();
        return config.getJobTriggerUrl(tisHost);
    }

    private String getHost() {
        final String host = taskExecutionContext.getDefinedParams().get(KEY_POOL_VAR_PIGEON_HOST);
        if (StringUtils.isEmpty(host)) {
            throw new IllegalStateException("global var '" + KEY_POOL_VAR_PIGEON_HOST + "' can not be empty");
        }
        return host;
    }

    private WebSocketClient receiveRealtimeLog(final String tisHost, String dataXName, int taskId) throws Exception {
        final String applyURI = config.getJobLogsFetchUrl(tisHost, dataXName, taskId);
        log.info("apply ws connection,uri:{}", applyURI);
        WebSocketClient webSocketClient = new WebSocketClient(new URI(applyURI)) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("start to receive remote execute log");
            }

            @Override
            public void onMessage(String message) {
                ExecLog execLog = JSONUtils.parseObject(message, ExecLog.class);
                log.info(execLog.getMsg());
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("stop to receive remote log,reason:{},taskId:{}", reason, taskId);
            }

            @Override
            public void onError(Exception t) {
                log.error(t.getMessage(), t);
            }
        };
        webSocketClient.connect();
        return webSocketClient;
    }

    private <T extends AjaxResult> T processResponse(String applyUrl, CloseableHttpResponse response,
                                                     Class<T> clazz) throws Exception {
        StatusLine resStatus = response.getStatusLine();
        if (HttpURLConnection.HTTP_OK != resStatus.getStatusCode()) {
            throw new IllegalStateException("request server " + applyUrl + " faild:" + resStatus.getReasonPhrase());
        }
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        T result = JSONUtils.parseObject(resp, clazz);
        return result;
    }

    @Override
    public AbstractParameters getParameters() {
        Objects.requireNonNull(this.parameters, "tisParameters can not be null");
        return this.parameters;
    }

    private static class CancelResult extends AjaxResult<Object> {

        private Object bizresult;

        @Override
        public Object getBizresult() {
            return this.bizresult;
        }

        public void setBizresult(Object bizresult) {
            this.bizresult = bizresult;
        }
    }

    private static class BizResult extends AjaxResult<TriggerBuildResult> {

        private TriggerBuildResult bizresult;

        @Override
        public TriggerBuildResult getBizresult() {
            return this.bizresult;
        }

        public int getTaskId() {
            return bizresult.taskid;
        }

        public void setBizresult(TriggerBuildResult bizresult) {
            this.bizresult = bizresult;
        }
    }

    private static class StatusResult extends AjaxResult<Map> {

        private Map bizresult;

        @Override
        public Map getBizresult() {
            return this.bizresult;
        }

        public void setBizresult(Map bizresult) {
            this.bizresult = bizresult;
        }
    }

    private abstract static class AjaxResult<T> {

        private boolean success;

        private List<String> errormsg;

        private List<String> msg;

        public abstract T getBizresult();

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<String> getErrormsg() {
            return this.errormsg;
        }

        public void setErrormsg(List<String> errormsg) {
            this.errormsg = errormsg;
        }

        public List<String> getMsg() {
            return this.msg;
        }

        public void setMsg(List<String> msg) {
            this.msg = msg;
        }

    }

    private static class TriggerBuildResult {

        private int taskid;

        public int getTaskid() {
            return taskid;
        }

        public void setTaskid(int taskid) {
            this.taskid = taskid;
        }
    }

    private enum ExecResult {

        SUCCESS(1), FAILD(-1), DOING(2), ASYN_DOING(22), CANCEL(3);

        private final int value;

        public static ExecResult parse(int value) {
            for (ExecResult r : values()) {
                if (r.value == value) {
                    return r;
                }
            }
            throw new IllegalStateException("vale:" + value + " is illegal");
        }

        private ExecResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private static class ExecLog {

        private String logType;
        private String msg;
        private int taskId;

        public String getLogType() {
            return logType;
        }

        public void setLogType(String logType) {
            this.logType = logType;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getTaskId() {
            return taskId;
        }

        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }
    }
}
