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

package org.apache.dolphinscheduler.plugin.task.tis;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;

/**
 * TIS DataX Task
 **/
public class TISTask extends AbstractTaskExecutor {

    public static final String WS_REQUEST_PATH = "/tjs/download/logfeedback";
    public static final String KEY_POOL_VAR_TIS_HOST = "tisHost";
    private final TaskRequest taskExecutionContext;

    private TISParameters tisParameters;

    public TISTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        super.init();
        logger.info("tis task params {}", taskExecutionContext.getTaskParams());
        tisParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), TISParameters.class);
        if (!tisParameters.checkParameters()) {
            throw new RuntimeException("datax task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        // Trigger TIS DataX pipeline
        logger.info("start execute TIS task");
        long startTime = System.currentTimeMillis();
        String targetJobName = this.tisParameters.getTargetJobName();
        final String tisHost = taskExecutionContext.getDefinedParams().get(KEY_POOL_VAR_TIS_HOST);
        if (StringUtils.isEmpty(tisHost)) {
            throw new IllegalStateException("global var '" + KEY_POOL_VAR_TIS_HOST + "' can not be empty");
        }
        try {
            final String triggerUrl = String.format("http://%s/tjs/coredefine/coredefine.ajax", tisHost);
            final String getStatusUrl = String.format("http://%s/tjs/config/config.ajax?action=collection_action&emethod=get_task_status", tisHost);
            HttpPost post = new HttpPost(triggerUrl);
            post.addHeader("appname", targetJobName);
            addFormUrlencoded(post);
            StringEntity entity = new StringEntity("action=datax_action&emethod=trigger_fullbuild_task", StandardCharsets.UTF_8);
            post.setEntity(entity);
            BizResult ajaxResult = null;
            ExecResult execState = null;
            int taskId;
            WebSocket webSocket = null;
            try (CloseableHttpClient client = HttpClients.createDefault();
                 // trigger to start TIS dataX task
                 CloseableHttpResponse response = client.execute(post)) {
                ajaxResult = processResponse(triggerUrl, response, BizResult.class);
                if (!ajaxResult.isSuccess()) {
                    List<String> errormsg = ajaxResult.getErrormsg();
                    StringBuffer errs = new StringBuffer();
                    if (CollectionUtils.isNotEmpty(errormsg)) {
                        errs.append(",errs:").append(errormsg.stream().collect(Collectors.joining(",")));
                    }
                    throw new Exception("trigger TIS job faild taskName:" + targetJobName + errs.toString());
                }
                taskId = ajaxResult.getBizresult().getTaskid();

                webSocket = receiveRealtimeLog(tisHost, targetJobName, taskId);

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
                try {
                    webSocket.sendCloseFrame();
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            long costTime = System.currentTimeMillis() - startTime;
            logger.info("TIS task: {},taskId:{} costTime : {} milliseconds, statusCode : {}",
                    targetJobName, taskId, costTime, (execState == ExecResult.SUCCESS) ? "'success'" : "'failure'");
            setExitStatusCode((execState == ExecResult.SUCCESS) ? TaskConstants.EXIT_CODE_SUCCESS : TaskConstants.EXIT_CODE_FAILURE);
        } catch (Exception e) {
            logger.error("execute TIS dataX faild,TIS task name:" + targetJobName, e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        }
    }

    private void addFormUrlencoded(HttpPost post) {
        post.addHeader("content-type", "application/x-www-form-urlencoded");
    }

    @Override
    public void cancelApplication(boolean status) throws Exception {
        super.cancelApplication(status);
    }

    private WebSocket receiveRealtimeLog(final String tisHost, String dataXName, int taskId) throws InterruptedException, java.util.concurrent.ExecutionException {

        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
                = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
                .addWebSocketListener(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket websocket) {
                        // WebSocket connection opened
                    }

                    @Override
                    public void onClose(WebSocket websocket, int code, String reason) {
                        // WebSocket connection closed
                    }

                    @Override
                    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                        ExecLog execLog = JSONUtils.parseObject(payload, ExecLog.class);
                        logger.info(execLog.getMsg());
                    }

                    @Override
                    public void onError(Throwable t) {
                        // WebSocket connection error
                        logger.error(t.getMessage(), t);
                    }
                }).build();
        WebSocket webSocketClient = Dsl.asyncHttpClient()
                .prepareGet(String.format("ws://%s" + WS_REQUEST_PATH, tisHost))
                // .addHeader("header_name", "header_value")
                .addQueryParam("logtype", "full")
                .addQueryParam("collection", dataXName)
                .addQueryParam("taskid", String.valueOf(taskId))
                .setRequestTimeout(5000)
                .execute(wsHandler)
                .get();

        return webSocketClient;
    }

    private <T extends AjaxResult> T processResponse(String applyUrl, CloseableHttpResponse response, Class<T> clazz) throws Exception {
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
        Objects.requireNonNull(this.tisParameters, "tisParameters can not be null");
        return this.tisParameters;
    }

    private static class BizResult extends AjaxResult<TriggerBuildResult> {
        private TriggerBuildResult bizresult;

        @Override
        public TriggerBuildResult getBizresult() {
            return this.bizresult;
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
