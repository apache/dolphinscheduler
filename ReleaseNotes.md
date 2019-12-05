## 1.2.0

### New Feature	
1. Support postgre sql
2. Change all Chinese names to English
3. Add flink and http task support
4. Cross project dependencies
5. Modify mybatis to mybatisplus, support multy databases.
6. Add export and import definition feaure
7. Github actions ci compile check
8. Add method and parameters comments
9. Add java doc for common module

	
### Enhancement	
1. Add license and notice files
2. Move batchDelete Process Define/Instance Outside for transactional
3. Remove space before and after login user name
4. Dockerfile optimization
5. Change mysql-connector-java scope to test
6. Owners and administrators can delete schedule
7. DB page rename and background color modification 
8. Add postgre performance monitor
9. Resolve style conflict, recipient cannot tab and value verification
10. Checkbox change background color and env to Chinese
11. Change chinese sql to english
12. Change sqlSessionTemplate singleton and reformat code 
13. The value of loadaverage should be two decimal places
14. Delete alert group need delete the relation of user and alert group
15. Remove check resources when delete tenant
16. Check processInstance state before delete worker group 
17. Add check user and definitions function when delete tenant
18. Delete before check to avoid KeeperException$NoNodeException

### Bug Fixes
1. Fix #1245, make scanCommand transactional 
2. Fix ZKWorkerClient not close PathChildrenCache
3. Data type convert error ，email send error bug fix
4. Catch exception transaction method does not take effect to modify
5. Fix the spring transaction not worker bug
6. Task log print worker log bug fix
7. Fix api server debug mode bug
8. The task is abnormal and task is running bug fix
9. Fix bug: tasks queue length error
10. Fix unsuitable error message
11. Fix bug: phone can be empty
12. Fix email error password
13. Fix CheckUtils.checkUserParams method
14. The process cannot be terminated while tasks in the status submit success
15. Fix too many connection in upgrade or create 
16. Fix the bug when worker execute task using queue. and remove checking
17. Resole verify udf name error and delete udf error 
18. Fix bug: task cannot submit when recovery failover
19. Fix bug: the administrator authorizes the project to ordinary users,but ordinary users cannot see the process definition created by the administrator
20. Fix bug: create dolphinscheduler sql failed