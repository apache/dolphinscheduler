# API 调用

## 背景
一般都是通过页面来创建项目、流程等，但是与第三方系统集成就需要通过调用 API 来管理项目、流程

## 操作步骤

### 创建 token

1. 登录调度系统，点击 "安全中心"，再点击左侧的 "令牌管理"，点击 "令牌管理" 创建令牌

<p align="center">
   <img src="/img/token-management.png" width="80%" />
 </p>
 
2. 选择 "失效时间" (Token有效期)，选择 "用户" (以指定的用户执行接口操作)，点击 "生成令牌" ，拷贝 Token 字符串，然后点击 "提交" 
<p align="center">
   <img src="/img/create-token.png" width="80%" />
 </p>

### 使用 Token
1. 打开 API文档页面
    > 地址：http://{api server ip}:12345/dolphinscheduler/doc.html?language=zh_CN&lang=cn
<p align="center">
   <img src="/img/api-documentation.png" width="80%" />
 </p>
    
2. 选一个测试的接口，本次测试选取的接口是：查询所有项目
    > projects/query-project-list

3. 打开 Postman，填写接口地址，并在 Headers 中填写 Token，发送请求后即可查看结果
    ```
    token:刚刚生成的Token
    ```
<p align="center">
   <img src="/img/test-api.png" width="80%" />
 </p>
 
### 创建项目
这里以创建名为 "wudl-flink-test" 的项目为例
<p align="center">
   <img src="/img/api/create_project1.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/api/create_project2.png" width="80%" />
 </p>
 
<p align="center">
   <img src="/img/api/create_project3.png" width="80%" />
 </p>
返回 msg 信息为 "success"，说明我们已经成功通过 API 的方式创建了项目。

如果您对创建项目的源码感兴趣，欢迎继续阅读下面内容
### 附:创建项目源码

<p align="center">
   <img src="/img/api/create_source1.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/api/create_source2.png" width="80%" />
 </p>


