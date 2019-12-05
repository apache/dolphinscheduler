# EasyScheduler Proposal

## Abstract

EasyScheduler is a distributed ETL scheduling engine with powerful DAG visualization interface. EasyScheduler focuses on solving the problem of 'complex task dependencies & triggers ' in data processing. Just like its name, we dedicated to making the scheduling system `out of the box`. 

## Proposal

EasyScheduler provides many easy-to-use features to accelerate the engineer efficiency on data ETL workflow job. We propose a new concept of 'instance of process' and 'instance of task' to let developers to tuning their jobs on the running state of workflow instead of changing the task's template. Its main objectives are as follows:

- Define the complex tasks' dependencies & triggers  in a DAG graph by dragging and dropping.
- Support cluster HA.
- Support multi-tenant and parallel or serial backfilling data.
- Support automatical failure job retry and recovery.
- Support many data task types and process priority, task priority and relative task timeout alarm.

For now, EasyScheduler has a fairly huge community in China. 
It is also widely adopted by many [companies and organizations](https://github.com/analysys/EasyScheduler/issues/57) as its ETL scheduling tool. 

We believe that bringing EasyScheduler into ASF could advance development of a much more stronger and more diverse open source community.

Analysys submits this proposal to donate EasyScheduler's source codes and all related documentations to Apache Software Foundation. 
The codes are already under Apache License Version 2.0.

- Code base: https://www.github.com/analysys/easyscheduler
- English Documentations: <https://analysys.github.io/easyscheduler_docs>
- Chinese Documentations: <https://analysys.github.io/easyscheduler_docs_cn>

## Background

We want to find a data processing tool with the following features:

- Easy to use，developers can build a ETL process with a very simple drag and drop operation. not only for ETL developers，people who can't write code also can use this tool for ETL operation such as system administrator.
- Solving the problem of "complex  task dependencies" , and it can monitor the ETL running status. 
- Support multi-tenant.
- Support many task types: Shell, MR, Spark, SQL (mysql, postgresql, hive, sparksql), Python, Sub_Process, Procedure, etc.
- Support HA and linear scalability.

For the above reasons, we realized that no existing product met our requirements, so we decided to develop this tool ourselves. We designed EasyScheduler at the end of 2017. The first internal use version was completed in May 2018. We then iterated several internal versions and the system gradually became stabilized. 

Then we open the source code of EasyScheduler on March 2019. It soon gained lot's of ETL developers interest and stars on github. 

## Rationale

Many organizations (>30) (refer to [Who is using EasyScheduler](https://github.com/analysys/EasyScheduler/issues/57) ) already benefit from running EasyScheduler to make data process pipelines more easier. More than 100  [feature ideas](https://github.com/analysys/EasyScheduler/projects/1) come from EasyScheduler community.   Some 3rd-party projects also plan to integrate with EasyScheduler through task plugin, such as [Scriptis](https://github.com/WeBankFinTech/Scriptis), [waterdrop](https://github.com/InterestingLab/waterdrop). These will strengthen the features of EasyScheduler.   

## Current Status

### Meritocracy

EasyScheduler was incubated at Analysys in 2017 and open sourced on GitHub in March 2019.  Once open sourced, we have been quickly adopted by multiple organizations，EasyScheduler has contributors and users from many companies; we have set up the Committer Team. New contributors are guided and reviewed by existed committer members. 
Contributions are always welcomed and highly valued. 

### Community

Now we have set development teams for EasyScheduler in Analysys, and we already have external developers who contributed the code.  We already have a user group of more than 1,000 people. 
We hope to grow the base of contributors by inviting all those who offer contributions through The Apache Way. 
Right now, we make use of github as code hosting as well as gitter for community communication.

### Core Developers

The core developers, including experienced senior developers, are often guided by mentors.

## Known Risks

### Orphaned products

EasyScheduler is widely adopted in China by many [companies and organizations](https://github.com/analysys/EasyScheduler/issues/57). The core developers of EasyScheduler team plan to work full time on this project. Currently there are 10 use cases with more that 1000 activity tasks per day using EasyScheduler in the user's production environment. There is very little risk of EasyScheduler getting orphaned as at least two large companies (xueqiu、fengjr) are widely using it in their production, and developers from these companies have also joined Easy Scheduler's team of contributors, EasyScheduler has eight major releases so far, and and received 373 pull requests from contributors, which further demonstrates EasyScheduler as a very active project. We also plan to extend and diversify this community further through Apache.

Thus, it is very unlikely that EasyScheduler becomes orphaned.

### Inexperience with Open Source

EasyScheduler's core developers have been running it as a community-oriented open source project for some time, several of them already have experience working with open source communities, they are also active in presto, alluxio and other projects. At the same time, we will learn more open source experiences by following the Apache way in our incubator journey.

### Homogeneous Developers

The current developers work across a variety of organizations including Analysys, guandata and hydee; 
some individual developers are accepted as developers of EasyScheduler as well. 
Considering that fengjr and sefonsoft have shown great interests in EasyScheduler, we plan to encourage them to contribute and invite them as contributors to work together.

### Reliance on Salaried Developers

At present, eight of the core developers are paid by their employer to contribute to EasyScheduler project. 
we also have some other developers and researchers taking part in the project, and we will make efforts to increase the diversity of the contributors and actively lobby for Domain experts in the workflow space to contribute. 

### Relationships with Other Apache Products

EasyScheduler integrates Apache Zookeeper as one of the service registration/discovery mechanisms. EasyScheduler is deeply integrated with Apache products. It currently support many task types like  Apache Hive, Apache Spark, Apache Hadoop, and so on

### A Excessive Fascination with the Apache Brand

We recognize the value and reputation that the Apache brand will bring to EasyScheduler.
However, we prefer that the community provided by the Apache Software Foundation will enable the project to achieve long-term stable development. so EasyScheduler is proposing to enter incubation at Apache in order to help efforts to diversify the community, not so much to capitalize on the Apache brand.

## Documentation

A complete set of EasyScheduler documentations is provided on github in both English and Simplified Chinese.

- [English](https://github.com/analysys/easyscheduler_docs)
- [Chinese](https://github.com/analysys/easyscheduler_docs_cn)

## Initial Source

The project consists of three distinct codebases: core and document. The address of two existed git repositories are as follows:

- <https://github.com/analysys/easyscheduler>
- <https://github.com/analysys/easyscheduler_docs> 
- <https://github.com/analysys/easyscheduler_docs_cn> 

## Source and Intellectual Property Submission Plan

As soon as EasyScheduler is approved to join Apache Incubator, Analysys will provide the Software Grant Agreement(SGA) and initial committers will submit ICLA(s). The code is already licensed under the Apache Software License, version 2.0. 

## External Dependencies

As all backend code dependencies are managed using Apache Maven, none of the external libraries need to be packaged in a source distribution. 

Most of dependencies have Apache compatible licenses，and the core dependencies are as follows:

### Backend Dependency

| Dependency                                             | License                                                      | Comments      |
| ------------------------------------------------------ | ------------------------------------------------------------ | ------------- |
| bonecp-0.8.0.RELEASE.jar                               | Apache v2.0                                                  |               |
| byte-buddy-1.9.10.jar                                  | Apache V2.0                                                  |               |
| c3p0-0.9.1.1.jar                                       | GNU LESSER GENERAL   PUBLIC LICENSE                          | will   remove |
| curator-*-2.12.0.jar                                   | Apache V2.0                                                  |               |
| druid-1.1.14.jar                                       | Apache V2.0                                                  |               |
| fastjson-1.2.29.jar                                    | Apache V2.0                                                  |               |
| fastutil-6.5.6.jar                                     | Apache V2.0                                                  |               |
| grpc-*-1.9.0.jar                                       | Apache V2.0                                                  |               |
| gson-2.8.5.jar                                         | Apache V2.0                                                  |               |
| guava-20.0.jar                                         | Apache V2.0                                                  |               |
| guice-*3.0.jar                                         | Apache V2.0                                                  |               |
| hadoop-*-2.7.3.jar                                     | Apache V2.0                                                  |               |
| hbase-*-1.1.1.jar                                      | Apache V2.0                                                  |               |
| hive-*-2.1.0.jar                                       | Apache V2.0                                                  |               |
| instrumentation-api-0.4.3.jar                          | Apache V2.0                                                  |               |
| jackson-*-2.9.8.jar                                    | Apache V2.0                                                  |               |
| jackson-jaxrs-1.8.3.jar                                | LGPL Version 2.1    Apache V2.0                              | will   remove |
| jackson-xc-1.8.3.jar                                   | LGPL Version 2.1    Apache V2.0                              | will   remove |
| javax.activation-api-1.2.0.jar                         | CDDL/GPLv2+CE                                                | will   remove |
| javax.annotation-api-1.3.2.jar                         | CDDL + GPLv2 with   classpath exception                      | will   remove |
| javax.servlet-api-3.1.0.jar                            | CDDL + GPLv2 with   classpath exception                      | will   remove |
| jaxb-*.jar                                             | (CDDL 1.1) (GPL2 w/   CPE)                                   | will   remove |
| jersey-*-1.9.jar                                       | CDDL+GPLv2                                                   | will   remove |
| jetty-*-9.4.14.v20181114.jar                           | Apache V2.0，EPL 1.0                                         |               |
| jna-4.5.2.jar                                          | Apache V2.0，LGPL 2.1                                        | will   remove |
| jna-platform-4.5.2.jar                                 | Apache V2.0，LGPL 2.1                                        | will   remove |
| jsp-api-2.x.jar                                        | CDDL，GPL 2.0                                                | will   remove |
| log4j-1.2.17.jar                                       | Apache V2.0                                                  |               |
| log4j-*-2.11.2.jar                                     | Apache V2.0                                                  |               |
| logback-x.jar                                          | dual-license      EPL 1.0,LGPL 2.1                           |               |
| mail-1.4.5.jar                                         | CDDL+GPLv2                                                   | will   remove |
| mybatis-3.5.1.jar                                      | Apache V2.0                                                  |               |
| mybatis-spring-*2.0.1.jar                              | Apache V2.0                                                  |               |
| mysql-connector-java-5.1.34.jar                        | GPL 2.0                                                      | will   remove |
| netty-*-4.1.33.Final.jar                               | Apache V2.0                                                  |               |
| oshi-core-3.5.0.jar                                    | EPL 1.0                                                      |               |
| parquet-hadoop-bundle-1.8.1.jar                        | Apache V2.0                                                  |               |
| postgresql-42.1.4.jar                                  | BSD 2-clause                                                 |               |
| protobuf-java-*3.5.1.jar                               | BSD 3-clause                                                 |               |
| quartz-2.2.3.jar                                       | Apache V2.0                                                  |               |
| quartz-jobs-2.2.3.jar                                  | Apache V2.0                                                  |               |
| slf4j-api-1.7.5.jar                                    | MIT                                                          |               |
| spring-*-5.1.5.RELEASE.jar                             | Apache V2.0                                                  |               |
| spring-beans-5.1.5.RELEASE.jar                         | Apache V2.0                                                  |               |
| spring-boot-*2.1.3.RELEASE.jar                         | Apache V2.0                                                  |               |
| springfox-*-2.9.2.jar                                  | Apache V2.0                                                  |               |
| stringtemplate-3.2.1.jar                               | BSD                                                          |               |
| swagger-annotations-1.5.20.jar                         | Apache V2.0                                                  |               |
| swagger-bootstrap-ui-1.9.3.jar                         | Apache V2.0                                                  |               |
| swagger-models-1.5.20.jar                              | Apache V2.0                                                  |               |
| zookeeper-3.4.8.jar                                    | Apache                                                       |               |




The front-end UI currently relies on many components, and the core dependencies are as follows:

### UI Dependency

| Dependency                                              | License                              | Comments    |
| ------------------------------------------------------- | ------------------------------------ | ----------- |
| autoprefixer                                            | MIT                                  |             |
| babel-core                                              | MIT                                  |             |
| babel-eslint                                            | MIT                                  |             |
| babel-helper-*                                          | MIT                                  |             |
| babel-helpers                                           | MIT                                  |             |
| babel-loader                                            | MIT                                  |             |
| babel-plugin-syntax-*                                   | MIT                                  |             |
| babel-plugin-transform-*                                | MIT                                  |             |
| babel-preset-env                                        | MIT                                  |             |
| babel-runtime                                           | MIT                                  |             |
| bootstrap                                               | MIT                                  |             |
| canvg                                                   | MIT                                  |             |
| clipboard                                               | MIT                                  |             |
| codemirror                                              | MIT                                  |             |
| copy-webpack-plugin                                     | MIT                                  |             |
| cross-env                                               | MIT                                  |             |
| css-loader                                              | MIT                                  |             |
| cssnano                                                 | MIT                                  |             |
| cyclist                                                 | MIT                                  |             |
| d3                                                      | BSD-3-Clause                         |             |
| dayjs                                                   | MIT                                  |             |
| echarts                                                 | Apache V2.0                          |             |
| env-parse                                               | ISC                                  |             |
| extract-text-webpack-plugin                             | MIT                                  |             |
| file-loader                                             | MIT                                  |             |
| globby                                                  | MIT                                  |             |
| html-loader                                             | MIT                                  |             |
| html-webpack-ext-plugin                                 | MIT                                  |             |
| html-webpack-plugin                                     | MIT                                  |             |
| html2canvas                                             | MIT                                  |             |
| jsplumb                                                 | (MIT OR GPL-2.0)                     |             |
| lodash                                                  | MIT                                  |             |
| node-sass                                               | MIT                                  |             |
| optimize-css-assets-webpack-plugin                      | MIT                                  |             |
| postcss-loader                                          | MIT                                  |             |
| rimraf                                                  | ISC                                  |             |
| sass-loader                                             | MIT                                  |             |
| uglifyjs-webpack-plugin                                 | MIT                                  |             |
| url-loader                                              | MIT                                  |             |
| util.promisify                                          | MIT                                  |             |
| vue                                                     | MIT                                  |             |
| vue-loader                                              | MIT                                  |             |
| vue-style-loader                                        | MIT                                  |             |
| vue-template-compiler                                   | MIT                                  |             |
| vuex-router-sync                                        | MIT                                  |             |
| watchpack                                               | MIT                                  |             |
| webpack                                                 | MIT                                  |             |
| webpack-dev-server                                      | MIT                                  |             |
| webpack-merge                                           | MIT                                  |             |
| xmldom                                                  | MIT,LGPL                             | will remove |


## Required Resources

### Git Repositories

- <https://github.com/analysys/EasyScheduler.git>
- <https://github.com/analysys/easyscheduler_docs.git>
- <https://github.com/analysys/easyscheduler_docs_cn.git>

### Issue Tracking

The community would like to continue using GitHub Issues.

### Continuous Integration tool

Jenkins

### Mailing Lists

- EasyScheduler-dev: for development discussions
- EasyScheduler-private: for PPMC discussions
- EasyScheduler-notifications: for users notifications

## Initial Committers

- William-GuoWei(guowei20m@outlook.com)
- Lidong Dai(lidong.dai@outlook.com)
- Zhanwei Qiao(qiaozhanwei@outlook.com)
- Liang Bao(baoliang.leon@gmail.com)
- Gang Li(lgcareer2019@outlook.com)
- Zijian Gong(quanquansy@gmail.com)
- Jun Gao(gaojun2048@gmail.com)
- Baoqi Wu(wubaoqi@gmail.com)

## Affiliations

- Analysys Inc: William-GuoWei，Zhanwei Qiao，Liang Bao，Gang Li，Jun Gao，Lidong Dai

- Hydee Inc: Zijian Gong

- Guandata Inc: Baoqi Wu

  

## Sponsors

### Champion

- Sheng Wu ( Apache Incubator PMC, [wusheng@apache.org](mailto:wusheng@apache.org))

### Mentors

- Sheng Wu ( Apache Incubator PMC,  [wusheng@apache.org](mailto:wusheng@apache.org))

- ShaoFeng Shi  ( Apache Incubator PMC,  [shaofengshi@apache.org](mailto:wusheng@apache.org))

- Liang Chen ( Apache Software Foundation Member,  [chenliang613@apache.org](mailto:chenliang613@apache.org))

  

### Sponsoring Entity

We are expecting the Apache Incubator could sponsor this project.
