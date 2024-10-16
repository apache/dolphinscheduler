# Apache DolphinScheduler Security Model

This document is mainly used to describe the scope of work, responsibilities and key functions of users with different roles. By introducing user permissions and functions, it helps users understand the precautions and rules in the deployment, use, operation and maintenance stages. Developers can use this document to understand the boundaries between security vulnerabilities and normal functions.

## Apache DolphinScheduler Workflow

From understanding to using Apache DolphinScheduler, users generally go through the following stages.

1. System deployment, configure the operating environment

2. Create system users and configure corresponding resources

3. Create workflow definitions and configure task operations

4. System operation and maintenance

Whether the user uses a single node, pseudo cluster, or cluster deployment (server or cloud deployment), the use of the system will go through the above four steps. For the above four steps, the following three types of users are generally involved.

## User Type

### 1. Service deployment personnel

Service deployment personnel need to have the authority to operate the server. Service deployment personnel need to understand the way related tasks are run to ensure the server security boundaries and environmental requirements.
(1). For multi-tenant operation scenarios, server deployment users need to have the permissions to create and switch users.
(2). Apache DolphinScheduler can run user-defined scripts and codes. Users can execute any command or code on the machine through node configuration. Service deployment personnel need to confirm the permissions of the service startup user, protect some sensitive files through permissions, and clarify the boundaries of the deployment user's operation permissions.
(3). The server will perform data source connection operations and execute user-defined SQL statements. The platform will not limit the type of SQL executed by the user. The permission for SQL execution is related to the user permissions for creating the data source.
(4). Server deployment personnel need to ensure the network and interaction security requirements between all worker servers in the worker group required by the business and the resources required for task operation.
(5). For worker local task types (such as datax), permissions to call corresponding services are required.
(6). The resource center provided by Apache DolphinScheduler can be directly connected to the local file system. In a cluster deployment environment, other server files can be mounted to the API server through shared files to achieve file access. Here, service deployment personnel need to ensure that the files contained in the mounted file directory allow system users to operate and trust the operation behavior of the operating user.
(7).Apache DolphinScheduler supports k8s task types. The k8s cluster is provided by the operation and maintenance. The operation and maintenance needs to ensure the security of k8s services and prevent security issues such as pod escape.

### 2. System administrator

The system administrator has all the operation permissions of Apache DolphinScheduler. In actual use, the scope of use of the administrator user should be ensured, and the administrator user should be highly trusted not to abuse this function.
(1). The administrator user can operate queue management, tenant management, user management, alarm group management, worker group management, token management and other functions. The administrator user can operate all configurations, including important information such as sensitive credentials required to connect to resources. It is necessary to ensure that the person using the administrator user can operate the corresponding resources. At the same time, the administrator user can authorize operations on resources, data sources, projects, etc. in the user management module. The administrator user needs to clearly authorize the user to have all usage permissions for the corresponding resources.
(2). System administrators have all the operational permissions that ordinary users have

### 3. Ordinary system users

Ordinary users of Apache DolphinScheduler are defined as actual workflow development and operation users. Of course, they also need to maintain some resources required during the workflow development process. These users should be highly trusted not to abuse this function.

(1). Users can create workflows and tasks. For a list of supported task types, see [Task List]. Tasks will be executed in workers. Users can customize any command and code to run in a specified worker group. Be sure to pay attention to any command and code here. Users can execute all tasks under the task types supported by Apache DolphinScheduler, including shell, sql, and jump to other servers to execute shell scripts. At the same time, logs will be generated during the task running process. Users can view and download task running logs through the UI page.

(2). Users can create data source connections, modify and delete authorized connections including corresponding configurations, especially operations on sensitive credentials of authorized connections. These operations may have a certain impact on the resources themselves or the system. Data sources include many types. For details, please see [Data Source List] on the official website.

### 4. Unlogged in users

Apache DolphinScheduler does not allow unlogged in users to access the system. The users mentioned below do not include this type of users.

In addition to the core workflow development and operation, the normal use of the platform also requires the configuration and management of the corresponding environment and resources.

## Data source management

All users can operate data source management, and administrator users can operate after authorizing ordinary users. The corresponding permissions for data source task operation are provided by the data source connection, and the connection configuration should try to control the task execution permissions. In the data source configuration, users can customize the connection parameters and they will take effect on all tasks using the data source.

## Resource Center

The resource center can configure local, distributed file storage, cloud object storage and other methods. When the resource center needs to be used to create or upload related files, all files and resources will be stored in the distributed file system HDFS or remote object storage. At the same time, users can modify the content of authorized files. In this process, it is necessary to trust that users will not damage files and will not cause other security risks.

## Alarm Management

The list of supported alarm methods can be viewed in [Alarm] on the official website. All users can configure authorized alarm channels to their respective processes. Users can modify alarm configurations containing sensitive credentials. Alarm configurations will be applied to alarms of rules such as workflow timeouts and results. It is necessary to trust that the configuration of user alarms and the sending of alarm information will not affect the alarm channel and the person receiving the alarm.

## Authentication method

Apache DolphinScheduler supports four authentication methods: login with your own account and password, LDAP, SSO login through Casdoor, and login through Oauth2 authorization, and the oauth2 authorization login method can be used with other authentication methods. It is necessary to highly trust that users who log in in any way will not abuse the corresponding permissions and functions.

## Security Center

Administrator users can configure resources such as queues, tenants, users, alarm groups, worker groups, tokens, k8s clusters, k8s namespaces, etc. It is necessary to trust that the user's permission allocation, use, and maintenance of resources will not affect the platform and service itself.

### Examples of Mistaken Security Vulnerabilities

The following are some erroneous vulnerabilities raised by users and developers in the past.

1. Using the insecure settings of plug-ins to attack or perform other operations
   When a user uses a plug-in, some parameters are set to insecure configurations, and then the system is attacked through the configuration. This problem does not belong to a security vulnerability. This type of plug-in includes but is not limited to data sources, tasks, etc. The user's setting of parameters is an active behavior, and the authorization has trusted the user's parameter configuration operations. When setting the corresponding parameters or a certain configuration, the user believes that the configuration user has fully understood the configured functions and the risks brought about, so this type of problem does not belong to a vulnerability. For example, when using MySQL driver to connect to Doris, {"aaa":"dsf&allowLoadLocalInfile=true#"} is added to the JDBC connection parameters. This configuration may send local sensitive files to the server. In this process, the user adds configurations as needed, and all operations of the user are trusted.
2. Use the security configuration during deployment to access the system for attack or other operations
   When deploying the system, the deployment user should follow the operation of the official website to modify the sensitive configuration. The configuration belongs to the service sensitive information, and its importance and security level are equivalent to the service database connection and other information. When other users obtain sensitive configurations through any means, the platform considers the user to be a normal authorized user and fully trusts all operations of the authorized user. For example, the user obtains auth-token data from the configuration file, authenticates and creates a user through the configuration, and uses the created user to operate the system. In this process, since the user obtains the authentication information of the platform, all operations of the user are considered to be trusted.
3. Intermediate files generated by the execution platform during task execution
   In the process of running tasks in Apache DolphinScheduler, some intermediate files are generated. This file mainly encapsulates the environment and parameters required for the task to run. The file is related to the task and is stored in the same node as the running task. Running these files is no different from running the same task in the same node by other users. During the deployment and permission allocation process, the corresponding worker or other resources are allocated to the corresponding user. This operation means that the user is fully trusted for all operations on the service node, including task running and reading and modifying the resources with permissions in the server, and of course, the files generated by the platform. Therefore, this type of problem does not belong to a vulnerability. For example, the remote shell task will produce an intermediate file in the server. The user knows the file information and operates the file through the shell node. In this process, the user has the permission of the node, and all operations of the user on the node are trusted.
4. Authorized users enter scripts through the page input box to attack or other operations
   There are multiple input boxes in Apache DolphinScheduler, allowing users to customize configurations as needed. As an open source task scheduling system, Apache DolphinScheduler requires administrators to fully trust all authorized operations of the target user in the process of deployment, authorization, and other security-related processes. If the user's behavior of adding and modifying configurations through pages or calling interfaces is within the scope of permissions, then the behavior of attacking or other operations in this way does not belong to security vulnerabilities.
5. Attack or other operations by modifying the image or providing an unsafe image to run
   Apache DolphinScheduler itself and task operations both support k8s clusters. Before the service or task runs, the user needs to ensure the image's functions and configured parameters, and trust all operations during the service and task running process. Therefore, modifying tasks or parameters by any means before the image runs to attack or complete other operations does not constitute a security vulnerability.

