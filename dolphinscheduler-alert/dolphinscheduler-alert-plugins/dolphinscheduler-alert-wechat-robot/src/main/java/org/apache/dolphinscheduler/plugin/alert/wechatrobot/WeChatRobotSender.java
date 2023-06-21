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

package org.apache.dolphinscheduler.plugin.alert.wechatrobot;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class WeChatRobotSender {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WeChatRobotSender.class);
    private static final String ALERT_STATUS = "false";

    private final String robot;
    private final String uploadRobot;
    private final String markdownKeyTmp =
            "> {alterKeyName}: <font color=\\\"{valueWarnType}\\\">{alterKeyValue}</font> \n";
    private static final Properties mappingProperties = new Properties();

    private final Map<String, String> warnMapping = new HashMap<>();

    WeChatRobotSender(Map<String, String> config) {
        String robotUrl = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_ROBOT_URL);
        String robotKey = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_ROBOT_KEY);
        robot = robotUrl + "?key=" + robotKey;
        uploadRobot = robotUrl.replaceAll("send", "upload_media") + "?key=" + robotKey + "&type=file";
        String path =
                Objects.requireNonNull(this.getClass().getResource("/conf/")).getPath() + "robotMappingProperties.yml";
        try {
            mappingProperties.load(new InputStreamReader(
                    Objects.requireNonNull(this.getClass().getResourceAsStream("/conf/robotMappingProperties.yml")),
                    StandardCharsets.UTF_8));
            mappingProperties.keySet()
                    .forEach(key -> log.info("properties key ${} , value ${}", key, mappingProperties.get(key)));
        } catch (Exception e) {
            log.error("can not load robotMappingProperties.yml path ${} ", path);
        }
        warnMapping.put("SUCCESS", "info");
        warnMapping.put("FAILURE", "red");
        warnMapping.put("COMMON", "comment");
    }

    private String post(String url, String data) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, WeChatAlertConstants.CHARSET));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("Enterprise WeChat send [{}], param:{}, resp:{}",
                    url, data, resp);
            return resp;
        }
    }

    private String uploadPost(String url, File file) throws IOException {
        // 企业微信机器人上传文件
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            String boundary = "--------------4585696313564699";
            httpPost.setHeader("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpPost.addHeader("Connection", "keep-alive");
            httpPost.addHeader("Content-Encoding", "gzip");
            builder.setCharset(StandardCharsets.UTF_8);
            builder.setBoundary(boundary);
            builder.addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA, file.getName());
            builder.addTextBody("filename", file.getName(), ContentType.create("text/plain", StandardCharsets.UTF_8));
            builder.addTextBody("filelength", file.length() + "",
                    ContentType.create("text/plain", StandardCharsets.UTF_8));

            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity responseEntity = response.getEntity();
                resp = EntityUtils.toString(responseEntity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            return resp;
        }
    }

    /**
     * 将alertData 转成对应的格式
     * 根据任务类型进行转换 目前支持类型：
     * 1. spark
     * 2. shell
     *
     */
    private String markdownRobot(String title, String content) {
        List<LinkedHashMap<String, Object>> mapItemsList =
                JSONUtils.parseObject(content, new TypeReference<List<LinkedHashMap<String, Object>>>() {
                });
        if (null == mapItemsList || mapItemsList.isEmpty()) {
            log.error("itemsList is null");
            throw new RuntimeException("itemsList is null");
        }
        StringBuilder msg = new StringBuilder(200);
        StringBuilder contents = new StringBuilder();
        List<String> titleWarnList = new ArrayList<>();
        for (LinkedHashMap<String, Object> map : mapItemsList) {
            List<String> collectContent = map.keySet().stream().map(key -> {
                String alterKeyName = Optional.ofNullable((String) mappingProperties.get(key))
                        .orElse("").replace("\"", "");
                String alterKeyValue = map.get(key).toString();
                if (!alterKeyName.isEmpty()) {
                    String warnType = "COMMON";
                    if (WeChatAlertConstants.TASK_STATUS_KEY.equals(key) ||
                            WeChatAlertConstants.PROCESS_STATUS_KEY.equals(key)) {
                        warnType = ((String) map.get(key)).toUpperCase();
                    }
                    titleWarnList.add(warnType);
                    return markdownKeyTmp
                            .replace("{valueWarnType}",
                                    warnMapping.get(warnType) == null ? "red" : warnMapping.get(warnType))
                            .replace("{alterKeyName}", alterKeyName)
                            .replace("{alterKeyValue}", alterKeyValue);
                } else {
                    return "";
                }
            }).collect(Collectors.toList());
            StringBuilder perContent = new StringBuilder();
            for (String per : collectContent) {
                perContent.append(per);
            }
            perContent.append("\n");
            contents.append(perContent);
        }

        Map<String, Long> collect = titleWarnList.stream()
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String titleWarnType = collect.getOrDefault("FAILURE", 0L) > 0 ? "FAILURE"
                : (collect.getOrDefault("SUCCESS", 0L) > 0 ? "SUCCESS" : "COMMON");
        String markdownAllTitleTmp = "# <font color=\\\"{warnType}\\\"> {title} </font> \n";
        msg.append(markdownAllTitleTmp
                .replace("{warnType}", warnMapping.get(titleWarnType) == null ? "red" : warnMapping.get(titleWarnType))
                .replace("{title}", title))
                .append(contents);
        return msg.toString();
    }

    private AlertResult checkWeChatSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(ALERT_STATUS);

        if (null == result) {
            alertResult.setMessage("we chat send fail");
            log.info("send we chat msg error,resp is null");
            return alertResult;
        }
        WeChatRobotSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, WeChatRobotSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("we chat send fail");
            log.info("send we chat msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("we chat alert send SUCCESS");
            return alertResult;
        }
        alertResult.setStatus(ALERT_STATUS);
        alertResult.setMessage(sendMsgResponse.getErrmsg());
        return alertResult;
    }

    public AlertResult sendEnterpriseWeChat(String title, String content) {
        AlertResult alertResult;
        log.info("from master title {} , content {}", title, content);
        String msg;
        if (content.contains("taskState") || content.contains("taskName") || content.contains("projectName")) {
            // markdown
            String data = markdownRobot(title, content);
            String markdownStrTmp = "{\"msgtype\":\"markdown\",\"markdown\":{\"content\":\"{data}\"}}";
            msg = markdownStrTmp.replace("{data}", data);

        } else {
            // excel方式发送
            String mediaId = excelRobot(title, content);
            String markdownStrTmp = "{\"msgtype\":\"file\",\"file\":{\"media_id\":\"{data}\"}}";
            msg = markdownStrTmp.replace("{data}", Optional.ofNullable(mediaId).orElse(""));
        }
        log.info("msg ${}", msg);
        try {
            return checkWeChatSendMsgResult(post(robot, msg));
        } catch (Exception e) {
            log.info("send we chat alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setMessage("send we chat alert fail");
            alertResult.setStatus(ALERT_STATUS);
        }

        return alertResult;
    }

    private String excelRobot(String title, String content) {
        List<Map<String, Object>> mapItemsList =
                JSONUtils.parseObject(content, new TypeReference<List<Map<String, Object>>>() {
                });
        File file = new File("/tmp/" + title + ".xlsx");
        List<String> columns = new ArrayList<>(mapItemsList.get(0).keySet());
        ExportExcel.exportExcel(title, columns, mapItemsList, file);
        try {
            String s = uploadPost(uploadRobot, file);
            Map<String, Object> stringObjectMap = JSONUtils.parseObject(s, new TypeReference<Map<String, Object>>() {
            });
            if (Optional.ofNullable(stringObjectMap.get("errcode")).isPresent()
                    && Integer.parseInt(stringObjectMap.get("errcode").toString()) == 0) {
                return stringObjectMap.get("media_id").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            file.delete();
        }
        return null;
    }

    static final class WeChatRobotSendMsgResponse {

        private Integer errcode;
        private String errmsg;

        public WeChatRobotSendMsgResponse() {
        }

        public Integer getErrcode() {
            return this.errcode;
        }
        @SuppressWarnings("unused")
        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return this.errmsg;
        }

        @SuppressWarnings("unused")
        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof WeChatRobotSendMsgResponse)) {
                return false;
            }
            final WeChatRobotSendMsgResponse other = (WeChatRobotSendMsgResponse) o;
            final Object this$errcode = this.getErrcode();
            final Object other$errcode = other.getErrcode();
            if (!Objects.equals(this$errcode, other$errcode)) {
                return false;
            }
            final Object this$errmsg = this.getErrmsg();
            final Object other$errmsg = other.getErrmsg();
            return Objects.equals(this$errmsg, other$errmsg);
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $errcode = this.getErrcode();
            result = result * PRIME + ($errcode == null ? 43 : $errcode.hashCode());
            final Object $errmsg = this.getErrmsg();
            result = result * PRIME + ($errmsg == null ? 43 : $errmsg.hashCode());
            return result;
        }

        public String toString() {
            return "WeChatSender.WeChatSendMsgResponse(errcode=" + this.getErrcode() + ", errmsg=" + this.getErrmsg()
                    + ")";
        }
    }
}
