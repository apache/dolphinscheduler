# Change Log

## Bugfix

- Fix default value for resource center in k8s mode (#12906)
- Fix send script alert NPE (#12495) (#12895)
- Fix procedure task output param (#12894)
- Fix memory leak in worker due to message retry map (#12878) (#12887)
- Fix flink sql cannot run due to missing main jar (#12705) (#12800)
- Fix sql error (#12717) (#12799)
- If the worker's clock early than master node, will ignore dispatch time (#12219) (#12798)
- Fix dependent task can not predicate the status of the corresponding task correctly (#12253) (#12792)
- Fix python class description error (#12790)
- Fix the duration in Workflow Instance page. (#12264) (#12788)
- Fix error problem on h2 startup data quality rule management page (#12108) (#12781)
- Fix hive datasource connection leak (#12226) (#12777)
- Fix constructing processInstance may NPE when master handling command (#12056) (#12776)
- Fix the bugs when upgrading ds from v1.3.9 to v3.0.0 ,such as file cannot be found or column not found etc (#11619) (#12770)
- Fix the bug which some scheduled tasks are not triggered on time (#12233) (#12767)
- Fix alert sending error (#11774) (#12762)
- Fix IPv4 Pattern (#11762) (#12739)
- Add unique key to process_definition_log avoid TooManyResultExpection (#12503) (#12670)
- Fix datax task data instance replay error
- Fix wrong env var name for alert in K8S (#12369) (#12635)
- Fix when the sql query result is empty, the email fails to send the attachment, and an exception will be reported (#12059) (#12633)

## Document

- Add new alert doc Slack (#12567) (#12797)
- Update the readme content (#12500) (#12796)
- Add http header to avoid github 403 in dlc (#12509) (#12793)
- Change optional parameters --jar to --jars for Spark (#12385) (#12386) (#12791)
- Change release process base on new tool (#12324) (#12783)
- Update ZooKeeper minimum version requirements (#12284) (#12288) (#12769)
- Update slack invitation link (#12258) (#12754)
- Correct release export key step (#12228) (#12751)
- Polish docs for standalone deployment (#12181) (#12743)
- Do not change docsdev.js during releasing (#12151) (#12740)
- Fix link errors in release documentation
- Fix list param error when use sql task (#11285) (#12632)
- Fix the API usage of gantt graph (#11642) (#12631)
- Fix task switch branch not show in webui (#12120)

## Improvement

- Script cannot contains ''' in params (#12067) (#12913)
- Add configmap resource permissions so config hot reload can work (#12572) (#12795)
- Only expose necessary actuator endpoints (#12571) (#12794)
- Correct spelling errors (#12174) (#12780)
- Avoid using search in for and start using testSaveTaskDefine (#11383) (#12773)
- Automatically convert resource storage type to upper case (#12281) (#12766)
- Delete useless exclusions (#12273) (#12761)
- Add chmod command after unzip DS tar.gz (#12752)
- Set the `required` to be reactive in the task modal. (#12225) (#12748)
- Improvement the error message when batch delete workflow (#11682) (#12747)
- Validate before deleting workflow or task used by other tasks (#10873) (#12731)
- Fix vulnerability in LDAP login (#11586) (#12730)
- Easier release: cherry-pick, changelog, contributor (#11478) (#12634)
