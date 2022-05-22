# Hardware Environment

DolphinScheduler, as an open-source distributed workflow task scheduling system, can deploy and run smoothly in Intel architecture server environments and mainstream virtualization environments and supports mainstream Linux operating system environments.

## Linux Operating System Version Requirements

| OS       | Version         |
| :----------------------- | :----------: |
| Red Hat Enterprise Linux | 7.0 and above   |
| CentOS                   | 7.0 and above   |
| Oracle Enterprise Linux  | 7.0 and above   |
| Ubuntu LTS               | 16.04 and above |

> **Attention:**
>The above Linux operating systems can run on physical servers and mainstream virtualization environments such as VMware, KVM, and XEN.

## Recommended Server Configuration

DolphinScheduler supports 64-bit hardware platforms with Intel x86-64 architecture. The following shows the recommended server requirements in a production environment:

### Production Environment

| **CPU** | **MEM** | **HD** | **NIC** | **Num** |
| --- | --- | --- | --- | --- |
| 4 core+ | 8 GB+ | SAS | GbE | 1+ |

> **Attention:**
> - The above recommended configuration is the minimum configuration for deploying DolphinScheduler. Higher configuration is strongly recommended for production environments.
> - The recommended hard disk size is more than 50GB and separate the system disk and data disk.


## Network Requirements

DolphinScheduler provides the following network port configurations for normal operation:

| Server | Port | Desc |
|  --- | --- | --- |
| MasterServer |  5678  | not the communication port, require the native ports do not conflict |
| WorkerServer | 1234  | not the communication port, require the native ports do not conflict |
| ApiApplicationServer |  12345 | backend communication port |

> **Attention:**
> - MasterServer and WorkerServer do not need to enable communication between the networks. As long as the local ports do not conflict.
> - Administrators can adjust relevant ports on the network side and host-side according to the deployment plan of DolphinScheduler components in the actual environment.

## Browser Requirements

DolphinScheduler recommends Chrome and the latest browsers which use Chrome Kernel to access the front-end UI page.