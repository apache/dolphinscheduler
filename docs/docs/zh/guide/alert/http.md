# HTTP告警

如果您需要使用到`Http`（GET或POST）进行告警，请在告警实例管理里创建告警实例，选择`Http`插件。

## 参数配置

|    **参数**    |                     **描述**                     |
|--------------|------------------------------------------------|
| URL          | 访问的`Http`连接URL,需要包含协议、Host、路径，如果是GET方法可以添加参数   |
| 请求方式         | 当前支持`GET`和`POST`以及`PUT`三种请求方式                  |
| 请求头(Headers) | `Http`请求的完整请求头，以JSON为格式(注意不包含Content-Type)，非必填 |
| 请求体(Body)    | Http`请求的完整请求体，以JSON为格式，非必填                     |
| Content-Type | 请求体的`Content-Type`，默认为`application/json`       |

> 告警消息，支持变量`$msg`，可在`URL`,`请求头`,`请求体`中使用，非必填。

### GET Http告警

GET `Http`告警指将告警结果作为参数通过`Http` GET方法进行请求。
下图是GET告警配置的示例:

![http-alert-msg-config](../../../../img/alert/http-alert-example.png)
