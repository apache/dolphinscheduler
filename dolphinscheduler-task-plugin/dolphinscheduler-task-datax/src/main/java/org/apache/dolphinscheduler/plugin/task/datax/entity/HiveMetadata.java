package org.apache.dolphinscheduler.plugin.task.datax.entity;

import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Arrays;

public class HiveMetadata {
    private String fieldDelimiter;
    private String location;
    private String defaultFS;
    private String path;
    private String fileType;

    public HiveMetadata() {
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        String[] temp = location.split("/");
        this.defaultFS = String.join("/", Arrays.copyOfRange(temp, 0, 3));
        this.path = "/" + String.join("/", Arrays.copyOfRange(temp, 3, temp.length));
    }

    public String getDefaultFS() {
        return defaultFS;
    }

    public String getPath() {
        return path;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean checkParameters() {
        return StringUtils.isNotEmpty(fieldDelimiter)
                && StringUtils.isNotEmpty(location)
                && StringUtils.isNotEmpty(defaultFS)
                && StringUtils.isNotEmpty(path)
                && StringUtils.isNotEmpty(fileType);
    }

    @Override
    public String toString() {
        return "HiveMetadata{" +
                "fieldDelimiter='" + fieldDelimiter + '\'' +
                ", location='" + location + '\'' +
                ", defaultFS='" + defaultFS + '\'' +
                ", path='" + path + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
