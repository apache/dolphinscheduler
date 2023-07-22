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
