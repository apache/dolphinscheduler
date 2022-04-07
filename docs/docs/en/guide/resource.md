# Resource Center

If you want to use the resource upload function, you can appoint the local file directory as the upload directory for a single machine (this operation does not need to deploy Hadoop). Or you can also upload to a Hadoop or MinIO cluster, at this time, you need to have Hadoop (2.6+) or MinIO or other related environments.

> **_Note:_**
>
> * If you want to use the resource upload function, the deployment user in [installation and deployment](installation/standalone.md) must have relevant operation authority.
> * If you using a Hadoop cluster with HA, you need to enable HDFS resource upload, and you need to copy the `core-site.xml` and `hdfs-site.xml` under the Hadoop cluster to `/opt/dolphinscheduler/conf`, otherwise skip this copy step.

## HDFS Resource Configuration

- Upload resource files and UDF functions, all uploaded files and resources will be stored on HDFS, so require the following configurations:

```  
conf/common.properties  
    # Users who have permission to create directories under the HDFS root path
    hdfs.root.user=hdfs
    # data base dir, resource file will store to this hadoop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permissions。"/dolphinscheduler" is recommended
    resource.upload.path=/dolphinscheduler
    # resource storage type : HDFS,S3,NONE
    resource.storage.type=HDFS
    # whether kerberos starts
    hadoop.security.authentication.startup.state=false
    # java.security.krb5.conf path
    java.security.krb5.conf.path=/opt/krb5.conf
    # loginUserFromKeytab user
    login.user.keytab.username=hdfs-mycluster@ESZ.COM
    # loginUserFromKeytab path
    login.user.keytab.path=/opt/hdfs.headless.keytab    
    # if resource.storage.type is HDFS，and your Hadoop Cluster NameNode has HA enabled, you need to put core-site.xml and hdfs-site.xml in the installPath/conf directory. In this example, it is placed under /opt/soft/dolphinscheduler/conf, and configure the namenode cluster name; if the NameNode is not HA, modify it to a specific IP or host name.
    # if resource.storage.type is S3，write S3 address，HA，for example ：s3a://dolphinscheduler，
    # Note，s3 be sure to create the root directory /dolphinscheduler
    fs.defaultFS=hdfs://mycluster:8020    
    #resourcemanager ha note this need ips , this empty if single
    yarn.resourcemanager.ha.rm.ids=192.168.xx.xx,192.168.xx.xx    
    # If it is a single resourcemanager, you only need to configure one host name. If it is resourcemanager HA, the default configuration is fine
    yarn.application.status.address=http://xxxx:8088/ws/v1/cluster/apps/%s

```

## File Management

> It is the management of various resource files, including creating basic `txt/log/sh/conf/py/java` and jar packages and other type files, and can do edit, rename, download, delete and other operations to the files.

![file-manage](/img/new_ui/dev/resource/file-manage.png)

- Create a file
  > The file format supports the following types: txt, log, sh, conf, cfg, py, java, sql, xml, hql, properties.

![create-file](/img/new_ui/dev/resource/create-file.png)

- upload files

> Upload file: Click the "Upload File" button to upload, drag the file to the upload area, the file name will be automatically completed with the uploaded file name.

![upload-file](/img/new_ui/dev/resource/upload-file.png)

- File View

> For the files that can be viewed, click the file name to view the file details.

<p align="center">
   <img src="/img/file_detail_en.png" width="80%" />
 </p>

- Download file

> Click the "Download" button in the file list to download the file or click the "Download" button in the upper right corner of the file details to download the file.

- File rename

![rename-file](/img/new_ui/dev/resource/rename-file.png)

- delete
  > File list -> Click the "Delete" button to delete the specified file.

- Re-upload file

  > Re-upload file: Click the "Re-upload File" button to upload a new file to replace the old file, drag the file to the re-upload area, the file name will be automatically completed with the new uploaded file name.

    <p align="center">
      <img src="/img/reupload_file_en.png" width="80%" />
    </p>


## UDF Management

### Resource Management

> The resource management and file management functions are similar. The difference is that the resource management is the UDF upload function, and the file management uploads the user programs, scripts and configuration files.
> Operation function: rename, download, delete.

- Upload UDF resources
  > Same as uploading files.

### Function Management

- Create UDF function
  > Click "Create UDF Function", enter the UDF function parameters, select the UDF resource, and click "Submit" to create the UDF function.

> Currently, only supports temporary UDF functions of Hive.

- UDF function name: enter the name of the UDF function.
- Package name Class name: enter the full path of the UDF function.
- UDF resource: set the resource file corresponding to the created UDF function.

![create-udf](/img/new_ui/dev/resource/create-udf.png)
 
## Task Group Settings

The task group is mainly used to control the concurrency of task instances and is designed to control the pressure of other resources (it can also control the pressure of the Hadoop cluster, the cluster will have queue control it). When creating a new task definition, you can configure the corresponding task group and configure the priority of the task running in the task group. 

### Task Group Configuration 

#### Create Task Group 

![create-taskGroup](/img/new_ui/dev/resource/create-taskGroup.png)

The user clicks [Resources] - [Task Group Management] - [Task Group option] - [Create Task Group] 

![create-taskGroup](/img/new_ui/dev/resource/create-taskGroup.png) 

You need to enter the information inside the picture:

- Task group name: the name displayed of the task group

- Project name: the project range that the task group functions, this item is optional, if not selected, all the projects in the whole system can use this task group.

- Resource pool size: The maximum number of concurrent task instances allowed.

#### View Task Group Queue 

![view-queue](/img/new_ui/dev/resource/view-queue.png) 

Click the button to view task group usage information:

![view-queue](/img/new_ui/dev/resource/view-groupQueue.png) 

#### Use of Task Groups 

**Note**: The usage of task groups is applicable to tasks executed by workers, such as [switch] nodes, [condition] nodes, [sub_process] and other node types executed by the master are not controlled by the task group. Let's take the shell node as an example: 

![use-queue](/img/new_ui/dev/resource/use-queue.png)                 

Regarding the configuration of the task group, all you need to do is to configure these parts in the red box:

- Task group name: The task group name is displayed on the task group configuration page. Here you can only see the task group that the project has permission to access (the project is selected when creating a task group) or the task group that scope globally (no project is selected when creating a task group).

- Priority: When there is a waiting resource, the task with high priority will be distributed to the worker by the master first. The larger the value of this part, the higher the priority. 

### Implementation Logic of Task Group 

#### Get Task Group Resources: 

The master judges whether the task is configured with a task group when distributing the task. If the task is not configured, it is normally thrown to the worker to run; if a task group is configured, it checks whether the remaining size of the task group resource pool meets the current task operation before throwing it to the worker for execution. , if the resource pool -1 is satisfied, continue to run; if not, exit the task distribution and wait for other tasks to wake up. 

#### Release and Wake Up: 

When the task that has occupied the task group resource is finished, the task group resource will be released. After the release, it will check whether there is a task waiting in the current task group. If there is, mark the task with the best priority to run, and create a new executable event. The event stores the task ID that is marked to acquire the resource, and then the task obtains the task group resource and run. 

#### Task Group Flowchart 

<p align="center">
    <img src="/img/task_group_process.png" width="80%" />
</p>        
