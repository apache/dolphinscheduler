package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Date;

// Could be put into org.apache.dolphinscheduler.common.model module?
// MayBe spi
public class StorageEntity {
    // builder. createStartTime(), createUpdateTime()
    private int id;
    private String fullName;
    private String alias;
    private String fileName;
    private boolean isDirectory;
    private String description;
    private int userId;
    private String userName;
    private ResourceType type;
    private long size;
    private Date createTime;
    private Date updateTime;
    // parent folder name
    private String pfullName;

    public static class Builder {
        private int id;
        private String alias;
        private String fileName;
        private boolean isDirectory;
        private String description;
        private int userId;
        private String userName;
        private ResourceType type;
        private long size;
        private Date createTime;
        private Date updateTime;
        private String fullName;
        private String pfullName;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder isDirectory(boolean isDir) {
            this.isDirectory = isDir;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder userId(int uid) {
            this.userId = uid;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder type(ResourceType type) {
            this.type = type;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder createTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(Date updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder pfullName(String pfullName) {
            this.pfullName = pfullName;
            return this;
        }


        public StorageEntity build(){
            return new StorageEntity(this);
        }
    }

    private StorageEntity(Builder builder) {
        this.id = builder.id;
        this.alias = builder.alias;
        this.fileName = builder.fileName;
        this.isDirectory = builder.isDirectory;
        this.description = builder.description;
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.type = builder.type;
        this.size = builder.size;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.fullName = builder.fullName;
        this.pfullName = builder.pfullName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setPfullName(String pfullName) {
        this.pfullName = pfullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getDescription() {
        return description;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public ResourceType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public String getPfullName() {
        return pfullName;
    }

    public String getFullName() {
        return fullName;
    }
}
