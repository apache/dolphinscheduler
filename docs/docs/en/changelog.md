# Change Log

## Bugfix

- change alert start.sh (#13100)
- [fix] Add token as authentication for python gateway (#12893)
- [Bug-13010] [Task] The Flink SQL task page selects the pre-job deployment mode, but the task executed by the worker is the Flink local mode
- [Bug-12997][api] Fix that the end time is not reset when the workflow instance reruns. (#12998)
- [Bug-12994] [Worker] Fix kill process does not take effect (#12995)
- Fix sql task will send alert if we don't choose the send email #12984
- [Bug-13008] [UI] When using the complement function, turn on the dependent mode to generate multiple unrelated workflow instances (#13009)
- [fix][doc] python api release link
- [fix] Python task can not pass the parameters to downstream task. (#12961)
- [Fix] Fix Java path in Kubernetes Helm Chart (#12987)
- [Bug-12963] [Master] Fix dependent task node null pointer exception (#12965)
- [Bug-12954] [Schedule] Fix that workflow-level configuration information does not take effect when timing triggers execution
- Fix execute shell task exception no dolphinscheduler_env.sh file execute permission (#12909)
- Upgrade clickhouse jdbc driver #12639
- add spring-context to alert api (#12892)
- [Upgrade][sql]Modify the table t_ds_worker_group to add a description field in the postgresql upgrade script #12883
- fix NPE while retry task (#12903)
- Fix-12832][API] Fix update worker group exception group name already exists. #12874
- fix and enhance helm db config (#12707)

## Document

- [fix][doc] Fix sql-hive and hive-cli doc (#12765)
- [Bug] [Alert] Ignore alert not write info to db (#12867)
- [doc] Add skip spotless check during ASF release #12835
- [Doc][bug] Fix dead link caused by markdown cross-files anchor #12357 (#12877)

## Python API

- [fix] python api upload resource center failed
- [Feature] Add CURD to the project/tenant/user section of the python-DS (#11162)
- [chore][python] Change name from process definition to workflow (#12918)
- [feat] Support set execute type to pydolphinscheduler (#12871)
- [hotfix] Correct python doc link
- [improve][python] Validate version of Python API at launch (#11626)

**Full Changelog**: https://github.com/apache/dolphinscheduler/compare/3.1.1...3.1.2
