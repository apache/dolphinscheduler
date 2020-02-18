package org.apache.dolphinscheduler.common.task.sqoop.targets;

public class TargetHdfsParameter {

    private String targetPath;
    private boolean deleteTargetDir;
    private String fileType;
    private String compressionCodec;

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public boolean isDeleteTargetDir() {
        return deleteTargetDir;
    }

    public void setDeleteTargetDir(boolean deleteTargetDir) {
        this.deleteTargetDir = deleteTargetDir;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCompressionCodec() {
        return compressionCodec;
    }

    public void setCompressionCodec(String compressionCodec) {
        this.compressionCodec = compressionCodec;
    }
}
