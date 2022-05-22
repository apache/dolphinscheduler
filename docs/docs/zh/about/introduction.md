# 关于DolphinScheduler

Apache DolphinScheduler是一个分布式易扩展的可视化DAG工作流任务调度开源系统。解决数据研发ETL 错综复杂的依赖关系，不能直观监控任务健康状态等问题。DolphinScheduler以DAG流式的方式将Task组装起来，可实时监控任务的运行状态，同时支持重试、从指定节点恢复失败、暂停及Kill任务等操作

# 简单易用
DAG监控界面，所有流程定义都是可视化，通过拖拽任务定制DAG，通过API方式与第三方系统对接, 一键部署
# 高可靠性
去中心化的多Master和多Worker, 自身支持HA功能, 采用任务队列来避免过载，不会造成机器卡死
# 丰富的使用场景
支持暂停恢复操作.支持多租户，更好的应对大数据的使用场景. 支持更多的任务类型，如 spark, hive, mr, python, sub_process, shell
# 高扩展性
支持自定义任务类型，调度器使用分布式调度，调度能力随集群线性增长，Master和Worker支持动态上下线
