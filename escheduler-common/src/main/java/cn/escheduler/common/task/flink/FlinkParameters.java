package cn.escheduler.common.task.flink;

import cn.escheduler.common.process.ResourceInfo;
import cn.escheduler.common.task.AbstractParameters;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class FlinkParameters extends AbstractParameters {

    /**
     * run model: yarn-cluster
     */
    private String deployMode;

    /**
     * main jar (only java and scala use it )
     */
    private ResourceInfo mainJar;

    /**
     * 项目类型： java / scala / python
     */
    private String programType;
    /**
     * main class
     */
    private String mainClass;
    /**
     * task manager number
     */
    private int yarncontainer;
    /**
     * yarn task name
     */
    private String yarnName;

    /**
     * yarn job manager memory setting
     */
    private Long yarnjobManagerMemory;
    /**
     * yarn task manager memory setting
     */
    private Long yarntaskManagerMemory;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;


    private String mainArgs;

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public ResourceInfo getMainJar() {
        return mainJar;
    }

    public void setMainJar(ResourceInfo mainJar) {
        this.mainJar = mainJar;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public int getYarncontainer() {
        return yarncontainer;
    }

    public void setYarncontainer(int yarncontainer) {
        this.yarncontainer = yarncontainer;
    }

    public String getYarnName() {
        return yarnName;
    }

    public void setYarnName(String yarnName) {
        this.yarnName = yarnName;
    }

    public Long getYarnjobManagerMemory() {
        return yarnjobManagerMemory;
    }

    public void setYarnjobManagerMemory(Long yarnjobManagerMemory) {
        this.yarnjobManagerMemory = yarnjobManagerMemory;
    }

    public Long getYarntaskManagerMemory() {
        return yarntaskManagerMemory;
    }

    public void setYarntaskManagerMemory(Long yarntaskManagerMemory) {
        this.yarntaskManagerMemory = yarntaskManagerMemory;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public void setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
    }

    @Override
    public boolean checkParameters() {
        if (!"PYTHON".equals(this.programType) && this.mainJar == null) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> getResourceFilesList() {
        if (resourceList != null) {
            this.resourceList.add(mainJar);
            return resourceList.stream()
                    .map(p -> p.getRes()).collect(Collectors.toList());
        }
        return null;
    }

}
