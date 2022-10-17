# Hardware Environment

This section briefs about the hardware requirements for DolphinScheduler. DolphinScheduler works as an open-source distributed workflow task scheduling system. It can deploy and run smoothly in Intel architecture server environments and mainstream virtualization environments. It also supports mainstream Linux operating system environments and ARM architecture.

## Linux Operating System Version Requirements

The Linux operating systems specified below can run on physical servers and mainstream virtualization environments such as VMware, KVM, and XEN.

| Operating System         |     Version     |
|:-------------------------|:---------------:|
| Red Hat Enterprise Linux |  7.0 and above  |
| CentOS                   |  7.0 and above  |
| Oracle Enterprise Linux  |  7.0 and above  |
| Ubuntu LTS               | 16.04 and above |

> **Note:**
> The above Linux operating systems can run on physical servers and mainstream virtualization environments such as VMware, KVM, and XEN.

## Server Configuration

DolphinScheduler supports 64-bit hardware platforms with Intel x86-64 architecture. The following table shows the recommended server requirements in a production environment:

### Production Environment

| **CPU** | **MEM** | **HD** | **NIC** | **Num** |
|---------|---------|--------|---------|---------|
| 4 core+ | 8 GB+   | SAS    | GbE     | 1+      |

> **Note:**
> - The above recommended configuration is the minimum configuration for deploying DolphinScheduler. Higher configuration is strongly recommended for production environments.
> - The recommended hard disk size is more than 50GB and separate the system disk and data disk.

## Network Requirements

DolphinScheduler provides the following network port configurations for normal operation:

|        Server        | Port  |                                 Desc                                 |
|----------------------|-------|----------------------------------------------------------------------|
| MasterServer         | 5678  | not the communication port, require the native ports do not conflict |
| WorkerServer         | 1234  | not the communication port, require the native ports do not conflict |
| ApiApplicationServer | 12345 | backend communication port                                           |

> **Note:**
> - MasterServer and WorkerServer do not need to enable communication between the networks. As long as the local ports do not conflict.
> - Administrators can adjust relevant ports on the network side and host-side according to the deployment plan of DolphinScheduler components in the actual environment.

## Browser Requirements

The minimum supported version of Google Chrome is version 85, but version 90 or above is recommended.

## Synchronize clocks
To avoid problems with internal cluster communications that can impact your task execution, make sure that the clocks on all of the cluster nodes are synchronized from a common clock source, such as using Chrony and/or NTP. Synchronizing the time ensures that every node in the cluster has the same time.