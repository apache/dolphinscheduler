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

package org.apache.dolphinscheduler.plugin.alert.wechat;

import org.apache.dolphinscheduler.alert.api.ShowType;

import java.util.Map;

public class WechatAppMessage {

    private String touser;
    private String msgtype;
    private Integer agentid;
    private Map<String, String> text;
    private Map<String, String> markdown;
    private Integer safe;
    private Integer enable_id_trans;
    private Integer enable_duplicate_check;

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public Integer getAgentid() {
        return agentid;
    }

    public void setAgentid(Integer agentid) {
        this.agentid = agentid;
    }

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Integer getSafe() {
        return safe;
    }

    public void setSafe(Integer safe) {
        this.safe = safe;
    }

    public Integer getEnable_id_trans() {
        return enable_id_trans;
    }

    public void setEnable_id_trans(Integer enable_id_trans) {
        this.enable_id_trans = enable_id_trans;
    }

    public Integer getEnable_duplicate_check() {
        return enable_duplicate_check;
    }

    public void setEnable_duplicate_check(Integer enable_duplicate_check) {
        this.enable_duplicate_check = enable_duplicate_check;
    }

    public Map<String, String> getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Map<String, String> markdown) {
        this.markdown = markdown;
    }

    public WechatAppMessage() {
    }

    public WechatAppMessage(String touser, String msgtype, Integer agentid, Map<String, String> contentMap,
                            Integer safe, Integer enableIdTrans, Integer enableDuplicateCheck) {
        this.touser = touser;
        this.msgtype = msgtype;
        this.agentid = agentid;
        if (msgtype.equals(ShowType.MARKDOWN.getDescp())) {
            this.markdown = contentMap;
        } else {
            this.text = contentMap;
        }
        this.safe = safe;
        this.enable_id_trans = enableIdTrans;
        this.enable_duplicate_check = enableDuplicateCheck;
    }
}
