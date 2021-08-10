package org.apache.dolphinscheduler.common.task.kettle;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

import java.util.List;

/**
 * kettle parameter
 */
public class KettleParameters extends AbstractParameters {

    /**
     * file name
     */
    private String fileName;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public boolean checkParameters() {
        return fileName != null && !fileName.isEmpty();
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }

}
