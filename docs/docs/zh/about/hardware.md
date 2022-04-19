# 软硬件环境建议配置

DolphinScheduler 作为一款开源分布式工作流任务调度系统，可以很好地部署和运行在 Intel 架构服务器及主流虚拟化环境下，并支持主流的Linux操作系统环境

## 1. Linux 操作系统版本要求

| 操作系统       | 版本         |
| :----------------------- | :----------: |
| Red Hat Enterprise Linux | 7.0 及以上   |
| CentOS                   | 7.0 及以上   |
| Oracle Enterprise Linux  | 7.0 及以上   |
| Ubuntu LTS               | 16.04 及以上 |

> **注意：**
>以上 Linux 操作系统可运行在物理服务器以及 VMware、KVM、XEN 主流虚拟化环境上

## 2. 服务器建议配置
DolphinScheduler 支持运行在 Intel x86-64 架构的 64 位通用硬件服务器平台。对生产环境的服务器硬件配置有以下建议：
### 生产环境

| **CPU** | **内存** | **硬盘类型** | **网络** | **实例数量** |
| --- | --- | --- | --- | --- |
| 4核+ | 8 GB+ | SAS | 千兆网卡 | 1+ |

> **注意：**
> - 以上建议配置为部署 DolphinScheduler 的最低配置，生产环境强烈推荐使用更高的配置
> - 硬盘大小配置建议 50GB+ ，系统盘和数据盘分开


## 3. 网络要求

DolphinScheduler正常运行提供如下的网络端口配置：

| 组件 | 默认端口 | 说明 |
|  --- | --- | --- |
| MasterServer |  5678  | 非通信端口，只需本机端口不冲突即可 |
| WorkerServer | 1234  | 非通信端口，只需本机端口不冲突即可 |
| ApiApplicationServer |  12345 | 提供后端通信端口 |


> **注意：**
> - MasterServer 和 WorkerServer 不需要开启网络间通信，只需本机端口不冲突即可
> - 管理员可根据实际环境中 DolphinScheduler 组件部署方案，在网络侧和主机侧开放相关端口

## 4. 客户端 Web 浏览器要求

DolphinScheduler 推荐 Chrome 以及使用 Chromium 内核的较新版本浏览器访问前端可视化操作界面