package org.apache.dolphinscheduler.plugin.alert.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;

final class FeiShuSendMsgResponse {

    @JsonProperty("Extra")
    private String extra;
    @JsonProperty("StatusCode")
    private Integer statusCode;
    @JsonProperty("StatusMessage")
    private String statusMessage;

    public FeiShuSendMsgResponse() {
    }

    public String getExtra() {
        return this.extra;
    }

    @JsonProperty("Extra")
    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    @JsonProperty("StatusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    @JsonProperty("StatusMessage")
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FeiShuSendMsgResponse)) {
            return false;
        }
        final FeiShuSendMsgResponse other = (FeiShuSendMsgResponse) o;
        final Object this$extra = this.getExtra();
        final Object other$extra = other.getExtra();
        if (this$extra == null ? other$extra != null : !this$extra.equals(other$extra)) {
            return false;
        }
        final Object this$statusCode = this.getStatusCode();
        final Object other$statusCode = other.getStatusCode();
        if (this$statusCode == null ? other$statusCode != null : !this$statusCode.equals(other$statusCode)) {
            return false;
        }
        final Object this$statusMessage = this.getStatusMessage();
        final Object other$statusMessage = other.getStatusMessage();
        if (this$statusMessage == null ? other$statusMessage != null
                : !this$statusMessage.equals(other$statusMessage)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $extra = this.getExtra();
        result = result * PRIME + ($extra == null ? 43 : $extra.hashCode());
        final Object $statusCode = this.getStatusCode();
        result = result * PRIME + ($statusCode == null ? 43 : $statusCode.hashCode());
        final Object $statusMessage = this.getStatusMessage();
        result = result * PRIME + ($statusMessage == null ? 43 : $statusMessage.hashCode());
        return result;
    }

    public String toString() {
        return "FeiShuSender.FeiShuSendMsgResponse(extra=" + this.getExtra() + ", statusCode="
                + this.getStatusCode() + ", statusMessage=" + this.getStatusMessage() + ")";
    }
}
