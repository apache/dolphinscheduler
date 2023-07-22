package org.apache.dolphinscheduler.plugin.alert.voice;

public class VoidceParam {

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getTtsCode() {
            return ttsCode;
        }

        public void setTtsCode(String ttsCode) {
            this.ttsCode = ttsCode;
        }
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public void setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
    }

    public String getCalledShowNumber() {
        return calledShowNumber;
    }

    public void setCalledShowNumber(String calledShowNumber) {
        this.calledShowNumber = calledShowNumber;
    }

    public String getTtsCode() {
        return ttsCode;
    }

    public void setTtsCode(String ttsCode) {
        this.ttsCode = ttsCode;
    }

    public String getTtsParam() {
        return ttsParam;
    }

    public void setTtsParam(String ttsParam) {
        this.ttsParam = ttsParam;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }
}
