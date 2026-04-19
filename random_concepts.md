# Random Concepts in Apache DolphinScheduler

## Concept 1: Task Execution State Machine

DolphinScheduler uses a **State Machine pattern** for task lifecycle management. Each state (SUBMITTED, RUNNING, SUCCESS, FAILED, etc.) has a dedicated `ITaskStateAction` handler. When a lifecycle event arrives, the current state's action processes it, enabling clean, isolated transitions without tangled conditionals — making the system highly extensible and maintainable.
