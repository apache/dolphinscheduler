package org.apache.dolphinscheduler.common.task.sqoop.targets;

public class TargetHiveParameter {

    private String hiveDatabase;
    private String hiveTable;
    private boolean createHiveTable;
    private boolean dropDelimiter;
    private boolean hiveOverWrite;
    private String replaceDelimiter;
    private String hivePartitionKey;
    private String hivePartitionValue;

    public String getHiveDatabase() {
        return hiveDatabase;
    }

    public void setHiveDatabase(String hiveDatabase) {
        this.hiveDatabase = hiveDatabase;
    }

    public String getHiveTable() {
        return hiveTable;
    }

    public void setHiveTable(String hiveTable) {
        this.hiveTable = hiveTable;
    }

    public boolean isCreateHiveTable() {
        return createHiveTable;
    }

    public void setCreateHiveTable(boolean createHiveTable) {
        this.createHiveTable = createHiveTable;
    }

    public boolean isDropDelimiter() {
        return dropDelimiter;
    }

    public void setDropDelimiter(boolean dropDelimiter) {
        this.dropDelimiter = dropDelimiter;
    }

    public boolean isHiveOverWrite() {
        return hiveOverWrite;
    }

    public void setHiveOverWrite(boolean hiveOverWrite) {
        this.hiveOverWrite = hiveOverWrite;
    }

    public String getReplaceDelimiter() {
        return replaceDelimiter;
    }

    public void setReplaceDelimiter(String replaceDelimiter) {
        this.replaceDelimiter = replaceDelimiter;
    }

    public String getHivePartitionKey() {
        return hivePartitionKey;
    }

    public void setHivePartitionKey(String hivePartitionKey) {
        this.hivePartitionKey = hivePartitionKey;
    }

    public String getHivePartitionValue() {
        return hivePartitionValue;
    }

    public void setHivePartitionValue(String hivePartitionValue) {
        this.hivePartitionValue = hivePartitionValue;
    }
}
