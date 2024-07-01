# Health Check

## Background

Health check are designed to provide a unique way to check the health of the dolphinscheduler service. It includes the health status of modules, such as DB, cache, network, etc.

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

> Notice: If you modify the default service port and address, you need to modify the IP+Port to the modified value.

