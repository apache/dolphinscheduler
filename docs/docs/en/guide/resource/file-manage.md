# File Management

When third party jars are used in the scheduling process or user defined scripts are required, these can be created from this page. The types of files that can be created include: txt, log, sh, conf, py, java and so on. Files can be edited, renamed, downloaded and deleted.

![file-manage](/img/new_ui/dev/resource/file-manage.png)

- Create a file
  > The file format supports the following types: txt, log, sh, conf, cfg, py, java, sql, xml, hql, properties.

![create-file](/img/new_ui/dev/resource/create-file.png)

- upload files

> Upload file: Click the "Upload File" button to upload, drag the file to the upload area, the file name will be automatically completed with the uploaded file name.

![upload-file](/img/new_ui/dev/resource/upload-file.png)

- File View

> For the files that can be viewed, click the file name to view the file details.

![file_detail](/img/tasks/demo/file_detail.png)

- Download file

> Click the "Download" button in the file list to download the file or click the "Download" button in the upper right corner of the file details to download the file.

- File rename

![rename-file](/img/new_ui/dev/resource/rename-file.png)

- delete
  > File list -> Click the "Delete" button to delete the specified file.

- Re-upload file

  > Re-upload file: Click the "Re-upload File" button to upload a new file to replace the old file, drag the file to the re-upload area, the file name will be automatically completed with the new uploaded file name.

![reuplod_file](/img/reupload_file_en.png)

## Example

The example uses a simple shell script to demonstrate the use of resource center files in workflow definitions. The same is true for tasks such as MR and Spark, which require jar packages.

### Create a shell file

Create a shell file, print `hello world`.

![create-shell](/img/new_ui/dev/resource/demo/file-demo01.png)

Create the workflow execution shell

In the workflow definition module of project Manage, create a new workflow using a shell task.

- Script: 'sh hello.sh'
- Resource: Select 'hello.sh'

![use-shell](/img/new_ui/dev/resource/demo/file-demo02.png)

### View the results

You can view the log results of running the node in the workflow example. The diagram below:

![log-shell](/img/new_ui/dev/resource/demo/file-demo03.png)
