# 文件管理

当在调度过程中需要使用到第三方的 jar 或者用户需要自定义脚本的情况，可以通过在该页面完成相关操作。可创建的文件类型包括：`txt/log/sh/conf/py/java` 等。并且可以对文件进行编辑、重命名、下载和删除等操作。

## 基础操作

![file-manage](/img/new_ui/dev/resource/file-manage.png)

### 创建文件

文件格式支持以下几种类型：txt、log、sh、conf、cfg、py、java、sql、xml、hql、properties

![create-file](/img/new_ui/dev/resource/create-file.png)

### 上传文件

上传文件：点击"上传文件"按钮进行上传，将文件拖拽到上传区域，文件名会自动以上传的文件名称补全

![upload-file](/img/new_ui/dev/resource/upload-file.png)

### 文件查看

对可查看的文件类型，点击文件名称，可查看文件详情

![file_detail](/img/tasks/demo/file_detail.png)

### 下载文件

点击文件列表的"下载"按钮下载文件或者在文件详情中点击右上角"下载"按钮下载文件

### 文件重命名

![rename-file](/img/new_ui/dev/resource/rename-file.png)

### 删除文件

文件列表->点击"删除"按钮，删除指定文件

### 重新上传文件

点击文件列表中的”重新上传文件“按钮进行重新上传文件，将文件拖拽到上传区域，文件名会自动以上传的文件名称补全

![reuplod_file](/img/reupload_file_en.png)

> 注意：上传、创建、重命名文件时，文件名和源文件名（上传时）均不能带有 `.` 以及 `/` 特殊符号。

## 任务样例

该样例主要通过一个简单的 shell 脚本，来演示如何在工作流定义中使用资源中心的文件。像 MR、Spark 等任务需要用到 jar 包，也是同理。

### 创建 shell 文件

创建一个 shell 文件，输出 “hello world”。

![create-shell](/img/new_ui/dev/resource/demo/file-demo01.png)

### 创建工作流执行文件

在项目管理的工作流定义模块，创建一个新的工作流，使用 shell 任务。

- 脚本：`sh hello.sh`
- 资源：选择 `hello.sh`

![use-shell](/img/new_ui/dev/resource/demo/file-demo02.png)

### 查看结果

可以在工作流实例中，查看该节点运行的日志结果。如下图：

![log-shell](/img/new_ui/dev/resource/demo/file-demo03.png)




