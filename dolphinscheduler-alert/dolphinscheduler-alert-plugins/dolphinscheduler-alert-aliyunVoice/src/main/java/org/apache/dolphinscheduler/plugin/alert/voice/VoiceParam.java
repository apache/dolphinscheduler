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

package org.apache.dolphinscheduler.plugin.alert.voice;

import lombok.Data;

@Data
public class VoiceParam {

    /**
     * called Number
     */
    private String calledNumber;
    /**
     * called Show Number
     */
    private String calledShowNumber;
    /**
     * tts code
     */
    private String ttsCode;
    /**
     * tts param
     */
    private String ttsParam;

    /**
     * connection info
     */
    private Connection connection;

    /**
     * outId
     */
    private String outId;

    @Data
    public static class Connection {

        /**
         * address
         */
        private String address;

        /**
         * accessKeyId
         */
        private String accessKeyId;
        /**
         * accessKeySecret
         */
        private String accessKeySecret;

        /**
         * tts Code
         */
        private String ttsCode;
    }
}
