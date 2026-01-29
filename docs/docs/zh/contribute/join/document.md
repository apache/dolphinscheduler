# 文档须知

良好的使用文档对任何类型的软件都是至关重要的。欢迎任何可以改进 DolphinScheduler 文档的贡献。

### 获取文档项目

DolphinScheduler 项目的文档维护在独立的 [git 仓库](https://github.com/apache/dolphinscheduler-website)中。

首先你需要先将文档项目 fork 到自己的 github 仓库中，然后将 fork 的文档克隆到本地计算机中。

```
git clone https://github.com/<your-github-user-name>/dolphinscheduler-website
```

### 文档构建指南

1. 在根目录中运行 `yarn` 以安装依赖项。

2. 运行命令收集资源：2.1.运行 `export PROTOCOL_MODE=ssh` 告诉Git克隆资源，通过SSH协议而不是HTTPS协议。 2.2.运行 `./scripts/prepare_docs.sh` 准备所有相关资源，关更多信息，您可以查看[how prepare script work](https://github.com/apache/dolphinscheduler-website/blob/master/HOW_PREPARE_WORK.md)。

3. 在根目录下运行 `yarn generate` 来格式化和准备数据。

4. 在根目录下运行 `yarn dev` 启动本地服务器，其将允许在 http://localhost:3000 。

```
注意：如果您在 Windows 而非 Mac 或 Linux 中克隆代码。请阅读下面的详细信息。
如果执行上述两个步骤中的命令，将出现异常 “UnhandledPromiseRejectionWarning.Error:”： Error： EPERM: operation not permitted, symlink ‘2.0.3’ -> ‘latest’".
如果出现异常 “Can't resolve ‘antd’ in xxx”，你可以运行 `yarn add antd` 和 `yarn install`。
因为这两个步骤运行的命令`./scripts/prepare_docs.sh`应该是 Linux 环境，所以如果你是 Windows 系统，可以使用 WSL 来完成。
遇到这个问题时。你可以以管理员身份运行 cmd.exe 中的两个步骤。
```

5. 运行 `yarn build` 来构建源代码，此时会自动生成一个名为 `build` 目录，等待执行完成之后进入 `build` 目录。

6. 在本地验证你的更改：`python -m SimpleHTTPServer 8000`，当 python 版本为 3 时，请使用：`python3 -m http.server 8000`。

如果本地安装了更高版本的 node，可以考虑使用 `nvm` 来允许不同版本的 `node` 在你的计算机上运行。

1. 参考[说明](http://nvm.sh)安装 nvm

2. 运行 `nvm install v18.12.1` 安装 node v18

3. 运行 `nvm use v18.12.1` 将当前工作环境切换到 node v18

然后你就可以在本地环境运行和建立网站了。

### 文档规范

1. 汉字与英文、数字之间**需空格**，中文标点符号与英文、数字之间**不需空格**，以增强中英文混排的美观性和可读性。

2. 建议在一般情况下使用 “你” 即可。当然必要的时候可以使用 “您” 来称呼，比如有 warning 提示的时候。

### 怎样提交文档 Pull Request

1. 不要使用 “git add.” 提交所有更改。

2. 只需推送更改的文件，例如：

* `*.md`
* `blog.js or docs.js or site.js`

3. 向 **master** 分支提交 Pull Request。

### 参考文档

[Apache Flink 中文文档规范](https://cwiki.apache.org/confluence/display/FLINK/Flink+Translation+Specifications)
