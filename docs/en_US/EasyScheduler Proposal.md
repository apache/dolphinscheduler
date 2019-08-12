# EasyScheduler Proposal

## Abstract

EasyScheduler is a distributed visual workflow scheduling system, which focuses on solving the problem of "complex  task dependencies"  in data processing. just like its name, we dedicated to making the scheduling system `out of the box` . 

## Proposal

EasyScheduler provides many easy-to-use features to simplify the use of data processing workflow，We propose the clear concept of "instance of process" and "instance of task"  to make it more convenient  to get the running state of workflow every time.  its main objectives are as follows:

- Associate the tasks according to the dependencies of the tasks in a DAG graph, which can visualize the running state of task in real time.
- Support for many task types: Shell, MR, Spark, SQL (mysql, postgresql, hive, sparksql), Python, Sub_Process, Procedure, etc.
- Support process scheduling, dependency scheduling, manual scheduling, manual pause/stop/recovery, support  failed retry/alarm, recovery from specified nodes, kill task, etc.
- Support process priority, task priority and task failover and task timeout alarm/failure
- Support process global parameters and node custom parameter settings
- Support online upload/download of resource files, management, etc. Support online file creation and editing
- Support task log online viewing and scrolling, online download log, etc.
- Implement cluster HA, decentralize Master cluster and Worker cluster through Zookeeper
- Support online viewing of `Master/Worker` cpu load, memory
- Support process running history tree/gantt chart display, support task status statistics, process status statistics
- Support backfilling data
- Support multi-tenant
- Easy to maintain 

for now, EasyScheduler has a fairly huge community in China. 
It is also widely adopted by many [companies and organizations](https://github.com/analysys/EasyScheduler/issues/57) as an ETL scheduling tool. 

We believe that bringing EasyScheduler into ASF could advance development of a much more stronger and more diverse open source community.

Analysys submits this proposal to donate EasyScheduler's source codes and all related documentations to Apache Software Foundation. 
The codes are already under Apache License Version 2.0.

- Code base: https://www.github.com/analysys/easyscheduler
- English Documentations: <https://analysys.github.io/easyscheduler_docs>
- Chinese Documentations: <https://analysys.github.io/easyscheduler_docs_cn>

## Background

We want to find a data processing tool with the following features:

- easy to use，It can be assembled into a process with a very simple drag and drop operation. not only for developers，people who can't write code also can use
- solving the problem of "complex  task dependencies" , and can monitor the running status 
- support multi-tenant
- support  many task types: Shell, MR, Spark, SQL (mysql, postgresql, hive, sparksql), Python, Sub_Process, Procedure, etc.
- linear scalability

For the above reasons, we realized that no existing product met our exact requirements externally, so we decided to develop it ourselves.EasyScheduler completed the architecture design at the end of 2017. The first internal use version was completed in May 2018. We then iterated several versions and the system gradually stabilized. 

EasyScheduler won the `GVP` (Gitee Most Valuable Project) in April 2019

## Rationale

Many organizations (>30) (refer to [Who is using EasyScheduler](https://github.com/analysys/EasyScheduler/issues/57) ) already benefit from running EasyScheduler to make data process pipelines more easier. More than 100  [feature ideas](https://github.com/analysys/EasyScheduler/projects/1) come from EasyScheduler community.   Some 3th projects also wanted to integrate with EasyScheduler through task plugin, like [Scriptis](https://github.com/WeBankFinTech/Scriptis) 、[waterdrop](https://github.com/InterestingLab/waterdrop) and so on. these will strengthen the features of EasyScheduler.   

## Current Status

### Meritocracy

EasyScheduler was incubated at Analysys in 2017 and open sourced on GitHub in March 2019.  Once open source,We have been quickly adopted by multiple organizations，EasyScheduler has contributors and users from many companies; we have set up the PMC Team and Committer Team. New contributors are guided and reviewed by existed PMC members. 
When they are ready, PMC will start a vote to promote him/her to become a member of PMC or Committer Team. 
Contributions are always welcomed and highly valued. 

### Community

Now we have set development teams for EasyScheduler  in Analysys, and we already have external developers who contributed the code.  We already have a user group of more than 1,000 people. 
We hope to grow the base of contributors by inviting all those who offer contributions through The Apache Way. 
Right now, we make use of github as code hosting as well as gitter for community communication.

### Core Developers

The core developers, including experienced open source developers and team leaders, have formed a group full of diversity. All of these core developers have deep expertise in workflow processing and the Hadoop Ecosystem in general.

## Known Risks

### Orphaned products

EasyScheduler is widely adopted in China by many [companies and organizations](https://github.com/analysys/EasyScheduler/issues/57). The core developers of EasyScheduler team plan to work full time on this project. Currently there are 10 use cases with more that  1000 activity tasks per day using EasyScheduler in the user's production environment. Furthermore, since EasyScheduler has received more than 1500 stars and been forked more than 500 times. EasyScheduler has eight major release so far and and received 365 pull requests from contributors, which further demonstrates EasyScheduler as a very active project. We plan to extend and diversify this community further through Apache.

Thus, it is very unlikely that EasyScheduler becomes orphaned.

### Inexperience with Open Source

The core developers are all active users and followers of open source. They are already committers and contributors to the EasyScheduler Github project. All have been involved with the source code that has been released under an open source license, and several of them also have experience developing code in an open source environment, they are also active in presto, alluxio and other projects.

Therefore, we believe we have enough experience to deal with open source.

### Homogenous Developers

The current developers work across a variety of organizations including Analysys, guandata and hydee; 
some individual developers are accepted as developers of EasyScheduler as well. 
Considering that fengjr and sefonsoft have shown great interest in EasyScheduler, we plan to encourage them to contribute and invite them as contributors to work together.

### Reliance on Salaried Developers

At present, four of the core developers are paid by their employer to contribute to EasyScheduler project. 
we also find some developers and researchers (>8) to contribute to the project, and we will make efforts to  increase the diversity of the contributors and actively lobby for Domain experts in the workflow space to contribute. 

### Relationships with Other Apache Products

EasyScheduler integrates Apache Zookeeper as one of the service registration/discovery mechanisms. EasyScheduler is deeply integrated with Apache products. It currently support many task types like  Apache Hive, Apache Spark, Apache Hadoop, and so on

### A Excessive Fascination with the Apache Brand

We recognize the value and reputation that the Apache brand will bring to EasyScheduler.
However, we prefer that the community provided by the Apache Software Foundation will enable the project to achieve long-term stable development. so EasyScheduler is proposing to enter incubation at Apache in order to help efforts to diversify the community, not so much to capitalize on the Apache brand.

## Documentation

A complete set of Sharding-Sphere documentations is provided on shardingsphere.io in both English and Simplified Chinese.

- [English](https://analysys.github.io/easyscheduler_docs)
- [Chinese](https://analysys.github.io/easyscheduler_docs_cn)

## Initial Source

The project consists of three distinct codebases: core and document. The address of two existed git repositories are as follows:

- <https://github.com/analysys/easyscheduler>

- <https://github.com/analysys/easyscheduler_docs> 

- <https://github.com/analysys/easyscheduler_docs_cn> 
  

## Source and Intellectual Property Submission Plan

As soon as EasyScheduler is approved to join Apache Incubator, Analysys will execute a Software Grant Agreement and the source code will be transitioned onto ASF infrastructure. The code is already licensed under the Apache Software License, version 2.0. 

## External Dependencies

As all backend code dependencies are managed using Apache Maven, none of the external libraries need to be packaged in a source distribution. 

most of dependencies have Apache compatible licenses，and the detail as follows:

### Backend Dependency

| Dependency                                             | License                                                      | Comments      |
| ------------------------------------------------------ | ------------------------------------------------------------ | ------------- |
| activation-1.1.jar                                     | CDDL v1.0                                                    |               |
| adal4j-1.0.0.jar                                       | Apache V2.0                                                  |               |
| ant-1.6.5.jar                                          | Apache V2.0                                                  |               |
| antlr-2.7.7.jar                                        | BSD                                                          |               |
| antlr-runtime-3.4.jar                                  | BSD License                                                  |               |
| aopalliance-1.0.jar                                    | Public Domain                                                |               |
| apacheds-i18n-2.0.0-M15.jar                            | Apache V2.0                                                  |               |
| apacheds-kerberos-codec-2.0.0-M15.jar                  | Apache V2.0                                                  |               |
| apache-el-8.5.35.1.jar                                 | Apache V2.0                                                  |               |
| api-asn1-api-1.0.0-M20.jar                             | Apache V2.0                                                  |               |
| api-util-1.0.0-M20.jar                                 | Apache V2.0                                                  |               |
| asm-3.1.jar                                            | BSD License                                                  |               |
| aspectjweaver-1.9.2.jar                                | Eclipse Public   License - v 1.0                             |               |
| aws-java-sdk-1.7.4.jar                                 | Apache V2.0                                                  |               |
| azure-core-0.9.3.jar                                   | Apache V2.0                                                  |               |
| azure-keyvault-0.9.3.jar                               | Apache V2.0                                                  |               |
| bcprov-jdk15on-1.51.jar                                | Bouncy Castle Licence                                        |               |
| bonecp-0.8.0.RELEASE.jar                               | Apache v2                                                    |               |
| byte-buddy-1.9.10.jar                                  | Apache V2.0                                                  |               |
| c3p0-0.9.1.1.jar                                       | GNU LESSER GENERAL   PUBLIC LICENSE                          | will   remove |
| classmate-1.4.0.jar                                    | Apache V2.0                                                  |               |
| clickhouse-jdbc-0.1.52.jar                             | Apache V2.0                                                  |               |
| commons-cli-1.2.jar                                    | Apache V2.0                                                  |               |
| commons-codec-1.6.jar                                  | Apache V2.0                                                  |               |
| commons-collections-3.2.2.jar                          | Apache V2.0                                                  |               |
| commons-collections4-4.1.jar                           | Apache V2.0                                                  |               |
| commons-compress-1.4.1.jar                             | Apache V2.0                                                  |               |
| commons-configuration-1.10.jar                         | Apache V2.0                                                  |               |
| commons-daemon-1.0.13.jar                              | Apache V2.0                                                  |               |
| commons-dbcp-1.4.jar                                   | Apache V2.0                                                  |               |
| commons-email-1.2.jar                                  | Apache V2.0                                                  |               |
| commons-httpclient-3.0.1.jar                           | Apache License                                               |               |
| commons-io-2.4.jar                                     | Apache V2.0                                                  |               |
| commons-lang-2.3.jar                                   | Apache V2.0                                                  |               |
| commons-lang3-3.5.jar                                  | Apache V2.0                                                  |               |
| commons-logging-1.1.1.jar                              | Apache V2.0                                                  |               |
| commons-math-2.2.jar                                   | Apache V2.0                                                  |               |
| commons-pool-1.6.jar                                   | Apache V2.0                                                  |               |
| cron-utils-5.0.5.jar                                   | Apache V2.0                                                  |               |
| curator-client-2.12.0.jar                              | Apache V2.0                                                  |               |
| curator-framework-2.12.0.jar                           | Apache V2.0                                                  |               |
| curator-recipes-2.12.0.jar                             | Apache V2.0                                                  |               |
| datanucleus-api-jdo-4.2.1.jar                          | Apache V2.0                                                  |               |
| datanucleus-core-4.1.6.jar                             | Apache V2.0                                                  |               |
| datanucleus-rdbms-4.1.7.jar                            | Apache V2.0                                                  |               |
| derby-10.14.2.0.jar                                    | Apache V2.0                                                  |               |
| disruptor-3.3.0.jar                                    | Apache V2.0                                                  |               |
| druid-1.1.14.jar                                       | Apache V2.0                                                  |               |
| error_prone_annotations-2.1.2.jar                      | Apache V2.0                                                  |               |
| fastjson-1.2.29.jar                                    | Apache V2.0                                                  |               |
| fastutil-6.5.6.jar                                     | Apache V2.0                                                  |               |
| findbugs-annotations-1.3.9-1.jar                       | Apache V2.0                                                  |               |
| freemarker-2.3.21.jar                                  | Apache V2.0                                                  |               |
| grpc-context-1.9.0.jar                                 | Apache V2.0                                                  |               |
| grpc-core-1.9.0.jar                                    | Apache V2.0                                                  |               |
| grpc-netty-1.9.0.jar                                   | Apache V2.0                                                  |               |
| grpc-protobuf-1.9.0.jar                                | Apache V2.0                                                  |               |
| grpc-protobuf-lite-1.9.0.jar                           | Apache V2.0                                                  |               |
| grpc-stub-1.9.0.jar                                    | Apache V2.0                                                  |               |
| gson-2.8.5.jar                                         | Apache V2.0                                                  |               |
| guava-20.0.jar                                         | Apache V2.0                                                  |               |
| guice-3.0.jar                                          | Apache V2.0                                                  |               |
| guice-assistedinject-3.0.jar                           | Apache V2.0                                                  |               |
| guice-servlet-3.0.jar                                  | Apache V2.0                                                  |               |
| hadoop-annotations-2.7.3.jar                           | Apache V2.0                                                  |               |
| hadoop-auth-2.7.3.jar                                  | Apache V2.0                                                  |               |
| hadoop-aws-2.7.3.jar                                   | Apache V2.0                                                  |               |
| hadoop-client-2.7.3.jar                                | Apache V2.0                                                  |               |
| hadoop-common-2.7.3.jar                                | Apache V2.0                                                  |               |
| hadoop-hdfs-2.7.3.jar                                  | Apache V2.0                                                  |               |
| hadoop-mapreduce-client-app-2.7.3.jar                  | Apache V2.0                                                  |               |
| hadoop-mapreduce-client-common-2.7.3.jar               | Apache V2.0                                                  |               |
| hadoop-mapreduce-client-core-2.7.3.jar                 | Apache V2.0                                                  |               |
| hadoop-mapreduce-client-jobclient-2.7.3.jar            | Apache V2.0                                                  |               |
| hadoop-yarn-api-2.7.3.jar                              | Apache V2.0                                                  |               |
| hadoop-yarn-client-2.7.3.jar                           | Apache V2.0                                                  |               |
| hadoop-yarn-common-2.7.3.jar                           | Apache V2.0                                                  |               |
| hadoop-yarn-registry-2.7.1.jar                         | Apache V2.0                                                  |               |
| hadoop-yarn-server-applicationhistoryservice-2.6.0.jar | Apache V2.0                                                  |               |
| hadoop-yarn-server-common-2.7.3.jar                    | Apache V2.0                                                  |               |
| hadoop-yarn-server-resourcemanager-2.6.0.jar           | Apache V2.0                                                  |               |
| hadoop-yarn-server-web-proxy-2.6.0.jar                 | Apache V2.0                                                  |               |
| hbase-annotations-1.1.1.jar                            | Apache V2.0                                                  |               |
| hbase-client-1.1.1.jar                                 | Apache V2.0                                                  |               |
| hbase-common-1.1.1.jar                                 | Apache V2.0                                                  |               |
| hbase-common-1.1.1-tests.jar                           | Apache License   Version 2.0                                 |               |
| hbase-hadoop2-compat-1.1.1.jar                         | Apache V2.0                                                  |               |
| hbase-hadoop-compat-1.1.1.jar                          | Apache V2.0                                                  |               |
| hbase-prefix-tree-1.1.1.jar                            | Apache V2.0                                                  |               |
| hbase-procedure-1.1.1.jar                              | Apache V2.0                                                  |               |
| hbase-protocol-1.1.1.jar                               | Apache V2.0                                                  |               |
| hbase-server-1.1.1.jar                                 | Apache V2.0                                                  |               |
| hibernate-validator-6.0.14.Final.jar                   | Apache License 2.0                                           |               |
| HikariCP-3.2.0.jar                                     | Apache V2.0                                                  |               |
| hive-common-2.1.0.jar                                  | Apache V2.0                                                  |               |
| hive-jdbc-2.1.0.jar                                    | Apache V2.0                                                  |               |
| hive-llap-client-2.1.0.jar                             | Apache V2.0                                                  |               |
| hive-llap-common-2.1.0.jar                             | Apache V2.0                                                  |               |
| hive-llap-server-2.1.0.jar                             | Apache V2.0                                                  |               |
| hive-llap-tez-2.1.0.jar                                | Apache V2.0                                                  |               |
| hive-metastore-2.1.0.jar                               | Apache V2.0                                                  |               |
| hive-orc-2.1.0.jar                                     | Apache V2.0                                                  |               |
| hive-serde-2.1.0.jar                                   | Apache V2.0                                                  |               |
| hive-service-2.1.0.jar                                 | Apache V2.0                                                  |               |
| hive-service-rpc-2.1.0.jar                             | Apache V2.0                                                  |               |
| hive-shims-0.23-2.1.0.jar                              | Apache V2.0                                                  |               |
| hive-shims-2.1.0.jar                                   | Apache V2.0                                                  |               |
| hive-shims-common-2.1.0.jar                            | Apache V2.0                                                  |               |
| hive-shims-scheduler-2.1.0.jar                         | Apache V2.0                                                  |               |
| hive-storage-api-2.1.0.jar                             | Apache V2.0                                                  |               |
| htrace-core-3.1.0-incubating.jar                       | Apache V2.0                                                  |               |
| httpclient-4.4.1.jar                                   | Apache V2.0                                                  |               |
| httpcore-4.4.1.jar                                     | Apache V2.0                                                  |               |
| httpmime-4.5.7.jar                                     | Apache V2.0                                                  |               |
| instrumentation-api-0.4.3.jar                          | Apache V2.0                                                  |               |
| jackson-annotations-2.9.8.jar                          | Apache V2.0                                                  |               |
| jackson-core-2.9.8.jar                                 | Apache V2.0                                                  |               |
| jackson-core-asl-1.9.13.jar                            | Apache V2.0                                                  |               |
| jackson-databind-2.9.8.jar                             | Apache V2.0                                                  |               |
| jackson-datatype-jdk8-2.9.8.jar                        | Apache V2.0                                                  |               |
| jackson-datatype-jsr310-2.9.8.jar                      | Apache V2.0                                                  |               |
| jackson-jaxrs-1.8.3.jar                                | GNU   Lesser General Public License (LGPL), Version 2.1            Apache V2.0 | will   remove |
| jackson-mapper-asl-1.9.13.jar                          | Apache V2.0                                                  |               |
| jackson-module-parameter-names-2.9.8.jar               | Apache V2.0                                                  |               |
| jackson-xc-1.8.3.jar                                   | GNU   Lesser General Public License (LGPL), Version 2.1      Apache V2.0 | will   remove |
| jamon-runtime-2.3.1.jar                                | Mozilla Public   License Version 1.1                         | MPL1.1        |
| jasper-compiler-5.5.23.jar                             | Apache V2.0                                                  |               |
| javax.activation-api-1.2.0.jar                         | CDDL/GPLv2+CE                                                | will remove   |
| javax.annotation-api-1.3.2.jar                         | CDDL + GPLv2 with   classpath exception                      | will remove   |
| javax.inject-1.jar                                     | Apache V2.0                                                  |               |
| javax.jdo-3.2.0-m3.jar                                 | Apache V2.0                                                  |               |
| java-xmlbuilder-0.4.jar                                | Apache V2.0                                                  |               |
| javax.servlet-api-3.1.0.jar                            | CDDL + GPLv2 with   classpath exception                      | will   remove |
| javolution-5.5.1.jar                                   | BSD License                                                  |               |
| jaxb-*.jar                                             | (CDDL 1.1) (GPL2 w/   CPE)                                   | will   remove |
| jboss-logging-3.3.2.Final.jar                          | Apache V2.0                                                  |               |
| jcip-annotations-1.0.jar                               | Public                                                       |               |
| jcodings-1.0.8.jar                                     | MIT                                                          |               |
| jcommander-1.30.jar                                    | Apache V2.0                                                  |               |
| jdo-api-3.0.1.jar                                      | Apache V2.0                                                  |               |
| jersey-*-1.9.jar                                       | CDDL+GPLv2                                                   | will   remove |
| jets3t-0.9.0.jar                                       | Apache V2.0                                                  |               |
| jettison-1.1.jar                                       | Apache V2.0                                                  |               |
| jetty-6.1.26.jar                                       | Apache V2.0,EPL 1.0                                          |               |
| jetty-continuation-9.4.14.v20181114.jar                | Apache V2.0，EPL 1.0                                         |               |
| jetty-http-9.4.14.v20181114.jar                        | Apache V2.0，EPL 1.0                                         |               |
| jetty-io-9.4.14.v20181114.jar                          | Apache V2.0，EPL 1.0                                         |               |
| jetty-security-9.4.14.v20181114.jar                    | Apache V2.0，EPL 1.0                                         |               |
| jetty-server-9.4.14.v20181114.jar                      | Apache V2.0，EPL 1.0                                         |               |
| jetty-servlet-9.4.14.v20181114.jar                     | Apache V2.0，EPL 1.0                                         |               |
| jetty-servlets-9.4.14.v20181114.jar                    | Apache V2.0，EPL 1.0                                         |               |
| jetty-sslengine-6.1.26.jar                             | Apache V2.0                                                  |               |
| jetty-util-6.1.26.jar                                  | Apache V2.0，EPL 1.0                                         |               |
| jetty-util-9.4.14.v20181114.jar                        | Apache V2.0，EPL 1.0                                         |               |
| jetty-webapp-9.4.14.v20181114.jar                      | Apache V2.0，EPL 1.0                                         |               |
| jetty-xml-9.4.14.v20181114.jar                         | Apache V2.0，EPL 1.0                                         |               |
| jline-0.9.94.jar                                       | BSD                                                          |               |
| jna-4.5.2.jar                                          | Apache V2.0，LGPL 2.1                                        | will   remove |
| jna-platform-4.5.2.jar                                 | Apache V2.0，LGPL 2.1                                        | will   remove |
| joda-time-2.10.1.jar                                   | Apache V2.0                                                  |               |
| joni-2.1.2.jar                                         | MIT                                                          |               |
| jpam-1.1.jar                                           | Apache V2.0                                                  |               |
| jsch-0.1.42.jar                                        | BSD                                                          |               |
| json-smart-1.1.1.jar                                   | Apache V2.0                                                  |               |
| json-smart-2.3.jar                                     | Apache V2.0                                                  |               |
| jsp-2.1-6.1.14.jar                                     | CDDL 1.0                                                     |               |
| jsp-api-2.x.jar                                        | CDDL，GPL 2.0                                                | will   remove |
| jsp-api-2.1-6.1.14.jar                                 | Apache V2.0                                                  |               |
| jsr305-3.0.0.jar                                       | Apache V2.0                                                  |               |
| jta-1.1.jar                                            | CDDL 1.0                                                     |               |
| jul-to-slf4j-1.7.25.jar                                | MIT                                                          |               |
| lang-tag-1.4.jar                                       | Apache V2.0                                                  |               |
| leveldbjni-all-1.8.jar                                 | BSD 3-clause                                                 |               |
| libfb303-0.9.3.jar                                     | Apache V2.0                                                  |               |
| libthrift-0.9.3.jar                                    | Apache V2.0                                                  |               |
| log4j-1.2.17.jar                                       | Apache V2.0                                                  |               |
| log4j-1.2-api-2.11.2.jar                               | Apache V2.0                                                  |               |
| log4j-api-2.11.2.jar                                   | Apache V2.0                                                  |               |
| log4j-core-2.11.2.jar                                  | Apache V2.0                                                  |               |
| log4j-web-2.11.2.jar                                   | Apache V2.0                                                  |               |
| logback-x.jar                                          | dual-license      EPL 1.0,LGPL 2.1                           |               |
| lz4-1.3.0.jar                                          | Apache V2.0                                                  |               |
| mail-1.4.5.jar                                         | CDDL+GPLv2                                                   | will   remove |
| mapstruct-1.2.0.Final.jar                              | Apache V2.0                                                  |               |
| metrics-core-2.2.0.jar                                 | Apache V2.0                                                  |               |
| mssql-jdbc-6.1.0.jre8.jar                              | MIT                                                          |               |
| mybatis-3.5.1.jar                                      | Apache V2.0                                                  |               |
| mybatis-spring-2.0.1.jar                               | Apache V2.0                                                  |               |
| mybatis-spring-boot-autoconfigure-2.0.1.jar            | Apache V2.0                                                  |               |
| mybatis-spring-boot-starter-2.0.1.jar                  | Apache V2.0                                                  |               |
| mysql-connector-java-5.1.34.jar                        | GPL 2.0                                                      | will   remove |
| netty-buffer-4.1.33.Final.jar                          | Apache V2.0                                                  |               |
| netty-codec-4.1.33.Final.jar                           | Apache V2.0                                                  |               |
| netty-codec-http2-4.1.33.Final.jar                     | Apache V2.0                                                  |               |
| netty-codec-http-4.1.33.Final.jar                      | Apache V2.0                                                  |               |
| netty-codec-socks-4.1.33.Final.jar                     | Apache V2.0                                                  |               |
| netty-common-4.1.33.Final.jar                          | Apache V2.0                                                  |               |
| netty-handler-4.1.33.Final.jar                         | Apache V2.0                                                  |               |
| netty-handler-proxy-4.1.33.Final.jar                   | Apache V2.0                                                  |               |
| netty-resolver-4.1.33.Final.jar                        | Apache V2.0                                                  |               |
| netty-transport-4.1.33.Final.jar                       | Apache V2.0                                                  |               |
| nimbus-jose-jwt-3.1.2.jar                              | Apache V2.0                                                  |               |
| oauth2-oidc-sdk-4.5.jar                                | Apache V2.0                                                  |               |
| opencensus-api-0.10.0.jar                              | Apache V2.0                                                  |               |
| opencensus-contrib-grpc-metrics-0.10.0.jar             | Apache V2.0                                                  |               |
| opencsv-2.3.jar                                        | Apache V2.0                                                  |               |
| oshi-core-3.5.0.jar                                    | EPL 1.0                                                      |               |
| parquet-hadoop-bundle-1.8.1.jar                        | Apache V2.0                                                  |               |
| poi-3.17.jar                                           | Apache V2.0                                                  |               |
| postgresql-42.1.4.jar                                  | BSD 2-clause                                                 |               |
| protobuf-java-3.5.1.jar                                | BSD 3-clause                                                 |               |
| protobuf-java-util-3.5.1.jar                           | BSD 3-clause                                                 |               |
| proto-google-common-protos-1.0.0.jar                   | Apache V2.0                                                  |               |
| quartz-2.2.3.jar                                       | Apache V2.0                                                  |               |
| quartz-jobs-2.2.3.jar                                  | Apache V2.0                                                  |               |
| slf4j-api-1.7.5.jar                                    | MIT                                                          |               |
| slider-core-0.90.2-incubating.jar                      | Apache V2.0                                                  |               |
| snakeyaml-1.23.jar                                     | Apache V2.0                                                  |               |
| snappy-0.2.jar                                         | Apache V2.0                                                  |               |
| spring-aop-5.1.5.RELEASE.jar                           | Apache V2.0                                                  |               |
| spring-beans-5.1.5.RELEASE.jar                         | Apache V2.0                                                  |               |
| spring-boot-2.1.3.RELEASE.jar                          | Apache V2.0                                                  |               |
| spring-boot-autoconfigure-2.1.3.RELEASE.jar            | Apache V2.0                                                  |               |
| spring-boot-starter-2.1.3.RELEASE.jar                  | Apache V2.0                                                  |               |
| spring-boot-starter-aop-2.1.3.RELEASE.jar              | Apache V2.0                                                  |               |
| spring-boot-starter-jdbc-2.1.3.RELEASE.jar             | Apache V2.0                                                  |               |
| spring-boot-starter-jetty-2.1.3.RELEASE.jar            | Apache V2.0                                                  |               |
| spring-boot-starter-json-2.1.3.RELEASE.jar             | Apache V2.0                                                  |               |
| spring-boot-starter-logging-2.1.3.RELEASE.jar          | Apache V2.0                                                  |               |
| spring-boot-starter-web-2.1.3.RELEASE.jar              | Apache V2.0                                                  |               |
| spring-context-5.1.5.RELEASE.jar                       | Apache V2.0                                                  |               |
| spring-core-5.1.5.RELEASE.jar                          | Apache V2.0                                                  |               |
| spring-expression-5.1.5.RELEASE.jar                    | Apache V2.0                                                  |               |
| springfox-core-2.9.2.jar                               | Apache V2.0                                                  |               |
| springfox-schema-2.9.2.jar                             | Apache V2.0                                                  |               |
| springfox-spi-2.9.2.jar                                | Apache V2.0                                                  |               |
| springfox-spring-web-2.9.2.jar                         | Apache V2.0                                                  |               |
| springfox-swagger2-2.9.2.jar                           | Apache V2.0                                                  |               |
| springfox-swagger-common-2.9.2.jar                     | Apache V2.0                                                  |               |
| springfox-swagger-ui-2.9.2.jar                         | Apache V2.0                                                  |               |
| spring-jcl-5.1.5.RELEASE.jar                           | Apache V2.0                                                  |               |
| spring-jdbc-5.1.5.RELEASE.jar                          | Apache V2.0                                                  |               |
| spring-plugin-core-1.2.0.RELEASE.jar                   | Apache V2.0                                                  |               |
| spring-plugin-metadata-1.2.0.RELEASE.jar               | Apache V2.0                                                  |               |
| spring-tx-5.1.5.RELEASE.jar                            | Apache V2.0                                                  |               |
| spring-web-5.1.5.RELEASE.jar                           | Apache V2.0                                                  |               |
| spring-webmvc-5.1.5.RELEASE.jar                        | Apache V2.0                                                  |               |
| stringtemplate-3.2.1.jar                               | BSD                                                          |               |
| swagger-annotations-1.5.20.jar                         | Apache V2.0                                                  |               |
| swagger-bootstrap-ui-1.9.3.jar                         | Apache V2.0                                                  |               |
| swagger-models-1.5.20.jar                              | Apache V2.0                                                  |               |
| tephra-api-0.6.0.jar                                   | Apache V2.0                                                  |               |
| tephra-core-0.6.0.jar                                  | Apache V2.0                                                  |               |
| tephra-hbase-compat-1.0-0.6.0.jar                      | Apache V2.0                                                  |               |
| threetenbp-1.3.6.jar                                   | BSD 3-clause                                                 |               |
| transaction-api-1.1.jar                                | CDDL1.0                                                      |               |
| twill-api-0.6.0-incubating.jar                         | Apache V2.0                                                  |               |
| twill-common-0.6.0-incubating.jar                      | Apache V2.0                                                  |               |
| twill-core-0.6.0-incubating.jar                        | Apache V2.0                                                  |               |
| twill-discovery-api-0.6.0-incubating.jar               | Apache V2.0                                                  |               |
| twill-discovery-core-0.6.0-incubating.jar              | Apache V2.0                                                  |               |
| twill-zookeeper-0.6.0-incubating.jar                   | Apache V2.0                                                  |               |
| validation-api-2.0.1.Final.jar                         | Apache V2.0                                                  |               |
| xercesImpl-2.9.1.jar                                   | Apache V2.0                                                  |               |
| xml-apis-1.4.01.jar                                    | Apache V2.0,W3C                                              |               |
| xz-1.0.jar                                             | Public                                                       |               |
| zookeeper-3.4.8.jar                                    | Apache                                                       |               |







The front-end UI currently relies on many components, which we will list separately at the end of the file.

### UI Dependency

| Dependency                                              | License                              | Comments    |
| ------------------------------------------------------- | ------------------------------------ | ----------- |
| abab                                                    | ISC                                  |             |
| abbrev                                                  | ISC                                  |             |
| accepts                                                 | MIT                                  |             |
| acorn                                                   | MIT                                  |             |
| acorn-dynamic-import                                    | MIT                                  |             |
| acorn-globals                                           | MIT                                  |             |
| after                                                   | MIT                                  |             |
| agent-base                                              | MIT                                  |             |
| ajv                                                     | MIT                                  |             |
| ajv-keywords                                            | MIT                                  |             |
| align-text                                              | MIT                                  |             |
| alphanum-sort                                           | MIT                                  |             |
| amdefine                                                | BSD-3-Clause OR MIT                  |             |
| ansi-html                                               | Apache-2.0                           |             |
| ansi-regex                                              | MIT                                  |             |
| ansi-styles                                             | MIT                                  |             |
| anymatch                                                | ISC                                  |             |
| aproba                                                  | ISC                                  |             |
| are-we-there-yet                                        | ISC                                  |             |
| argparse                                                | MIT                                  |             |
| arr-diff                                                | MIT                                  |             |
| arr-flatten                                             | MIT                                  |             |
| arr-union                                               | MIT                                  |             |
| array-equal                                             | MIT                                  |             |
| array-find-index                                        | MIT                                  |             |
| array-flatten                                           | MIT                                  |             |
| array-includes                                          | MIT                                  |             |
| array-slice                                             | MIT                                  |             |
| array-union                                             | MIT                                  |             |
| array-uniq                                              | MIT                                  |             |
| array-unique                                            | MIT                                  |             |
| arraybuffer.slice                                       | MIT                                  |             |
| arrify                                                  | MIT                                  |             |
| asn1                                                    | MIT                                  |             |
| asn1.js                                                 | MIT                                  |             |
| assert                                                  | MIT                                  |             |
| assert-plus                                             | MIT                                  |             |
| assign-symbols                                          | MIT                                  |             |
| ast-types                                               | MIT                                  |             |
| async                                                   | MIT                                  |             |
| async-each                                              | MIT                                  |             |
| async-foreach                                           | MIT                                  |             |
| async-limiter                                           | MIT                                  |             |
| asynckit                                                | MIT                                  |             |
| atob                                                    | (MIT OR Apache-2.0)                  |             |
| autoprefixer                                            | MIT                                  |             |
| aws-sign2                                               | Apache-2.0                           |             |
| aws4                                                    | MIT                                  |             |
| babel-code-frame                                        | MIT                                  |             |
| babel-core                                              | MIT                                  |             |
| babel-eslint                                            | MIT                                  |             |
| babel-generator                                         | MIT                                  |             |
| babel-helper-builder-binary-assignment-operator-visitor | MIT                                  |             |
| babel-helper-call-delegate                              | MIT                                  |             |
| babel-helper-define-map                                 | MIT                                  |             |
| babel-helper-explode-assignable-expression              | MIT                                  |             |
| babel-helper-function-name                              | MIT                                  |             |
| babel-helper-get-function-arity                         | MIT                                  |             |
| babel-helper-hoist-variables                            | MIT                                  |             |
| babel-helper-optimise-call-expression                   | MIT                                  |             |
| babel-helper-regex                                      | MIT                                  |             |
| babel-helper-remap-async-to-generator                   | MIT                                  |             |
| babel-helper-replace-supers                             | MIT                                  |             |
| babel-helper-vue-jsx-merge-props                        | MIT                                  |             |
| babel-helpers                                           | MIT                                  |             |
| babel-loader                                            | MIT                                  |             |
| babel-messages                                          | MIT                                  |             |
| babel-plugin-check-es2015-constants                     | MIT                                  |             |
| babel-plugin-syntax-async-functions                     | MIT                                  |             |
| babel-plugin-syntax-class-properties                    | MIT                                  |             |
| babel-plugin-syntax-dynamic-import                      | MIT                                  |             |
| babel-plugin-syntax-exponentiation-operator             | MIT                                  |             |
| babel-plugin-syntax-jsx                                 | MIT                                  |             |
| babel-plugin-syntax-object-rest-spread                  | MIT                                  |             |
| babel-plugin-syntax-trailing-function-commas            | MIT                                  |             |
| babel-plugin-transform-async-to-generator               | MIT                                  |             |
| babel-plugin-transform-class-properties                 | MIT                                  |             |
| babel-plugin-transform-es2015-arrow-functions           | MIT                                  |             |
| babel-plugin-transform-es2015-block-scoped-functions    | MIT                                  |             |
| babel-plugin-transform-es2015-block-scoping             | MIT                                  |             |
| babel-plugin-transform-es2015-classes                   | MIT                                  |             |
| babel-plugin-transform-es2015-computed-properties       | MIT                                  |             |
| babel-plugin-transform-es2015-destructuring             | MIT                                  |             |
| babel-plugin-transform-es2015-duplicate-keys            | MIT                                  |             |
| babel-plugin-transform-es2015-for-of                    | MIT                                  |             |
| babel-plugin-transform-es2015-function-name             | MIT                                  |             |
| babel-plugin-transform-es2015-literals                  | MIT                                  |             |
| babel-plugin-transform-es2015-modules-amd               | MIT                                  |             |
| babel-plugin-transform-es2015-modules-commonjs          | MIT                                  |             |
| babel-plugin-transform-es2015-modules-systemjs          | MIT                                  |             |
| babel-plugin-transform-es2015-modules-umd               | MIT                                  |             |
| babel-plugin-transform-es2015-object-super              | MIT                                  |             |
| babel-plugin-transform-es2015-parameters                | MIT                                  |             |
| babel-plugin-transform-es2015-shorthand-properties      | MIT                                  |             |
| babel-plugin-transform-es2015-spread                    | MIT                                  |             |
| babel-plugin-transform-es2015-sticky-regex              | MIT                                  |             |
| babel-plugin-transform-es2015-template-literals         | MIT                                  |             |
| babel-plugin-transform-es2015-typeof-symbol             | MIT                                  |             |
| babel-plugin-transform-es2015-unicode-regex             | MIT                                  |             |
| babel-plugin-transform-exponentiation-operator          | MIT                                  |             |
| babel-plugin-transform-object-rest-spread               | MIT                                  |             |
| babel-plugin-transform-regenerator                      | MIT                                  |             |
| babel-plugin-transform-remove-console                   | MIT                                  |             |
| babel-plugin-transform-runtime                          | MIT                                  |             |
| babel-plugin-transform-strict-mode                      | MIT                                  |             |
| babel-plugin-transform-vue-jsx                          | MIT                                  |             |
| babel-preset-env                                        | MIT                                  |             |
| babel-register                                          | MIT                                  |             |
| babel-runtime                                           | MIT                                  |             |
| babel-template                                          | MIT                                  |             |
| babel-traverse                                          | MIT                                  |             |
| babel-types                                             | MIT                                  |             |
| babylon                                                 | MIT                                  |             |
| backo2                                                  | MIT                                  |             |
| balanced-match                                          | MIT                                  |             |
| base                                                    | MIT                                  |             |
| base64-arraybuffer                                      | MIT                                  |             |
| base64-js                                               | MIT                                  |             |
| base64id                                                | MIT                                  |             |
| batch                                                   | MIT                                  |             |
| bcrypt-pbkdf                                            | BSD-3-Clause                         |             |
| better-assert                                           | MIT                                  |             |
| big.js                                                  | MIT                                  |             |
| binary-extensions                                       | MIT                                  |             |
| blob                                                    | MIT                                  |             |
| block-stream                                            | ISC                                  |             |
| bluebird                                                | MIT                                  |             |
| bn.js                                                   | MIT                                  |             |
| body-parser                                             | MIT                                  |             |
| bonjour                                                 | MIT                                  |             |
| boolbase                                                | ISC                                  |             |
| bootstrap                                               | MIT                                  |             |
| brace-expansion                                         | MIT                                  |             |
| braces                                                  | MIT                                  |             |
| brorand                                                 | MIT                                  |             |
| browserify-aes                                          | MIT                                  |             |
| browserify-cipher                                       | MIT                                  |             |
| browserify-des                                          | MIT                                  |             |
| browserify-rsa                                          | MIT                                  |             |
| browserify-sign                                         | ISC                                  |             |
| browserify-zlib                                         | MIT                                  |             |
| browserslist                                            | MIT                                  |             |
| browserstack                                            | MIT                                  |             |
| browserstack-local                                      | MIT                                  |             |
| buffer                                                  | MIT                                  |             |
| buffer-alloc                                            | MIT                                  |             |
| buffer-alloc-unsafe                                     | MIT                                  |             |
| buffer-fill                                             | MIT                                  |             |
| buffer-from                                             | MIT                                  |             |
| buffer-indexof                                          | MIT                                  |             |
| buffer-xor                                              | MIT                                  |             |
| builtin-status-codes                                    | MIT                                  |             |
| bytes                                                   | MIT                                  |             |
| cacache                                                 | ISC                                  |             |
| cache-base                                              | MIT                                  |             |
| call-me-maybe                                           | MIT                                  |             |
| caller-callsite                                         | MIT                                  |             |
| caller-path                                             | MIT                                  |             |
| callsite                                                | MIT                                  |             |
| callsites                                               | MIT                                  |             |
| camel-case                                              | MIT                                  |             |
| camelcase                                               | MIT                                  |             |
| camelcase-keys                                          | MIT                                  |             |
| caniuse-api                                             | MIT                                  |             |
| caniuse-db                                              | CC-BY-4.0                            |             |
| caniuse-lite                                            | CC-BY-4.0                            |             |
| canvg                                                   | MIT                                  |             |
| caseless                                                | Apache-2.0                           |             |
| center-align                                            | MIT                                  |             |
| chalk                                                   | MIT                                  |             |
| chokidar                                                | MIT                                  |             |
| chownr                                                  | ISC                                  |             |
| cipher-base                                             | MIT                                  |             |
| circular-json                                           | MIT                                  |             |
| clap                                                    | MIT                                  |             |
| class-utils                                             | MIT                                  |             |
| clean-css                                               | MIT                                  |             |
| clipboard                                               | MIT                                  |             |
| cliui                                                   | ISC                                  |             |
| clone                                                   | MIT                                  |             |
| clone-deep                                              | MIT                                  |             |
| co                                                      | MIT                                  |             |
| coa                                                     | MIT                                  |             |
| code-frame                                              | MIT                                  |             |
| code-point-at                                           | MIT                                  |             |
| codemirror                                              | MIT                                  |             |
| coffee                                                  | MIT                                  |             |
| coffee-requirejs                                        | MIT                                  |             |
| collection-visit                                        | MIT                                  |             |
| color                                                   | MIT                                  |             |
| color-convert                                           | MIT                                  |             |
| color-name                                              | MIT                                  |             |
| color-string                                            | MIT                                  |             |
| colormin                                                | MIT                                  |             |
| colors                                                  | MIT                                  |             |
| combine-lists                                           | MIT                                  |             |
| combined-stream                                         | MIT                                  |             |
| commander                                               | MIT                                  |             |
| commondir                                               | MIT                                  |             |
| component-bind                                          | MIT                                  |             |
| component-emitter                                       | MIT                                  |             |
| component-inherit                                       | MIT                                  |             |
| compressible                                            | MIT                                  |             |
| compression                                             | MIT                                  |             |
| concat-map                                              | MIT                                  |             |
| concat-stream                                           | MIT                                  |             |
| connect                                                 | MIT                                  |             |
| connect-history-api-fallback                            | MIT                                  |             |
| console-browserify                                      | MIT                                  |             |
| console-control-strings                                 | ISC                                  |             |
| consolidate                                             | MIT                                  |             |
| constants-browserify                                    | MIT                                  |             |
| content-disposition                                     | MIT                                  |             |
| content-type                                            | MIT                                  |             |
| convert-source-map                                      | MIT                                  |             |
| cookie                                                  | MIT                                  |             |
| cookie-signature                                        | MIT                                  |             |
| copy-concurrently                                       | ISC                                  |             |
| copy-descriptor                                         | MIT                                  |             |
| copy-webpack-plugin                                     | MIT                                  |             |
| core-js                                                 | MIT                                  |             |
| core-util-is                                            | MIT                                  |             |
| cosmiconfig                                             | MIT                                  |             |
| create-ecdh                                             | MIT                                  |             |
| create-hash                                             | MIT                                  |             |
| create-hmac                                             | MIT                                  |             |
| cross-env                                               | MIT                                  |             |
| cross-spawn                                             | MIT                                  |             |
| crypto-browserify                                       | MIT                                  |             |
| css-color-names                                         | MIT                                  |             |
| css-declaration-sorter                                  | MIT                                  |             |
| css-loader                                              | MIT                                  |             |
| css-select                                              | BSD-like                             |             |
| css-select-base-adapter                                 | MIT                                  |             |
| css-selector-tokenizer                                  | MIT                                  |             |
| css-tree                                                | MIT                                  |             |
| css-unit-converter                                      | MIT                                  |             |
| css-what                                                | BSD-2-Clause                         |             |
| cssesc                                                  | MIT                                  |             |
| cssnano                                                 | MIT                                  |             |
| cssnano-preset-default                                  | MIT                                  |             |
| cssnano-util-get-arguments                              | MIT                                  |             |
| cssnano-util-get-match                                  | MIT                                  |             |
| cssnano-util-raw-cache                                  | MIT                                  |             |
| cssnano-util-same-parent                                | MIT                                  |             |
| csso                                                    | MIT                                  |             |
| cssom                                                   | MIT                                  |             |
| cssstyle                                                | MIT                                  |             |
| currently-unhandled                                     | MIT                                  |             |
| custom-event                                            | MIT                                  |             |
| cyclist                                                 | MIT                                  |             |
| d                                                       | ISC                                  |             |
| d3                                                      | BSD-3-Clause                         |             |
| dashdash                                                | MIT                                  |             |
| date-format                                             | MIT                                  |             |
| date-now                                                | MIT                                  |             |
| dateformat                                              | MIT                                  |             |
| dayjs                                                   | MIT                                  |             |
| de-indent                                               | MIT                                  |             |
| debug                                                   | MIT                                  |             |
| decamelize                                              | MIT                                  |             |
| decode-uri-component                                    | MIT                                  |             |
| deep-equal                                              | MIT                                  |             |
| deep-is                                                 | MIT                                  |             |
| define-properties                                       | MIT                                  |             |
| define-property                                         | MIT                                  |             |
| defined                                                 | MIT                                  |             |
| del                                                     | MIT                                  |             |
| delayed-stream                                          | MIT                                  |             |
| delegate                                                | MIT                                  |             |
| delegates                                               | MIT                                  |             |
| depd                                                    | MIT                                  |             |
| des.js                                                  | MIT                                  |             |
| destroy                                                 | MIT                                  |             |
| detect-indent                                           | MIT                                  |             |
| detect-node                                             | ISC                                  |             |
| di                                                      | MIT                                  |             |
| diffie-hellman                                          | MIT                                  |             |
| dir-glob                                                | MIT                                  |             |
| dns-equal                                               | MIT                                  |             |
| dns-packet                                              | MIT                                  |             |
| dns-txt                                                 | MIT                                  |             |
| dom-converter                                           | MIT                                  |             |
| dom-serialize                                           | MIT                                  |             |
| dom-serializer                                          | MIT                                  |             |
| domain-browser                                          | MIT                                  |             |
| domelementtype                                          | BSD-2-Clause                         |             |
| domhandler                                              | BSD-2-Clause                         |             |
| domutils                                                | BSD-2-Clause                         |             |
| dot-prop                                                | MIT                                  |             |
| dotenv                                                  | BSD-2-Clause                         |             |
| duplexer                                                | BSD-2-Clause                         |             |
| duplexify                                               | MIT                                  |             |
| ecc-jsbn                                                | MIT                                  |             |
| echarts                                                 | Apache License   Version 2.0         |             |
| ee-first                                                | MIT                                  |             |
| ejs                                                     | Apache-2.0                           |             |
| electron-to-chromium                                    | ISC                                  |             |
| elliptic                                                | MIT                                  |             |
| emojis-list                                             | MIT                                  |             |
| encodeurl                                               | MIT                                  |             |
| end-of-stream                                           | MIT                                  |             |
| engine.io                                               | MIT                                  |             |
| engine.io-client                                        | MIT                                  |             |
| engine.io-parser                                        | MIT                                  |             |
| enhanced-resolve                                        | MIT                                  |             |
| ent                                                     | MIT                                  |             |
| entities                                                | BSD-2-Clause                         |             |
| env-parse                                               | ISC                                  |             |
| errno                                                   | MIT                                  |             |
| error-ex                                                | MIT                                  |             |
| es-abstract                                             | MIT                                  |             |
| es-to-primitive                                         | MIT                                  |             |
| es5-ext                                                 | ISC                                  |             |
| es6-iterator                                            | MIT                                  |             |
| es6-map                                                 | MIT                                  |             |
| es6-promise                                             | MIT                                  |             |
| es6-promisify                                           | MIT                                  |             |
| es6-set                                                 | MIT                                  |             |
| es6-symbol                                              | MIT                                  |             |
| es6-templates                                           | Apache 2                             |             |
| es6-weak-map                                            | ISC                                  |             |
| escape-html                                             | MIT                                  |             |
| escape-string-regexp                                    | MIT                                  |             |
| escodegen                                               | BSD-2-Clause                         |             |
| escope                                                  | BSD-2-Clause                         |             |
| eslint-scope                                            | BSD-2-Clause                         |             |
| eslint-visitor-keys                                     | Apache-2.0                           |             |
| esprima                                                 | BSD-2-Clause                         |             |
| esrecurse                                               | BSD-2-Clause                         |             |
| estraverse                                              | BSD                                  |             |
| esutils                                                 | BSD-2-Clause                         |             |
| etag                                                    | MIT                                  |             |
| event-emitter                                           | MIT                                  |             |
| event-stream                                            | MIT                                  |             |
| eventemitter3                                           | MIT                                  |             |
| events                                                  | MIT                                  |             |
| eventsource                                             | MIT                                  |             |
| evp_bytestokey                                          | MIT                                  |             |
| execa                                                   | MIT                                  |             |
| expand-braces                                           | MIT                                  |             |
| expand-brackets                                         | MIT                                  |             |
| expand-range                                            | MIT                                  |             |
| extend                                                  | MIT                                  |             |
| extend-shallow                                          | MIT                                  |             |
| extglob                                                 | MIT                                  |             |
| extract-text-webpack-plugin                             | MIT                                  |             |
| extsprintf                                              | MIT                                  |             |
| fast-deep-equal                                         | MIT                                  |             |
| fast-glob                                               | MIT                                  |             |
| fast-json-stable-stringify                              | MIT                                  |             |
| fast-levenshtein                                        | MIT                                  |             |
| fastparse                                               | MIT                                  |             |
| faye-websocket                                          | Apache-2.0                           |             |
| file-loader                                             | MIT                                  |             |
| fill-range                                              | MIT                                  |             |
| finalhandler                                            | MIT                                  |             |
| find-cache-dir                                          | MIT                                  |             |
| find-up                                                 | MIT                                  |             |
| flatted                                                 | ISC                                  |             |
| flatten                                                 | MIT                                  |             |
| flush-write-stream                                      | MIT                                  |             |
| follow-redirects                                        | MIT                                  |             |
| for-in                                                  | MIT                                  |             |
| for-own                                                 | MIT                                  |             |
| forever-agent                                           | Apache-2.0                           |             |
| form-data                                               | MIT                                  |             |
| forwarded                                               | MIT                                  |             |
| fragment-cache                                          | MIT                                  |             |
| fresh                                                   | MIT                                  |             |
| from                                                    | MIT                                  |             |
| from2                                                   | MIT                                  |             |
| fs-access                                               | MIT                                  |             |
| fs-write-stream-atomic                                  | ISC                                  |             |
| fs.realpath                                             | ISC                                  |             |
| fs.stat                                                 | MIT                                  |             |
| fstream                                                 | ISC                                  |             |
| function-bind                                           | MIT                                  |             |
| gauge                                                   | ISC                                  |             |
| gaze                                                    | MIT                                  |             |
| generator                                               | MIT                                  |             |
| get-caller-file                                         | ISC                                  |             |
| get-stdin                                               | MIT                                  |             |
| get-stream                                              | MIT                                  |             |
| get-value                                               | MIT                                  |             |
| getpass                                                 | MIT                                  |             |
| glob                                                    | ISC                                  |             |
| glob-parent                                             | ISC                                  |             |
| glob-to-regexp                                          | BSD                                  |             |
| globals                                                 | MIT                                  |             |
| globby                                                  | MIT                                  |             |
| globule                                                 | MIT                                  |             |
| good-listener                                           | MIT                                  |             |
| graceful-fs                                             | ISC                                  |             |
| handle-thing                                            | MIT                                  |             |
| handlebars                                              | MIT                                  |             |
| har-schema                                              | ISC                                  |             |
| har-validator                                           | MIT                                  |             |
| has                                                     | MIT                                  |             |
| has-ansi                                                | MIT                                  |             |
| has-binary2                                             | MIT                                  |             |
| has-cors                                                | MIT                                  |             |
| has-flag                                                | MIT                                  |             |
| has-symbols                                             | MIT                                  |             |
| has-unicode                                             | ISC                                  |             |
| has-value                                               | MIT                                  |             |
| has-values                                              | MIT                                  |             |
| hash-base                                               | MIT                                  |             |
| hash-sum                                                | MIT                                  |             |
| hash.js                                                 | MIT                                  |             |
| he                                                      | MIT                                  |             |
| helper-function-name                                    | MIT                                  |             |
| helper-get-function-arity                               | MIT                                  |             |
| helper-split-export-declaration                         | MIT                                  |             |
| hex-color-regex                                         | MIT                                  |             |
| highlight                                               | MIT                                  |             |
| hmac-drbg                                               | MIT                                  |             |
| home-or-tmp                                             | MIT                                  |             |
| hosted-git-info                                         | ISC                                  |             |
| hpack.js                                                | MIT                                  |             |
| hsl-regex                                               | MIT                                  |             |
| hsla-regex                                              | MIT                                  |             |
| html-comment-regex                                      | MIT                                  |             |
| html-entities                                           | MIT                                  |             |
| html-loader                                             | MIT                                  |             |
| html-minifier                                           | MIT                                  |             |
| html-webpack-ext-plugin                                 | MIT                                  |             |
| html-webpack-plugin                                     | MIT                                  |             |
| html2canvas                                             | MIT                                  |             |
| htmlparser2                                             | MIT                                  |             |
| http-deceiver                                           | MIT                                  |             |
| http-errors                                             | MIT                                  |             |
| http-parser-js                                          | MIT                                  |             |
| http-proxy                                              | MIT                                  |             |
| http-proxy-middleware                                   | MIT                                  |             |
| http-signature                                          | MIT                                  |             |
| https-browserify                                        | MIT                                  |             |
| https-proxy-agent                                       | MIT                                  |             |
| iconv-lite                                              | MIT                                  |             |
| icss-replace-symbols                                    | ISC                                  |             |
| icss-utils                                              | ISC                                  |             |
| ieee754                                                 | BSD-3-Clause                         |             |
| iferr                                                   | MIT                                  |             |
| ignore                                                  | MIT                                  |             |
| import-cwd                                              | MIT                                  |             |
| import-fresh                                            | MIT                                  |             |
| import-from                                             | MIT                                  |             |
| import-local                                            | MIT                                  |             |
| imurmurhash                                             | MIT                                  |             |
| in-publish                                              | ISC                                  |             |
| indent-string                                           | MIT                                  |             |
| indexes-of                                              | MIT                                  |             |
| indexof                                                 | MIT                                  |             |
| inflight                                                | ISC                                  |             |
| inherits                                                | ISC                                  |             |
| internal-ip                                             | MIT                                  |             |
| interpret                                               | MIT                                  |             |
| invariant                                               | MIT                                  |             |
| invert-kv                                               | MIT                                  |             |
| ip                                                      | MIT                                  |             |
| ipaddr.js                                               | MIT                                  |             |
| is-absolute-url                                         | MIT                                  |             |
| is-accessor-descriptor                                  | MIT                                  |             |
| is-arrayish                                             | MIT                                  |             |
| is-binary-path                                          | MIT                                  |             |
| is-buffer                                               | MIT                                  |             |
| is-callable                                             | MIT                                  |             |
| is-color-stop                                           | MIT                                  |             |
| is-data-descriptor                                      | MIT                                  |             |
| is-date-object                                          | MIT                                  |             |
| is-descriptor                                           | MIT                                  |             |
| is-directory                                            | MIT                                  |             |
| is-extendable                                           | MIT                                  |             |
| is-extglob                                              | MIT                                  |             |
| is-finite                                               | MIT                                  |             |
| is-fullwidth-code-point                                 | MIT                                  |             |
| is-glob                                                 | MIT                                  |             |
| is-number                                               | MIT                                  |             |
| is-obj                                                  | MIT                                  |             |
| is-path-cwd                                             | MIT                                  |             |
| is-path-in-cwd                                          | MIT                                  |             |
| is-path-inside                                          | MIT                                  |             |
| is-plain-obj                                            | MIT                                  |             |
| is-plain-object                                         | MIT                                  |             |
| is-regex                                                | MIT                                  |             |
| is-resolvable                                           | ISC                                  |             |
| is-running                                              | BSD                                  |             |
| is-stream                                               | MIT                                  |             |
| is-svg                                                  | MIT                                  |             |
| is-symbol                                               | MIT                                  |             |
| is-typedarray                                           | MIT                                  |             |
| is-utf8                                                 | MIT                                  |             |
| is-windows                                              | MIT                                  |             |
| is-wsl                                                  | MIT                                  |             |
| isarray                                                 | MIT                                  |             |
| isbinaryfile                                            | MIT                                  |             |
| isexe                                                   | ISC                                  |             |
| isobject                                                | MIT                                  |             |
| isstream                                                | MIT                                  |             |
| istanbul                                                | BSD-3-Clause                         |             |
| jasmine-core                                            | MIT                                  |             |
| jquery                                                  | MIT                                  |             |
| js-base64                                               | BSD-3-Clause                         |             |
| js-tokens                                               | MIT                                  |             |
| js-yaml                                                 | MIT                                  |             |
| jsbn                                                    | MIT                                  |             |
| jsdom                                                   | MIT                                  |             |
| jsesc                                                   | MIT                                  |             |
| json-loader                                             | MIT                                  |             |
| json-parse-better-errors                                | MIT                                  |             |
| json-schema                                             | cv2.1,BSD                            |             |
| json-schema-traverse                                    | MIT                                  |             |
| json-stringify-safe                                     | ISC                                  |             |
| json3                                                   | MIT                                  |             |
| json5                                                   | MIT                                  |             |
| jsplumb                                                 | (MIT OR GPL-2.0)                     |             |
| jsprim                                                  | MIT                                  |             |
| karma                                                   | MIT                                  |             |
| karma-browserstack-launcher                             | MIT                                  |             |
| karma-chrome-launcher                                   | MIT                                  |             |
| karma-coverage                                          | MIT                                  |             |
| karma-jasmine                                           | MIT                                  |             |
| karma-sourcemap-loader                                  | MIT                                  |             |
| karma-spec-reporter                                     | MIT                                  |             |
| karma-webpack                                           | MIT                                  |             |
| killable                                                | ISC                                  |             |
| kind-of                                                 | MIT                                  |             |
| last-call-webpack-plugin                                | MIT                                  |             |
| lazy-cache                                              | MIT                                  |             |
| lcid                                                    | MIT                                  |             |
| levn                                                    | MIT                                  |             |
| load-json-file                                          | MIT                                  |             |
| loader-runner                                           | MIT                                  |             |
| loader-utils                                            | MIT                                  |             |
| locate-path                                             | MIT                                  |             |
| lodash                                                  | MIT                                  |             |
| lodash.camelcase                                        | MIT                                  |             |
| lodash.memoize                                          | MIT                                  |             |
| lodash.tail                                             | MIT                                  |             |
| lodash.uniq                                             | MIT                                  |             |
| log-symbols                                             | MIT                                  |             |
| log4js                                                  | Apache-2.0                           |             |
| loglevel                                                | MIT                                  |             |
| loglevelnext                                            | MIT                                  |             |
| longest                                                 | MIT                                  |             |
| loose-envify                                            | MIT                                  |             |
| loud-rejection                                          | MIT                                  |             |
| lower-case                                              | MIT                                  |             |
| lru-cache                                               | ISC                                  |             |
| make-dir                                                | MIT                                  |             |
| map-cache                                               | MIT                                  |             |
| map-obj                                                 | MIT                                  |             |
| map-stream                                              | MIT                                  |             |
| map-visit                                               | MIT                                  |             |
| math-expression-evaluator                               | MIT                                  |             |
| md5.js                                                  | MIT                                  |             |
| mdn-data                                                | CC0-1.0                              |             |
| media-typer                                             | MIT                                  |             |
| mem                                                     | MIT                                  |             |
| memory-fs                                               | MIT                                  |             |
| meow                                                    | MIT                                  |             |
| merge-descriptors                                       | MIT                                  |             |
| merge2                                                  | MIT                                  |             |
| methods                                                 | MIT                                  |             |
| micromatch                                              | MIT                                  |             |
| miller-rabin                                            | MIT                                  |             |
| mime                                                    | MIT                                  |             |
| mime-db                                                 | MIT                                  |             |
| mime-types                                              | MIT                                  |             |
| mimic-fn                                                | MIT                                  |             |
| minimalistic-assert                                     | ISC                                  |             |
| minimalistic-crypto-utils                               | MIT                                  |             |
| minimatch                                               | ISC                                  |             |
| minimist                                                | MIT                                  |             |
| mississippi                                             | BSD-2-Clause                         |             |
| mixin-deep                                              | MIT                                  |             |
| mixin-object                                            | MIT                                  |             |
| mkdirp                                                  | MIT                                  |             |
| move-concurrently                                       | ISC                                  |             |
| ms                                                      | MIT                                  |             |
| multicast-dns                                           | MIT                                  |             |
| multicast-dns-service-types                             | MIT                                  |             |
| multirepo                                               | MIT                                  |             |
| mylib                                                   | ISC                                  |             |
| nan                                                     | MIT                                  |             |
| nanomatch                                               | MIT                                  |             |
| negotiator                                              | MIT                                  |             |
| neo-async                                               | MIT                                  |             |
| next-tick                                               | MIT                                  |             |
| nice-try                                                | MIT                                  |             |
| no-case                                                 | MIT                                  |             |
| node-forge                                              | (BSD-3-Clause OR   GPL-2.0)          |             |
| node-gyp                                                | MIT                                  |             |
| node-libs-browser                                       | MIT                                  |             |
| node-releases                                           | MIT                                  |             |
| node-sass                                               | MIT                                  |             |
| nopt                                                    | ISC                                  |             |
| normalize-package-data                                  | BSD-2-Clause                         |             |
| normalize-path                                          | MIT                                  |             |
| normalize-range                                         | MIT                                  |             |
| normalize-url                                           | MIT                                  |             |
| npm-run-path                                            | MIT                                  |             |
| npmlog                                                  | ISC                                  |             |
| nth-check                                               | BSD-2-Clause                         |             |
| null-check                                              | MIT                                  |             |
| num2fraction                                            | MIT                                  |             |
| number-is-nan                                           | MIT                                  |             |
| nwmatcher                                               | MIT                                  |             |
| oauth-sign                                              | Apache-2.0                           |             |
| object-assign                                           | MIT                                  |             |
| object-component                                        | MIT                                  |             |
| object-copy                                             | MIT                                  |             |
| object-keys                                             | MIT                                  |             |
| object-visit                                            | MIT                                  |             |
| object.assign                                           | MIT                                  |             |
| object.getownpropertydescriptors                        | MIT                                  |             |
| object.pick                                             | MIT                                  |             |
| object.values                                           | MIT                                  |             |
| obuf                                                    | MIT                                  |             |
| on-finished                                             | MIT                                  |             |
| on-headers                                              | MIT                                  |             |
| once                                                    | ISC                                  |             |
| opn                                                     | MIT                                  |             |
| optimist                                                | MIT/X11                              |             |
| optimize-css-assets-webpack-plugin                      | MIT                                  |             |
| optionator                                              | MIT                                  |             |
| original                                                | MIT                                  |             |
| os-browserify                                           | MIT                                  |             |
| os-homedir                                              | MIT                                  |             |
| os-locale                                               | MIT                                  |             |
| os-tmpdir                                               | MIT                                  |             |
| osenv                                                   | ISC                                  |             |
| p-finally                                               | MIT                                  |             |
| p-limit                                                 | MIT                                  |             |
| p-locate                                                | MIT                                  |             |
| p-map                                                   | MIT                                  |             |
| p-try                                                   | MIT                                  |             |
| package-a                                               | MIT                                  |             |
| package-b                                               | MIT                                  |             |
| pako                                                    | (MIT AND Zlib)                       |             |
| parallel-transform                                      | MIT                                  |             |
| param-case                                              | MIT                                  |             |
| parse-asn1                                              | ISC                                  |             |
| parse-json                                              | MIT                                  |             |
| parse5                                                  | MIT                                  |             |
| parseqs                                                 | MIT                                  |             |
| parseuri                                                | MIT                                  |             |
| parseurl                                                | MIT                                  |             |
| pascalcase                                              | MIT                                  |             |
| path-browserify                                         | MIT                                  |             |
| path-dirname                                            | MIT                                  |             |
| path-exists                                             | MIT                                  |             |
| path-is-absolute                                        | MIT                                  |             |
| path-is-inside                                          | (WTFPL OR MIT)                       |             |
| path-key                                                | MIT                                  |             |
| path-parse                                              | MIT                                  |             |
| path-to-regexp                                          | MIT                                  |             |
| path-type                                               | MIT                                  |             |
| pause-stream                                            | MIT,Apache2                          |             |
| pbkdf2                                                  | MIT                                  |             |
| performance-now                                         | MIT                                  |             |
| pify                                                    | MIT                                  |             |
| pinkie                                                  | MIT                                  |             |
| pinkie-promise                                          | MIT                                  |             |
| pkg-dir                                                 | MIT                                  |             |
| portfinder                                              | MIT                                  |             |
| posix-character-classes                                 | MIT                                  |             |
| postcss                                                 | MIT                                  |             |
| postcss-calc                                            | MIT                                  |             |
| postcss-colormin                                        | MIT                                  |             |
| postcss-convert-values                                  | MIT                                  |             |
| postcss-discard-comments                                | MIT                                  |             |
| postcss-discard-duplicates                              | MIT                                  |             |
| postcss-discard-empty                                   | MIT                                  |             |
| postcss-discard-overridden                              | MIT                                  |             |
| postcss-discard-unused                                  | MIT                                  |             |
| postcss-filter-plugins                                  | MIT                                  |             |
| postcss-load-config                                     | MIT                                  |             |
| postcss-load-options                                    | MIT                                  |             |
| postcss-load-plugins                                    | MIT                                  |             |
| postcss-loader                                          | MIT                                  |             |
| postcss-merge-idents                                    | MIT                                  |             |
| postcss-merge-longhand                                  | MIT                                  |             |
| postcss-merge-rules                                     | MIT                                  |             |
| postcss-message-helpers                                 | MIT                                  |             |
| postcss-minify-font-values                              | MIT                                  |             |
| postcss-minify-gradients                                | MIT                                  |             |
| postcss-minify-params                                   | MIT                                  |             |
| postcss-minify-selectors                                | MIT                                  |             |
| postcss-modules-extract-imports                         | ISC                                  |             |
| postcss-modules-local-by-default                        | MIT                                  |             |
| postcss-modules-scope                                   | ISC                                  |             |
| postcss-modules-values                                  | ISC                                  |             |
| postcss-normalize-charset                               | MIT                                  |             |
| postcss-normalize-display-values                        | MIT                                  |             |
| postcss-normalize-positions                             | MIT                                  |             |
| postcss-normalize-repeat-style                          | MIT                                  |             |
| postcss-normalize-string                                | MIT                                  |             |
| postcss-normalize-timing-functions                      | MIT                                  |             |
| postcss-normalize-unicode                               | MIT                                  |             |
| postcss-normalize-url                                   | MIT                                  |             |
| postcss-normalize-whitespace                            | MIT                                  |             |
| postcss-ordered-values                                  | MIT                                  |             |
| postcss-reduce-idents                                   | MIT                                  |             |
| postcss-reduce-initial                                  | MIT                                  |             |
| postcss-reduce-transforms                               | MIT                                  |             |
| postcss-selector-parser                                 | MIT                                  |             |
| postcss-svgo                                            | MIT                                  |             |
| postcss-unique-selectors                                | MIT                                  |             |
| postcss-value-parser                                    | MIT                                  |             |
| postcss-zindex                                          | MIT                                  |             |
| prelude-ls                                              | MIT                                  |             |
| prepend-http                                            | MIT                                  |             |
| prettier                                                | MIT                                  |             |
| pretty-error                                            | MIT                                  |             |
| private                                                 | MIT                                  |             |
| process                                                 | MIT                                  |             |
| process-nextick-args                                    | MIT                                  |             |
| promise-inflight                                        | ISC                                  |             |
| proxy-addr                                              | MIT                                  |             |
| prr                                                     | MIT                                  |             |
| ps-tree                                                 | MIT                                  |             |
| pseudomap                                               | ISC                                  |             |
| psl                                                     | MIT                                  |             |
| public-encrypt                                          | MIT                                  |             |
| pump                                                    | MIT                                  |             |
| pumpify                                                 | MIT                                  |             |
| punycode                                                | MIT                                  |             |
| q                                                       | MIT                                  |             |
| qjobs                                                   | MIT                                  |             |
| qs                                                      | BSD-3-Clause                         |             |
| query-string                                            | MIT                                  |             |
| querystring                                             | MIT                                  |             |
| querystring-es3                                         | MIT                                  |             |
| querystringify                                          | MIT                                  |             |
| randombytes                                             | MIT                                  |             |
| randomfill                                              | MIT                                  |             |
| range-parser                                            | MIT                                  |             |
| raw-body                                                | MIT                                  |             |
| read-pkg                                                | MIT                                  |             |
| read-pkg-up                                             | MIT                                  |             |
| readable-stream                                         | MIT                                  |             |
| readdir-enhanced                                        | MIT                                  |             |
| readdirp                                                | MIT                                  |             |
| recast                                                  | MIT                                  |             |
| redent                                                  | MIT                                  |             |
| reduce-css-calc                                         | MIT                                  |             |
| reduce-function-call                                    | MIT                                  |             |
| regenerate                                              | MIT                                  |             |
| regenerator-runtime                                     | MIT                                  |             |
| regenerator-transform                                   | BSD                                  |             |
| regex-not                                               | MIT                                  |             |
| regexpu-core                                            | MIT                                  |             |
| regjsgen                                                | MIT                                  |             |
| regjsparser                                             | BSD                                  |             |
| relateurl                                               | MIT                                  |             |
| remove-trailing-separator                               | ISC                                  |             |
| renderkid                                               | MIT                                  |             |
| repeat-element                                          | MIT                                  |             |
| repeat-string                                           | MIT                                  |             |
| repeating                                               | MIT                                  |             |
| request                                                 | Apache-2.0                           |             |
| require-directory                                       | MIT                                  |             |
| require-from-string                                     | MIT                                  |             |
| require-main-filename                                   | ISC                                  |             |
| requires-port                                           | MIT                                  |             |
| resolve                                                 | MIT                                  |             |
| resolve-cwd                                             | MIT                                  |             |
| resolve-from                                            | MIT                                  |             |
| resolve-url                                             | MIT                                  |             |
| ret                                                     | MIT                                  |             |
| rfdc                                                    | MIT                                  |             |
| rgb-regex                                               | MIT                                  |             |
| rgba-regex                                              | MIT                                  |             |
| rgbcolor                                                | MIT OR SEE LICENSE IN   FEEL-FREE.md |             |
| right-align                                             | MIT                                  |             |
| rimraf                                                  | ISC                                  |             |
| ripemd160                                               | MIT                                  |             |
| run-queue                                               | ISC                                  |             |
| safe-buffer                                             | MIT                                  |             |
| safe-regex                                              | MIT                                  |             |
| safer-buffer                                            | MIT                                  |             |
| sass-graph                                              | MIT                                  |             |
| sass-loader                                             | MIT                                  |             |
| sax                                                     | ISC                                  |             |
| schema-utils                                            | MIT                                  |             |
| scss-tokenizer                                          | MIT                                  |             |
| select                                                  | MIT                                  |             |
| select-hose                                             | MIT                                  |             |
| selfsigned                                              | MIT                                  |             |
| semver                                                  | ISC                                  |             |
| send                                                    | MIT                                  |             |
| serialize-javascript                                    | BSD-3-Clause                         |             |
| serve-index                                             | MIT                                  |             |
| serve-static                                            | MIT                                  |             |
| set-blocking                                            | ISC                                  |             |
| set-value                                               | MIT                                  |             |
| setimmediate                                            | MIT                                  |             |
| setprototypeof                                          | ISC                                  |             |
| sha.js                                                  | (MIT AND   BSD-3-Clause)             |             |
| shallow-clone                                           | MIT                                  |             |
| shebang-command                                         | MIT                                  |             |
| shebang-regex                                           | MIT                                  |             |
| signal-exit                                             | ISC                                  |             |
| simple-swizzle                                          | MIT                                  |             |
| slash                                                   | MIT                                  |             |
| snapdragon                                              | MIT                                  |             |
| snapdragon-node                                         | MIT                                  |             |
| snapdragon-util                                         | MIT                                  |             |
| socket.io                                               | MIT                                  |             |
| socket.io-adapter                                       | MIT                                  |             |
| socket.io-client                                        | MIT                                  |             |
| socket.io-parser                                        | MIT                                  |             |
| sockjs                                                  | MIT                                  |             |
| sockjs-client                                           | MIT                                  |             |
| sort-keys                                               | MIT                                  |             |
| source-list-map                                         | MIT                                  |             |
| source-map                                              | BSD-3-Clause                         |             |
| source-map-resolve                                      | MIT                                  |             |
| source-map-support                                      | MIT                                  |             |
| source-map-url                                          | MIT                                  |             |
| spdx-correct                                            | Apache-2.0                           |             |
| spdx-exceptions                                         | CC-BY-3.0                            |             |
| spdx-expression-parse                                   | MIT                                  |             |
| spdx-license-ids                                        | CC0-1.0                              |             |
| spdy                                                    | MIT                                  |             |
| spdy-transport                                          | MIT                                  |             |
| split                                                   | MIT                                  |             |
| split-string                                            | MIT                                  |             |
| sprintf-js                                              | BSD-3-Clause                         |             |
| sshpk                                                   | MIT                                  |             |
| ssri                                                    | ISC                                  |             |
| stable                                                  | MIT                                  |             |
| stackblur-canvas                                        | MIT                                  |             |
| static-extend                                           | MIT                                  |             |
| statuses                                                | MIT                                  |             |
| stdout-stream                                           | MIT                                  |             |
| stream-browserify                                       | MIT                                  |             |
| stream-combiner                                         | MIT                                  |             |
| stream-each                                             | MIT                                  |             |
| stream-http                                             | MIT                                  |             |
| stream-shift                                            | MIT                                  |             |
| streamroller                                            | MIT                                  |             |
| strict-uri-encode                                       | MIT                                  |             |
| string-width                                            | MIT                                  |             |
| string_decoder                                          | MIT                                  |             |
| strip-ansi                                              | MIT                                  |             |
| strip-bom                                               | MIT                                  |             |
| strip-eof                                               | MIT                                  |             |
| strip-indent                                            | MIT                                  |             |
| stylehacks                                              | MIT                                  |             |
| supports-color                                          | MIT                                  |             |
| svgo                                                    | MIT                                  |             |
| symbol-tree                                             | MIT                                  |             |
| tapable                                                 | MIT                                  |             |
| tar                                                     | ISC                                  |             |
| temp-fs                                                 | MIT                                  |             |
| template                                                | MIT                                  |             |
| through                                                 | MIT                                  |             |
| through2                                                | MIT                                  |             |
| thunky                                                  | MIT                                  |             |
| time-stamp                                              | MIT                                  |             |
| timers-browserify                                       | MIT                                  |             |
| timsort                                                 | MIT                                  |             |
| tiny-emitter                                            | MIT                                  |             |
| tmp                                                     | MIT                                  |             |
| to-array                                                | MIT                                  |             |
| to-arraybuffer                                          | MIT                                  |             |
| to-fast-properties                                      | MIT                                  |             |
| to-object-path                                          | MIT                                  |             |
| to-regex                                                | MIT                                  |             |
| to-regex-range                                          | MIT                                  |             |
| toidentifier                                            | MIT                                  |             |
| tools                                                   | MIT                                  |             |
| toposort                                                | MIT                                  |             |
| tough-cookie                                            | BSD-3-Clause                         |             |
| tr46                                                    | MIT                                  |             |
| traverse                                                | MIT                                  |             |
| trim-newlines                                           | MIT                                  |             |
| trim-right                                              | MIT                                  |             |
| true-case-path                                          | Apache-2.0                           |             |
| tty-browserify                                          | MIT                                  |             |
| tunnel-agent                                            | Apache-2.0                           |             |
| tweetnacl                                               | The Unlicense                        |             |
| type                                                    | ISC                                  |             |
| type-check                                              | MIT                                  |             |
| type-is                                                 | MIT                                  |             |
| typedarray                                              | MIT                                  |             |
| types                                                   | MIT                                  |             |
| uglify-es                                               | BSD-2-Clause                         |             |
| uglify-js                                               | BSD-2-Clause                         |             |
| uglify-to-browserify                                    | MIT                                  |             |
| uglifyjs-webpack-plugin                                 | MIT                                  |             |
| ultron                                                  | MIT                                  |             |
| union-value                                             | MIT                                  |             |
| uniq                                                    | MIT                                  |             |
| uniqs                                                   | MIT                                  |             |
| unique-filename                                         | ISC                                  |             |
| unique-slug                                             | ISC                                  |             |
| unpipe                                                  | MIT                                  |             |
| unquote                                                 | MIT                                  |             |
| unset-value                                             | MIT                                  |             |
| upath                                                   | MIT                                  |             |
| upper-case                                              | MIT                                  |             |
| uri-js                                                  | BSD-2-Clause                         |             |
| urix                                                    | MIT                                  |             |
| url                                                     | MIT                                  |             |
| url-join                                                | MIT                                  |             |
| url-loader                                              | MIT                                  |             |
| url-parse                                               | MIT                                  |             |
| use                                                     | MIT                                  |             |
| useragent                                               | MIT                                  |             |
| util                                                    | MIT                                  |             |
| util-deprecate                                          | MIT                                  |             |
| util.promisify                                          | MIT                                  |             |
| utila                                                   | MIT                                  |             |
| utils-merge                                             | MIT                                  |             |
| uuid                                                    | MIT                                  |             |
| validate-npm-package-license                            | Apache-2.0                           |             |
| vary                                                    | MIT                                  |             |
| vendors                                                 | MIT                                  |             |
| verror                                                  | MIT                                  |             |
| vm-browserify                                           | MIT                                  |             |
| void-elements                                           | MIT                                  |             |
| vue                                                     | MIT                                  |             |
| vue-hot-reload-api                                      | MIT                                  |             |
| vue-loader                                              | MIT                                  |             |
| vue-router                                              | MIT                                  |             |
| vue-style-loader                                        | MIT                                  |             |
| vue-template-compiler                                   | MIT                                  |             |
| vue-template-es2015-compiler                            | MIT                                  |             |
| vuex                                                    | MIT                                  |             |
| vuex-router-sync                                        | MIT                                  |             |
| watchpack                                               | MIT                                  |             |
| wbuf                                                    | MIT                                  |             |
| webidl-conversions                                      | BSD-2-Clause                         |             |
| webpack                                                 | MIT                                  |             |
| webpack-dev-middleware                                  | MIT                                  |             |
| webpack-dev-server                                      | MIT                                  |             |
| webpack-log                                             | MIT                                  |             |
| webpack-merge                                           | MIT                                  |             |
| webpack-sources                                         | MIT                                  |             |
| websocket-driver                                        | Apache-2.0                           |             |
| websocket-extensions                                    | MIT                                  |             |
| whatwg-url                                              | MIT                                  |             |
| whet.extend                                             | MIT                                  |             |
| which                                                   | ISC                                  |             |
| which-module                                            | ISC                                  |             |
| wide-align                                              | ISC                                  |             |
| window-size                                             | MIT                                  |             |
| wordwrap                                                | MIT/X11                              |             |
| worker-farm                                             | MIT                                  |             |
| wrap-ansi                                               | MIT                                  |             |
| wrappy                                                  | ISC                                  |             |
| ws                                                      | MIT                                  |             |
| xml-name-validator                                      | WTFPL                                |             |
| xmldom                                                  | MIT,LGPL                             | will remove |
| xmlhttprequest-ssl                                      | MIT                                  |             |
| xtend                                                   | MIT                                  |             |
| y18n                                                    | ISC                                  |             |
| yallist                                                 | ISC                                  |             |
| yargs                                                   | MIT                                  |             |
| yargs-parser                                            | ISC                                  |             |
| yeast                                                   | MIT                                  |             |
| zrender                                                 | BSD                                  |             |



## Required Resources

### Git Repositories

- <https://github.com/analysys/EasyScheduler.git>
- <https://github.com/analysys/easyscheduler_docs.git>

### Issue Tracking

The community would like to continue using GitHub Issues.

### Continuous Integration tool

Travis  （TODO）

### Mailing Lists

- EasyScheduler-dev: for development discussions
- EasyScheduler-private: for PPMC discussions
- EasyScheduler-notifications: for users notifications

## Initial Committers

- William-GuoWei
- Lidong Dai
- Zhanwei Qiao
- Liang Bao
- Gang Li
- Zijian Gong
- Jun Gao
- Baoqi Wu

## Affiliations

- Analysys: William-GuoWei，Zhanwei Qiao，Liang Bao，Gang Li，Jun Gao，Lidong Dai

- Hydee: Zijian Gong

- Guandata: Baoqi Wu

  

## Sponsors

### Champion

- Sheng Wu ( Apache Software Foundation Member  [wusheng@apache.org](mailto:wusheng@apache.org))

### Mentors

- Sheng Wu ( Apache Software Foundation Member  [wusheng@apache.org](mailto:wusheng@apache.org))

- ShaoFeng Shi  ( Apache Software Foundation Incubator PMC  [wusheng@apache.org](mailto:wusheng@apache.org))

- Liang Chen ( Apache Software Foundation Member  chenliang613@apache.org](mailto:chenliang613@apache.org))

  

### Sponsoring Entity

We are expecting the Apache Incubator could sponsor this project.



