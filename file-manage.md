When the third-party jar needs to be used in the scheduling process or the user needs to customize the script, the related operations can be completed on this page. The file types that can be created include: `txt/log/sh/conf/py/java` etc. And you can edit, rename, download and delete files.

Basic operation
---------------

![file-manage](/img/new_ui/dev/resource/file-manage.png)

### Create a file

The file format supports the following types: txt, log, sh, conf, cfg, py, java, sql, xml, hql properties.

![create-file](/img/new_ui/dev/resource/create-file.png)

### Upload files

Upload file: Click the "`Upload File`" button to upload, drag and drop the file to the upload area, the file name will be automatically completed with the uploaded file name.

![upload-file](/img/new_ui/dev/resource/upload-file.png)

### File view

For viewable file types, click the file name to view the file details.

![file_detail](/img/tasks/demo/file_detail.png)

### Download file

Click the "`Download`" button in the file list to download the file or click the "`Download`" button in the upper right corner of the file details to download the file.

### File rename

![rename-file](/img/new_ui/dev/resource/rename-file.png)

### Delete Files

File list -> Click the "`Delete`" button to delete the specified file.

### Re-upload file

Click the "`Re-upload file`" button in the file list to re-upload the file, drag and drop the file to the upload area, and the file name will be automatically completed with the uploaded file name.

![reuplod_file](/img/reupload_file_en.png)

> Note: When uploading, creating and renaming files, neither the file name nor the source file name (when uploading) can have `.`and `/`special symbols.

Task Example
------------

This sample mainly uses a simple shell script to demonstrate how to use Content Center files in a workflow definition. The same is true for tasks such as MR and Spark that require jar packages.

### Create shell file

Create a shell file that outputs "hello world".

![create-shell](/img/new_ui/dev/resource/demo/file-demo01.png)

### Create a workflow execution file

In the Workflow Definition module of Project Management, create a new workflow using shell tasks.

*   script:`sh hello.sh`
*   Resources: Select`hello.sh`

![use-shell](/img/new_ui/dev/resource/demo/file-demo02.png)

### View Results

You can view the log results of the node running in the workflow instance. As shown below:

![log-shell](/img/new_ui/dev/resource/demo/file-demo03.png)