# 企业微信

如果您需要使用到企业微信进行告警，请在告警实例管理里创建告警实例，选择 WeChat 插件。企业微信的配置样例如下

![enterprise-wechat-plugin](/img/alert/enterprise-wechat-plugin.png)

其中 send.type 分别对应企微文档：

应用：https://work.weixin.qq.com/api/doc/90000/90135/90236

群聊：https://work.weixin.qq.com/api/doc/90000/90135/90248

user.send.msg 对应文档中的 content，与此相对应的值的变量为 {msg}