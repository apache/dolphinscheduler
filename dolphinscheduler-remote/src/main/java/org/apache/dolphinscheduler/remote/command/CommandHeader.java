package org.apache.dolphinscheduler.remote.command;

import java.io.Serializable;

/**
 * @Author: Tboy
 */
public class CommandHeader implements Serializable {

    private byte type;

    private long opaque;

    private int bodyLength;

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }
}
