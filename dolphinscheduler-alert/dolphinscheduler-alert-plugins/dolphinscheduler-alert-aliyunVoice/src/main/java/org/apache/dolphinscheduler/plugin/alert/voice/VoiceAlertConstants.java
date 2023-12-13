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

public class VoiceAlertConstants {

    /**
     * called Number
     */
    static final String NAME_CALLED_NUMBER = "calledNumber";
    /**
     * called Number
     */
    static final String CALLED_NUMBER = "$t('calledNumber')";
    /**
     * called Show Number
     */
    static final String NAME_CALLED_SHOW_NUMBER = "calledShowNumber";
    /**
     * called Show Number
     */
    static final String CALLED_SHOW_NUMBER = "$t('calledShowNumber')";
    /**
     * tts Code
     */
    static final String NAME_TTS_CODE = "ttsCode";
    /**
     * tts Code
     */
    static final String TTS_CODE = "$t('ttsCode')";
    /**
     * tts Param
     */
    static final String TTS_PARAM = "ttsParam";

    /**
     * address
     */
    static final String NAME_ADDRESS = "address";
    /**
     * address
     */
    static final String ADDRESS = "$t('address')";
    /**
     * accessKeyId
     */
    static final String NAME_ACCESS_KEY_ID = "accessKeyId";
    /**
     * accessKeyId
     */
    static final String ACCESS_KEY_ID = "$t('accessKeyId')";
    /**
     * accessKeySecret
     */
    static final String NAME_ACCESS_KEY_SECRET = "accessKeySecret";
    /**
     * accessKeySecret
     */
    static final String ACCESS_KEY_SECRET = "$t('accessKeySecret')";

    private VoiceAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
