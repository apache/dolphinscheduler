# 安全中心（权限系统）

* 安全中心由系统管理员管理队列、租户、用户、告警组、Worker 分组、令牌等。项目成员权限与项目资源使用授权在项目管理中配置，边界见下文“授予权限”。
* 管理员登录，默认用户名/密码：admin/dolphinscheduler123

## 创建队列

- 队列是在执行 spark、mapreduce 等程序，需要用到“队列”参数时使用的。
- 管理员进入安全中心 -> 队列管理页面，点击“创建队列”按钮，创建队列。

> 注意：目前仅有 admin 用户可以修改队列。

![create-queue](../../../../img/new_ui/dev/security/create-queue.png)

## 添加租户

- 租户对应的是 Linux 的用户，用于 worker 提交作业所使用的用户。如果 linux 没有这个用户，则会导致任务运行失败。你可以通过修改 `worker.properties` 配置文件中参数 `worker.tenant.auto.create=true` 实现当 linux 用户不存在时自动创建该用户。`worker.tenant.auto.create=true` 参数会要求 worker 可以免密运行 `sudo` 命令
- 租户编码：**租户编码是 Linux上 的用户，唯一，不能重复**
- 管理员进入安全中心->租户管理页面，点击“创建租户”按钮，创建租户。

> 注意：
> 1. 目前仅有 admin 用户可以修改租户；
> 2. 如果您在 Linux 中手动创建一个租户，则需要将手动创建的租户添加到 dolphinscheduler 启动用户组，以便该租户拥有足够的工作目录权限。

![create-tenant](../../../../img/new_ui/dev/security/create-tenant.png)

## 创建普通用户

- 用户分为**管理员用户**和**普通用户**
  * 系统管理员管理全局配置并通过项目操作权限校验；使用项目资源仍需相应的项目授权。
  * 普通用户可以创建项目，在项目内按显式分配的操作权限查看、开发、发布或执行工作流。
  * 注意：如果该用户切换了租户，则该用户所在租户下所有资源将复制到切换的新租户下。
- 进入安全中心->用户管理页面，点击“创建用户”按钮，创建用户。

![create-user](../../../../img/new_ui/dev/security/create-user.png)

### 编辑用户信息

- 管理员进入安全中心->用户管理页面，点击"编辑"按钮，编辑用户信息。
- 普通用户登录后，点击用户名下拉框中的用户信息，进入用户信息页面，点击"编辑"按钮，编辑用户信息。

### 修改用户密码

- 管理员进入安全中心->用户管理页面，点击"编辑"按钮，编辑用户信息时，输入新密码修改用户密码。
- 普通用户登录后，点击用户名下拉框中的用户信息，进入修改密码页面，输入密码并确认密码后点击"编辑"按钮，则修改密码成功。

## 创建告警组

* 告警组是在启动时设置的参数，在流程结束以后会将流程的状态和其他信息以邮件形式发送给告警组。
* 管理员进入安全中心->告警组管理页面，点击“创建告警组”按钮，创建告警组。

![create-alarmInstance](../../../../img/new_ui/dev/security/create-alarmInstance.png)

## 令牌管理

> 由于后端接口有登录检查，令牌管理提供了一种可以通过调用接口的方式对系统进行各种操作。
> - 管理员进入安全中心->令牌管理页面，点击“创建令牌”按钮，选择失效时间与用户，点击"生成令牌"按钮，点击"提交"按钮，则选择用户的token创建成功。

![create-token](../../../../img/new_ui/dev/security/create-token.png)

- 普通用户登录后，点击用户名下拉框中的用户信息，进入令牌管理页面，选择失效时间，点击"生成令牌"按钮，点击"提交"按钮，则该用户创建 token 成功。

- 调用示例：

```java
    /**
     * test token
     */
    public  void doPOSTParam()throws Exception{
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // create http post request
        HttpPost httpPost = new HttpPost("http://127.0.0.1:12345/escheduler/projects/create");
        httpPost.setHeader("token", "123");
        // set parameters
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("projectName", "qzw"));
        parameters.add(new BasicNameValuePair("desc", "qzw"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            // execute
            response = httpclient.execute(httpPost);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }
```

## 授予权限

以下规则适用于 3.5.0 引入的项目权限模型。权限分为两个维度：**用户在项目中能做什么**，以及**项目可以使用哪些资源**。涉及资源的操作需要同时满足对应的项目操作权限和项目资源授权。

### 用户的项目操作权限

|         权限         |                允许的操作                |
|--------------------|-------------------------------------|
| `VIEW`             | 查看项目、工作流和任务定义、实例、日志及项目内已授权资源的可选列表   |
| `DEVELOP`          | 创建、编辑、删除工作流和任务定义，导入、复制或移动定义         |
| `PUBLISH`          | 上线、下线工作流和任务定义                       |
| `EXECUTE`          | 手动启动、补数、重跑、恢复、暂停或停止工作流实例，以及执行实例中的任务 |
| `MANAGE_SCHEDULES` | 创建、修改、上线、下线或删除定时调度                  |
| `MANAGE_MEMBERS`   | 分配或撤销当前项目内用户的上述操作权限                 |

这些权限独立授予，不自动相互包含。例如，`DEVELOP` 不包含 `PUBLISH` 或 `EXECUTE`；授予操作权限也不会自动补充 `VIEW`，通过界面查看和操作项目通常还需显式授予 `VIEW`。

项目创建时，创建者获得全部六项显式权限，之后可以被撤销。普通用户不因创建者身份、旧授权表记录或资源所有者身份绕过项目操作权限校验。**修改项目信息和删除项目是独立的生命周期操作，仅系统管理员或记录的项目创建者可以执行**，且仍需满足项目可删除等业务条件；`DEVELOP`、`MANAGE_MEMBERS` 不授予这些操作。撤销创建者的成员权限不会改变其记录的创建者身份，修改项目信息也不会转移项目所有者。

系统管理员通过项目操作权限校验，但在保存、上线或执行等涉及资源的操作中，项目仍必须具有相应资源授权。

### 项目资源使用授权

系统管理员可以向项目授予以下八类资源的使用权限：数据源、Worker Group、租户、队列、环境、告警组、Kubernetes Namespace 和任务组。授权属于项目，供项目成员在其操作权限范围内使用，无需再逐个授予成员个人资源权限。

资源使用授权**不授予资源的修改、删除或转授权权限**。`MANAGE_MEMBERS` 仅管理当前项目的成员操作权限，不能授予或撤销项目资源，也不能将用户提升为系统管理员。项目资源的授予、撤销，以及不带项目上下文的全局资源目录查询，仅系统管理员可以执行。

项目内资源选择接口需要携带 `projectCode`，并同时按用户的 `VIEW` 和项目资源授权过滤。数据源选择结果不返回连接参数，环境选择结果不返回环境配置。资源文件不在上述八类资源中，继续使用原有的资源文件权限机制。

默认资源也需要显式授权，包括实际使用的 `default` Worker Group 和内置 `default` 租户。内置默认租户已有数据库记录，无需重复创建；租户配置了队列时，项目还需获得该队列的授权。仅在选择了具体环境、告警组等资源时，才需要相应授权。

### 各操作的校验边界

|              操作               |                      用户项目权限                       |           项目资源校验            |
|-------------------------------|---------------------------------------------------|-----------------------------|
| 创建、更新或导入工作流/任务定义              | `DEVELOP`                                         | 检查所保存任务引用的资源                |
| 复制或移动工作流到其他项目                 | 复制需源项目 `VIEW`；移动需源项目 `DEVELOP`；两者均需目标项目 `DEVELOP` | 按目标项目检查任务引用的资源，源项目授权不会随定义转移 |
| 上线工作流或任务定义                    | `PUBLISH`                                         | 检查该版本任务引用的资源                |
| 手动启动、补数、重跑、失败恢复、暂停恢复或执行实例中的任务 | `EXECUTE`                                         | 检查所执行版本的任务资源，以及生效的执行参数使用的资源 |
| 创建、修改或上线定时调度                  | `MANAGE_SCHEDULES`                                | 检查任务资源，以及调度执行参数使用的资源        |
| 下线工作流或任务定义                    | `PUBLISH`                                         | 不要求重新取得已撤销的资源授权             |
| 下线或删除定时调度                     | `MANAGE_SCHEDULES`                                | 不要求重新取得已撤销的资源授权             |
| 暂停或停止已有实例                     | `EXECUTE`                                         | 不要求重新取得已撤销的资源授权             |

执行参数中的 Worker Group、租户及其队列、所选环境和告警组也在检查范围内。重跑或恢复既有实例时，检查该实例记录的工作流版本和执行参数，而不是只检查当前最新定义。

**执行人不需要 `DEVELOP`，也不需要工作流所用数据源等上述八类资源的个人授权；但执行人的项目 `EXECUTE` 不能替代项目资源授权。** 例如，管理员将数据源 R 授权给项目 P，开发者 A 具有 P 的 `VIEW` 和 `DEVELOP`，可以保存使用 R 的工作流；执行人 B 具有 P 的 `VIEW` 和 `EXECUTE`，即可手动执行该工作流，无需再向 B 单独授权 R，也不能因此编辑工作流或修改 R。

### 跨项目和子工作流

定义和实例的权限按其实际所属项目校验，资源使用按操作对应的项目授权校验，不能用请求路径中另一个项目的权限代替。批量查询工作流任务时，普通用户只能得到自己具有 `VIEW` 的实际项目中的结果，旧关联表记录不会恢复可见性；父子实例和血缘等跨项目查询也需要相应项目的 `VIEW`。

保存或上线引用子工作流的定义时，用户还需具有所引用子工作流所在项目的 `EXECUTE`。执行或配置、上线调度时，递归检查子工作流：用户需具有每个子工作流所在项目的 `EXECUTE`，任务资源按各子工作流自己的项目授权校验。继承的租户、队列、Worker Group、环境和告警组等执行参数也按子项目校验，父项目授权不能代替子项目授权。

### 撤销授权的生效范围

- 撤销用户的项目操作权限后，后续需要该权限的请求会被拒绝。各权限独立撤销，单独撤销 `VIEW` 不会同时撤销其他操作权限。撤销 `VIEW` 后，用户无法继续通过项目列表、批量任务查询等入口查看该项目；创建者的生命周期操作边界见上文。
- 撤销项目资源授权后，后续保存、上线、手动启动、补数、重跑或恢复等操作，以及新建、修改、上线定时调度，均按当前授权重新校验。上述示例中，撤销 P 对 R 的授权后，即使 B 仍有 `EXECUTE`，也不能再手动启动使用 R 的工作流。
- **撤权不追溯中止已运行或排队的实例，不自动下线已有定时调度，也不会在已有定时的后续后台触发或自动重试时重新检查这些项目授权。** 因此，仅撤销权限不能保证已有工作负载停止；需要停止后续定时触发时，应另行下线定时调度，需要停止已有实例时，应另行发起停止操作，这些操作仍需对应权限。

项目授权限定 DolphinScheduler 的操作和资源选择范围。任务仍在 Worker 上使用所配置的租户和资源凭证执行；实际可访问的文件、网络和数据库对象还受操作系统、部署环境和数据源凭证权限约束。项目授权不提供任务代码的沙箱隔离，也不等同于数据库表级授权。

### 配置与升级

在项目管理中，系统管理员或具有 `MANAGE_MEMBERS` 的用户通过“成员权限”为项目成员勾选操作权限并保存；保存会替换该成员在当前项目的权限集合，清空集合即撤销全部成员权限。系统管理员通过“资源授权”选择资源类型和资源，为项目授权或撤权。成员管理者可为自己或其他用户分配当前项目的六项操作权限，包括 `MANAGE_MEMBERS`，应按此范围选择可信任的成员管理者。

从旧版本升级时，旧授权表及数据保留，但不自动迁移，也不作为新校验的回退。管理员需要重新配置项目成员权限和项目资源授权，包含实际使用的默认资源。参见[升级不兼容说明](../upgrade/incompatible.md#350)。

以下截图为 3.5.0 之前的个人授权界面，仅供升级对照；其中读/写权限和个人数据源授权不能代替上述项目成员权限及项目资源授权。

![project-authroize-step-1](../../../../img/new_ui/dev/security/project-authroize-step-1.png)

![project-authroize-step-2](../../../../img/new_ui/dev/security/project-authroize-step-2.png)

![no-permission-error](../../../../img/new_ui/dev/security/no-permission-error.png)

## Worker 分组

每个 worker 节点都会归属于自己的 worker 分组，默认分组为 `default`。

在任务执行时,可以将任务分配给指定 worker 分组，最终由该组中的 worker 节点执行该任务。

### 新增 / 更新 worker 分组

- 打开要设置分组的 worker 节点上的 `worker-server/conf/application.yaml` 配置文件. 修改 `worker` 配置下的 `groups` 参数.
- `groups` 参数的值为 worker 节点对应的分组名称，默认为 `default`。
- 如果该 worker 节点对应多个分组，则用连字符列出，示范如下：

```conf
worker:
......
  groups:
    - default
    - group1
    - group2
......
```

- 也可以在运行中添加 worker 所属的 worker 分组而忽略 `application.yaml` 中的配置。修改步骤为 `安全中心` -> `worker分组管理` -> 点击 `创建worker分组` -> 输入`分组名称`和`worker地址` -> 点击`确定`

## 环境管理

* 在线配置 worker 运行环境，一个 worker 可以指定多个环境，每个环境等价于 dolphinscheduler_env.sh 文件.

* 默认环境为dolphinscheduler_env.sh文件.

* 在任务执行时,可以将任务分配给指定 worker 分组，根据 worker 分组选择对应的环境，最终由该组中的 worker 节点执行环境后执行该任务.

> 创建/更新 环境

- 环境配置等价于dolphinscheduler_env.sh文件内配置

![create-environment](../../../../img/new_ui/dev/security/create-environment.png)

> 使用环境

- 在工作流定义中创建任务节点选择 worker 分组和 worker 分组对应的环境，任务执行时 worker 会先执行环境在执行任务.

![use-environment](../../../../img/new_ui/dev/security/use-environment.png)

> 注意: 当无法在任务定义或工作流运行对话框中使用你想要使用的环境时，请检查您已经选择worker，并且您要使用的环境已经关联到您选择的worker中；还需确认项目已获得该环境和 Worker Group 的资源使用授权

## 集群管理

> 创建/更新 集群

- 每个工作流可以绑定零到若干个集群用来支持多集群，目前先用于k8s。

> 使用集群

- 创建和授权后，k8s命名空间和工作流会增加关联集群的功能。每一个集群会有独立的工作流和任务实例独立运行。

![create-cluster](../../../../img/new_ui/dev/security/create-cluster.png)

## 命名空间管理

> 创建/更新 k8s集群

- 先把k8s集群连接的配置录入 database 的表 `t_ds_k8s`给批次使用后续移除，namespace的创建现在通过下拉选择集群.

> 创建/更新 namespace

- 创建和授权后，在相关k8s任务选择命名空间时下拉可选，如果k8s集群名字是`ds_null_k8s`是测试模式，不会真正操作集群.

![create-environment](../../../../img/new_ui/dev/security/create-namespace.png)
