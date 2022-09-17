# 健康检查

## 背景

运行状况检查旨在提供一种独特的方法来检查 Dolphinscheduler 服务的运行状况。它包括模块的运行状况，例如 DB、缓存、网络等等。

## Endpoint

### API-Server

```shell
curl --request GET 'http://localhost:12345/dolphinscheduler/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
```

### Master-Server

```shell
curl --request GET 'http://localhost:5679/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
```

### Worker-Server

```shell
curl --request GET 'http://localhost:1235/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
```

### Alert-Server

```shell
curl --request GET 'http://localhost:50053/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
```

> 注意: 如果你修改过默认的服务端口和地址，那么你需要修改 IP+Port 为你修改后的值。

