# Log specification

## Preface

Logs are used to track and record various actions during the development and operation of a system. Standardized log printing can help users or developers to quickly understand the system operation status and locate problems.

Apache DolphinScheduler uses the Logback logging framework to print logs according to four levels: DEBUG, WARN, INFO, and ERROR, with a priority of DEBUG < INFO < WARN < ERROR.

## Specifications

### Log level specification

Different levels of logs play different roles in the business process, and failure to use reasonable log levels for printing can cause great difficulties for system operations and maintenance.

- DEBUG level is used in the development and testing process to output debugging information. Developers should print parameter information, process details, and result information during debugging as much as possible using this level to facilitate locating and analyzing problems during the development and testing phases. In addition, it is prohibited to use this level to print logs in the production environment.
- INFO level is used to record information during system operation. The logs printed using this level should be able to reflect the behavior of the system, such as status changes of workflows, tasks, etc.
- WARN level is used to warn of problems that will occur during operation. For example, the checksum of API module parameters, etc.
- ERROR level is used to record some unpredictable errors and exceptions that will affect the system process. For example, errors and exceptions that cause workflows and tasks to fail to complete properly.

### Log content specification

The content of the logs determines whether the logs can completely restore the system behavior or state.

- DEBUG-level logs record debugging information during the development process, and appear at critical programs that need to be debugged, covering detailed site information, parameters, results, etc.

- INFO-level logs need to record the status information or operation information of the current program calls, and play the role of describing the system operation process. Therefore, the logs at this level need to appear at the critical point of the system operation, and their contents need to cover the description of the critical point, parameters and results. For example, when a workflow instance is scheduled, the status change of each key link is printed.

- WARN-level logs record information about tolerable errors that occur in current program calls that do not affect the normal operation of the system or functionality, but the content of this level log also needs to cover detailed site descriptions, parameters, and results. For example, when the API module interface parameter verification fails, the description of the verification failure and the parameters are recorded.

- ERROR-level logs record information about intolerable errors that occur in the current program call, which may cause the system or function to fail to operate normally. Therefore, the logs at this level need to record the error description, site parameters, error results, etc. in detail to ensure that the problem and the cause can be quickly located based on the log. In addition, when handling exceptions, if you are sure you want to print the stack information, use the following format:

  ```java
  logger.error("description of current error, parameter is {}", parameter, e);
  ```

### Log format specification

The logs of Master module and Worker module are printed using the following format.

```xml
[%level] %date{yyyy-MM-dd HH:mm:ss.SSS Z} %logger{96}:[%line] - [WorkflowInstance-%X{workflowInstanceId:-0}][TaskInstance-%X{taskInstanceId:-0}] - %msg%n
```

That is, the workflow instance ID and task instance ID are injected in the printed logs using MDC, so the developer needs to get the IDs and inject them before printing the logs related to the workflow instance and task instance in these two modules; after the printing is finished, the related IDs need to be removed.

## Cautions

- Disable the use of standard output to print logs. Standard output can greatly affect system performance.
- The use of printStackTrace() is prohibited for exception handling. This method prints the exception stack information to the standard error output.
- Branch printing of logs is prohibited. The contents of the logs need to be associated with the relevant information in the log format, and printing them in separate lines will cause the contents of the logs to not match the time and other information, and cause the logs to be mixed in a large number of log environments, which will make log retrieval more difficult.
- The use of the "+" operator for splicing log content is prohibited. Use placeholders for formatting logs for printing to improve memory usage efficiency.
- When the log content includes object instances, you need to make sure to override the toString() method to prevent printing meaningless hashcode.

