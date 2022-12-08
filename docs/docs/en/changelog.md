# Change Log

## Bugfix

- [fix-12675]edit workflow related task, workflow's task version change
- [Bug] Resource default auth function disabled false.
- [3.1.1][api]Fix updating workflow definition causing task definition data to be duplicated. (#12712)
- Fix flink sql cannot run due to missing main jar (#12705)
- Add pythonNodePort in config file (#12685)
- [Fix-12109]Fix the errors when starting 2 times with dolphinscheduler-daemon.sh (#12118)
- Fix the waiting strategy cannot recovery if the serverstate is already in running (#12651)
- Fix alert status spelling error #12592
- Add configmap resource permissions so config hot reload can work (#12572)
- [Fix][ui] download resource return 401 (#12566)
- [Bug] [API] Before deleting a worker group, check whether there is environment that reference the worker group. #12534
- [Fix-12518][swagger]Fill up missing i18 properties (#12599)
- [Bug][master] Add the aws-java-sdk-s3 jar package to the master module (#12259) (#12512)
- Remove equals in User to fix UT #12487
- [Bug] Set tenantDir permission #12486
- [Fix-12451][k8s] Read the kubeconfig from cluster conf (#12452)
- [Fix-12356][k8s] fix the null exception when submitting k8s task plugin (#12358)
- [fix#12439] [Alert] fix send script alert NPE #12495
- [Bug] [API] The workflow definition and the tenant in the workflow instance are inconsistent. (#12533)
- [fix](dolphinscheduler-dao) fix upgrade to 3.1.0 sql missing field (#12314) (#12315)
- [Fix-12425][api] Add rollbackFor setting.
- [Bug-12410] [API]Fix the worker list result in workflow definition only has default
- [BUG-12306][ui]Fix the password item always is disabled (#12437)
- [Fix][task] Fix dependent task can not predicate the status of the corresponding task correctly (#12253)
- make sure all failed task will save in errorTaskMap (#12424)
- Fix timing scheduling trigger master service report to get command parameter null pointer exception (#12419)
- [Bug] [spark-sql] In spark-sql, select both SPARK1 and SPARK2 versions and execute ${SPARK_HOME2}/bin/spark-sql (#11721) (#12420)
- fix hdfs defaultFs not working (#11823) (#12418)
- source is not available in sh (#12413)
- fix datax NPE issue (#12388) (#12407)
- [BUG-12396][schedule] Fixed that the workflow definition scheduling that has been online after the version upgrade does not execute

## Doc

- [doc] Correct descriptions in glossary.md (#12282)
- [Hotfix][docs] Fix 404 dead link
- update english oracle.md (#12332)

## Improvement

- adjust the args of router in the dag (#12759)
- Change command file permission to 755 (#12678)
- beautify the dag (#12728)
- support to use the clearable button of components to search (#12668)
- Add worker-group-refresh-interval in master config #12601
- [Improvement][ui] Support to view the process variables on the page of DAG. (#12609)
- [Improvement] Merge spi.utils into common.utils (#12607)
- Add task executor threads full policy config in worker (#12510)
- Add mysql support to helm chart (#12517)
- Reorganize some classes in common module, remove duplicate classes #12321
- [DS-12131][master] Optimize the log printing of the master module acc… (#12152)
- [Improvement][workergroup]Remove workerGroup in registry #12217
- [Improvement] remove log-server and server module #12206
- Refactor LogServiceClient Singleton to avoid repeat creation of NettyClient #11777
- [Improvement] Add remote task model #11767 (#12541)
- Use temurin Java docker images instead of deprecated ones (#12334)
- [DS-12154][worker] Optimize the log printing of the worker module (#12183)
- [Improvement-12372][k8s] Update the deprecated k8s api (#12373)
- [Improvement][task plugin] Modify the comment of 'deployMode'. (#12163)
- Remove the DataxTaskTest class because there is no junit5 package.
- [Improvement][api] When the workflow definition is copied, the operation user of the timed instance is changed to the current user
- [Improvement-12391][api] Workflow definitions that contain logical task nodes support the copy function
