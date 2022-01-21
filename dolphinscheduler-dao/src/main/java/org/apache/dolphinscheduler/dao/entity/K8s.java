package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_ds_k8s")
public class K8s {
    /**
     * id
     */
    @TableId(value="id", type= IdType.AUTO)
    private int id;
    /**
     * queue name
     */
    @TableField(value = "k8s_name")
    private String k8sName;
    /**
     * yarn queue name
     */
    @TableField(value = "k8s_config")
    private String k8sConfig;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;
    /**
     * update_time
     */
    @TableField("update_time")
    private Date updateTime;


    public K8s(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getK8sName() {
        return k8sName;
    }

    public void setK8sName(String k8sName) {
        this.k8sName = k8sName;
    }

    public String getK8sConfig() {
        return k8sConfig;
    }

    public void setK8sConfig(String k8sConfig) {
        this.k8sConfig = k8sConfig;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
