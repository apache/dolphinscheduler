# 日志规范

## 前言

日志被用来追踪、记录系统开发、运行期间的各种行为。规范的日志打印可以帮助用户或开发者快速了解系统运行状态、定位问题。

Apache DolphinScheduler使用Logback日志框架，按照DEBUG、WARN、INFO、ERROR四个级别对日志进行打印，其优先级为DEBUG < INFO < WARN < ERROR。

## 规范

### 日志级别规范

不同级别的日志在业务过程中起着不同的作用，如果不能使用合理的日志级别进行打印，会对系统运维带来很大的困难。

- DEBUG级别在开发、测试过程中使用，用于输出调试信息。开发者尽可能将调试过程中的参数信息，过程细节，结果信息使用该级别打印，方便在开发、测试阶段定位、分析问题。此外，禁止在生产环境中使用该级别打印日志。
- INFO级别用于记录系统运行期间的信息。使用该级别打印的日志应能够反映系统的行为，比如工作流、任务的状态变化等。
- WARN级别用于对运行过程中将会出现的问题进行警告。比如API模块参数的校验等。
- ERROR级别用于记录一些不可预知的错误、异常，这些错误、异常会影响系统流程。比如导致工作流、任务无法正常完成的错误、异常。

### 日志内容规范

日志内容是否规范决定着日志能否完整地还原系统行为或状态。

- DEBUG级别的日志记录开发过程中的调试信息。DEBUG级别的日志出现在需要调试的关键程序处，其内容涵盖细致的现场信息，参数，结果等。

- INFO级别的日志需要记录当前程序调用的状态信息或运行信息，起到描述系统运行过程的作用。所以该级别的日志需要在系统运行的关键环节出现，其内容需涵盖关键环节描述，参数以及结果等。比如工作流实例被调度时，打印每一个关键环节的状态变化。

- WARN级别的日志记录当前程序调用中发生的可容忍错误的信息，该错误不会影响系统或功能的正常运行，但是该级别的日志内容也需要涵盖详细地环节描述，参数以及结果等信息。比如API模块接口参数校验失败时，记录校验失败的描述，参数。

- ERROR级别的日志记录当前程序调用中发生的不可容忍错误的信息，该错误会导致系统或功能无法正常运行。所以该级别的日志需要详细地记录错误描述，现场参数，错误结果等，保证能够根据日志快速定位到问题以及原因。此外，处理异常时如果确定要打印堆栈信息，使用如下格式：

  ```java
  logger.error("description of current error, parameter is {}", parameter, e);
  ```

### 日志格式规范

Master模块和Worker模块的日志打印使用如下格式。即在打印的日志中使用MDC注入工作流实例ID和任务实例ID，因此开发者在打印这两个模块中与工作流实例和任务实例有关的日志前，需要获取ID并注入；在打印完成后，需要移除相关ID。

```xml
[%level] %date{yyyy-MM-dd HH:mm:ss.SSS Z} %logger{96}:[%line] - [WorkflowInstance-%X{workflowInstanceId:-0}][TaskInstance-%X{taskInstanceId:-0}] - %msg%n
```

## 日志配置修改

DolphinScheduler使用[`LogBack`](https://docs.spring.io/spring-boot/docs/2.1.8.RELEASE/reference/html/howto-logging.html)作为日志工具。若您要修改某个包的日志打点级别，您需要修改对应模块的`logback-spring.xml`文件。
举例来说，若您需要将`standalone`模式下`org.springframework.web`包日志提升到`DEBUG`级别，您需要在`apache-dolphinscheduler-dev-SNAPSHOT-bin/standalone-server/conf/logback-spring.xml`文件中加入如下配置：

```xml
<configuration scan="true" scanPeriod="120 seconds">

......

  <logger name="org.springframework.web" level="DEBUG">
    <appender-ref ref="STANDALONELOGFILE" />
    <appender-ref ref="TASKLOGFILE"/>
  </logger>

......

</configuration>
```

## 注意事项

- 禁止使用标准输出打印日志。标准输出会极大影响系统性能。
- 异常处理时禁止使用printStackTrace()。该方法会将异常堆栈信息打印到标准错误输出中。
- 禁止分行打印日志。日志的内容需要与日志格式中的相关信息关联，如果分行打印会导致日志内容与时间等信息匹配不上，并且在大量日志环境下导致日志混合，会加大日志检索难度。
- 禁止使用"+"运算符对日志内容进行拼接。使用占位符进行日志格式化打印，提高内存使用效率。
- 日志内容中包括对象实例时，需要确保重写toString()方法，防止打印无意义的hashcode。

