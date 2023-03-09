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

public class WechatAppChatMessage {

    private String chatid;
    private String msgtype;
    private Map<String, String> text;
    private Map<String, String> markdown;
    private Integer safe;

    public String getChatid() {
        return chatid;
    }

    public void setChatid(String chatid) {
        this.chatid = chatid;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
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

    public Map<String, String> getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Map<String, String> markdown) {
        this.markdown = markdown;
    }

    public WechatAppChatMessage() {
    }

    public WechatAppChatMessage(String chatid, String msgtype, Map<String, String> contentMap, Integer safe) {
        this.chatid = chatid;
        this.msgtype = msgtype;
        if (msgtype.equals(ShowType.MARKDOWN.getDescp())) {
            this.markdown = contentMap;
        } else {
            this.text = contentMap;
        }
        this.safe = safe;
    }
}
