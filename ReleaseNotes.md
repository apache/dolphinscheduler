## 1.2.1

### New Feature	
1. #1367,Add java checkstyle
2. Add feature ci
3. #1547,node text edit box supports full-screen magnification
4. Add github action to analyze code with sonarcloud
5. #1635,merge configurations in order to reduce configuration files


	
### Enhancement	
1. #184,Use the default workgroup in the page ,Instead of the value id.
2. #1441,add user success when user name contains '.'.
3. Support Mac local development and debugging
4. #839,add Spark Task Component can switch Spark Version.
5. DAG automatic layout
6. Merge frontend and backend tar into one binary tar
7. Remove master server and worker server listening port (5566、7788)
8. Remove kazoo
9. #1300,Add right alignment function in sql email content
10. #747,Worker Log desensitization
11. add profile nginx in order to deploy frontend


### Bug Fixes
1. Solve the problem that the pop-up box cannot be closed when the browser returns
2. Fix #1399,The wrong field order in logger.info
3. Fix #1379,SQL task，date parameter need to add explicit type casts
4. Fix #1477,some tasks would be running all the time when db delayed
5. Fix #1514,the field queue in table t_ds_user not change with tabke t_ds_queue modify queue_name field
