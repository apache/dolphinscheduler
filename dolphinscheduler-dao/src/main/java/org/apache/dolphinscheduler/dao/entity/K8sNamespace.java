package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * k8s_namespace
 */
@TableName("t_ds_k8s_namespace")
public class K8sNamespace {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * namespace name
   */
  @TableField(value = "namespace")
  private String namespace;
  /**
   * total cpu limit
   */
  @TableField(value = "limits_cpu")
  private Double limitsCpu;
  /**
   * total memory limit,mi
   */
  private Integer limitsMemory;
  /**
   * owner
   */
  @TableField(value = "owner")
  private String owner;

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
  /**
   * tag use for set this namespace allow run which type
   */
  @TableField("tag")
  private String tag;

  @TableField("pod_request_cpu")
  private Double podRequestCpu = 0.0;
  /**
   * Mi
   */
  @TableField("pod_request_memory")
  private Integer podRequestMemory = 0;
  /**
   *
   */
  @TableField("pod_replicas")
  private Integer podReplicas =0;
  /**
   * online job
   */
  @TableField("online_job_num")
  private Integer onlineJobNum =0;
  /**
   * k8s name
   */
  @TableField("k8s")
  private String k8s;

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public Double getLimitsCpu() {
    return limitsCpu;
  }

  public void setLimitsCpu(Double limitsCpu) {
    this.limitsCpu = limitsCpu;
  }

  public Integer getLimitsMemory() {
    return limitsMemory;
  }

  public void setLimitsMemory(Integer limitsMemory) {
    this.limitsMemory = limitsMemory;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
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

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Integer getPodRequestMemory() {
    return podRequestMemory;
  }

  public void setPodRequestMemory(Integer podRequestMemory) {
    this.podRequestMemory = podRequestMemory;
  }

  public Integer getPodReplicas() {
    return podReplicas;
  }

  public void setPodReplicas(Integer podReplicas) {
    this.podReplicas = podReplicas;
  }

  public Integer getOnlineJobNum() {
    return onlineJobNum;
  }

  public void setOnlineJobNum(Integer onlineJobNum) {
    this.onlineJobNum = onlineJobNum;
  }

  public String getK8s() {
    return k8s;
  }

  public void setK8s(String k8s) {
    this.k8s = k8s;
  }

  public Double getPodRequestCpu() {
    return podRequestCpu;
  }

  public void setPodRequestCpu(Double podRequestCpu) {
    this.podRequestCpu = podRequestCpu;
  }
}
