## 1.2.1

### New Feature	
1. [[#1497](https://github.com/apache/incubator-dolphinscheduler/issues/1497)] DAG automatic layout when use api call process definition create
2. [[ #747](https://github.com/apache/incubator-dolphinscheduler/issues/747)]  Worker server runtime log desensitization
3. [[#1635](https://github.com/apache/incubator-dolphinscheduler/issues/1635)] merge configurations in order to reduce configuration files
4. [[#1460](https://github.com/apache/incubator-dolphinscheduler/issues/1460)] Add CI
5. [[#1547](https://github.com/apache/incubator-dolphinscheduler/issues/1547)] node text edit box supports full-screen magnification
6. [[#1646](https://github.com/apache/incubator-dolphinscheduler/issues/1646)] Add github action to analyze code with sonarcloud
7. [[#1367](https://github.com/apache/incubator-dolphinscheduler/issues/1367)] Add java checkstyle

	
### Enhancement	
1. [[#184](https://github.com/apache/incubator-dolphinscheduler/issues/184)] Use the default workgroup in the page ,Instead of the value id.
2. [[#1441](https://github.com/apache/incubator-dolphinscheduler/issues/1441)] add user success when user name contains '.'.
3. [[#839](https://github.com/apache/incubator-dolphinscheduler/issues/839)] add Spark Task Component can switch Spark Version.
4. [[#1511](https://github.com/apache/incubator-dolphinscheduler/issues/1511)] Merge frontend and backend tar into one binary tar
5. [[#1509](https://github.com/apache/incubator-dolphinscheduler/issues/1509)] Remove master server and worker server listening port (5566、7788)
6. [[#1575](https://github.com/apache/incubator-dolphinscheduler/issues/1575)] Remove kazoo, simplify deployment
7. [[#1300](https://github.com/apache/incubator-dolphinscheduler/issues/1300)] Add right alignment function in sql email content
8. [[#1599](https://github.com/apache/incubator-dolphinscheduler/issues/1599)] add profile nginx in order to deploy frontend
9. Support Mac local development and debugging


### Bug Fixes
1. Solve the problem that the pop-up box cannot be closed when the browser returns
2. Fix [[#1399](https://github.com/apache/incubator-dolphinscheduler/issues/1399)] The wrong field order in logger.info
3. Fix [[#1379](https://github.com/apache/incubator-dolphinscheduler/issues/1379)] SQL task，date parameter need to add explicit type casts
4. Fix [[#1477](https://github.com/apache/incubator-dolphinscheduler/issues/1477)] some tasks would be running all the time when db delayed
5. Fix [[#1514](https://github.com/apache/incubator-dolphinscheduler/issues/1514)] the field queue in table t_ds_user not change with tabke t_ds_queue modify queue_name field
6. Fix [[#1768](https://github.com/apache/incubator-dolphinscheduler/issues/1768)] There are multiple pages of data. After deleting all the data on one page, the data is displayed as empty
7. Fix [[#1770](https://github.com/apache/incubator-dolphinscheduler/issues/1770)] After canceling the file authorization, the running workflow should not obtain resource files from the original tenant directory
8. Fix [[#1779](https://github.com/apache/incubator-dolphinscheduler/issues/1779)] The execution of the SUB_PROCESS task failed first, but eventually succeeded
9. Fix [[#1789](https://github.com/apache/incubator-dolphinscheduler/issues/1789)] Click to view the history, enter the task instance page, the results of the query based on the search conditions are displayed incorrectly
10. Fix [[#1810](https://github.com/apache/incubator-dolphinscheduler/issues/1810)] Workflow instance does not show dependencies
11. Fix [[#1816](https://github.com/apache/incubator-dolphinscheduler/issues/1816)] Add multiple dependencies, the workflow definitions of the first few dependencies read the workflow definition of the last project
12. Fix [[#1828](https://github.com/apache/incubator-dolphinscheduler/issues/1828)] After executing the authorized UDF function, the path of the read resource file is incorrect
