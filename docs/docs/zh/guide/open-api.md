# API 调用

## 背景

一般都是通过页面来创建项目、流程等，但是与第三方系统集成就需要通过调用 API 来管理项目、流程。

## 操作步骤

### 创建 token

1. 登录调度系统，点击 "安全中心"，再点击左侧的 "令牌管理"，点击 "令牌管理" 创建令牌。

![create-token](/img/new_ui/dev/security/create-token.png)
 
2. 选择 "失效时间" (Token 有效期)，选择 "用户" (以指定的用户执行接口操作)，点击 "生成令牌" ，拷贝 Token 字符串，然后点击 "提交" 。

![token-expiration](/img/new_ui/dev/open-api/token_expiration.png)

### 使用案例

#### 查询项目列表信息

1. 打开 API 文档页面

> 地址：http://{api server ip}:12345/dolphinscheduler/doc.html?language=zh_CN&lang=cn

![api-doc](/img/new_ui/dev/open-api/api_doc.png)
    
2. 选一个测试的接口，本次测试选取的接口是：查询所有项目

> projects/list

3. 打开 Postman，填写接口地址，并在 Headers 中填写 Token，发送请求后即可查看结果

    ```
    token: 刚刚生成的 Token
    ```
   
![api-test](/img/new_ui/dev/open-api/api_test.png)
 
#### 创建项目

这里演示如何使用调用 api 来创建对应的项目。

通过查阅 api 文档，在 Postman 的 Headers 中配置 KEY 为 Accept，VALUE 为 application/json 的参数。

![create-project01](/img/new_ui/dev/open-api/create_project01.png)

然后再 Body 中配置所需的 projectName 和 description 参数。

![create-project02](/img/new_ui/dev/open-api/create_project02.png)

检查 post 请求结果。

![create-project03](/img/new_ui/dev/open-api/create_project03.png)

返回 msg 信息为 "success"，说明我们已经成功通过 API 的方式创建了项目。

如果您对创建项目的源码感兴趣，欢迎继续阅读下面内容

### 附:创建项目源码

![api-source01](/img/new_ui/dev/open-api/api_source01.png)

![api-source02](/img/new_ui/dev/open-api/api_source02.png)


