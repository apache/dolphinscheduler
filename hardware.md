This section briefs about the hardware requirements for DolphinScheduler. DolphinScheduler works as an open-source distributed workflow task scheduling system. It can deploy and run smoothly in Intel architecture server environments and mainstream virtualization environments. It also supports mainstream Linux operating system environments and ARM architecture.

Linux Operating System Version Requirements 
--------------------------------------------

The Linux operating systems specified below can run on physical servers and mainstream virtualization environments such as VMware, KVM, and XEN.

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Operating System</th><th class="confluenceTh">Version</th></tr><tr><td class="confluenceTd">Red Hat Enterprise Linux</td><td class="confluenceTd">7.0 and above</td></tr><tr><td class="confluenceTd">CentOS</td><td class="confluenceTd">7.0 and above</td></tr><tr><td class="confluenceTd">Oracle Enterprise Linux</td><td class="confluenceTd">7.0 and above</td></tr><tr><td colspan="1" class="confluenceTd">Ubuntu LTS</td><td colspan="1" class="confluenceTd">16.04 and above</td></tr></tbody></table>

Server Configuration
--------------------

DolphinScheduler supports 64-bit hardware platforms with Intel x86-64 architecture. The following table shows the recommended server requirements in a production environment:

### Production Environment

| **CPU** | **MEM** | **HD** | **NIC** | **Num** |
| --- | --- | --- | --- | --- |
| 4 core+ | 8 GB+ | SAS | GbE | 1+ |

> **Attention:**
> 
> *   The above recommended configuration is the minimum configuration for deploying DolphinScheduler. Higher configuration is strongly recommended for production environments.
> *   The recommended hard disk size is more than 50GB and separate the system disk and data disk.

Network Requirements
--------------------

DolphinScheduler provides the following network port configurations for normal operation:

| Server | Port | Description |
| --- | --- | --- |
| MasterServer | 5678 | Not the communication port. It is required that the native ports do not conflict. |
| WorkerServer | 1234 | Not the communication port. It is required that the native ports do not conflict. |
| ApiApplicationServer | 12345 | Backend communication port. |

> **Attention:**
> 
> *   MasterServer and WorkerServer do not need to enable communication between the networks. As long as the local ports do not conflict.
> *   Administrators can adjust relevant ports on the network side and host-side according to the deployment plan of DolphinScheduler components in the actual environment.

Browser Requirement
-------------------

The minimum supported version of Google Chrome is version 85, but version 90 or above is recommended.