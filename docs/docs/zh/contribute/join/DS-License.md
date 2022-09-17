# License 须知

如您所知，DolphinScheduler现属于ASF(Apache基金会)下的开源项目，这意味着当您想要成为DolphinScheduler的贡献者的时候，就必须按照Apache的规则来，而Apache对于License有着极其严苛的规则，为了避免贡献者在License上浪费过多的时间，
本文将为您讲解ASF—License以及参与DolphinScheduler如何过早的规避掉License风险。

注：本文仅适用于Apache项目。

### Apache项目可接受的License

当您想要为DolphinScheduler（亦或其他Apache项目）增添一个新的功能，这个功能涉及到其他开源软件的引用，那么您必须注意，目前Apache项目支持遵从以下协议的开源软件（如果有遗漏，欢迎补充）：

[ASF第三方许可证策](https://apache.org/legal/resolved.html)

如果您所使用的第三方软件并不在以上协议之中，那么很抱歉，您的代码将无法通过审核，建议您找寻其他替代方案。

另外，当您需要使用新的软件的时候，请将您这样做的原因、最终产出结果发邮件至dev@dolphinscheduler.apache.org讨论，当得到至少3票PPMC认同的时候，您方可以引入。

### 如何在DolphinScheduler合法的使用第三方开源软件

当我们想要引入一个新的第三方软件(包含但不限于第三方的jar、文本、css、js、图片、图标、音视频等及在第三方基础上做的修改)至我们的项目中的时候，除了他们所遵从的协议是Apache允许的，另外一点很重要，就是合法的使用。您可以参考以下文章

* [COMMUNITY-LED DEVELOPMENT "THE APACHE WAY"](https://apache.org/dev/licensing-howto.html)

以Apache为例，当我们使用了ZooKeeper，那么ZooKeeper的NOTICE文件（每个开源项目都会有NOTICE文件，一般位于根目录）则必须在我们的项目中体现，用Apache的话来讲，就是"Work" shall mean the work of authorship, whether in Source or Object form, made available under the License, as indicated by a
copyright notice that is included in or attached to the work.

关于具体的各个开源协议使用协议，在此不做过多篇幅一一介绍，有兴趣可以自行查询了解。

### DolphinScheduler-License 检测规则

一般来讲，我们都会为自己的项目建立License-check脚本，DolphinScheduler-License是由[kezhenxu94](https://github.com/kezhenxu94)提供，其他开源软件略有不同，但最终结果都是为了确保我们在使用过程中能够第一时间避免License的问题。

当我们需要添加新的Jar或其他外部资源的时候，我们需要按照以下步骤：

* 在known-dependencies.txt中添加你所需要的jar名称+版本。
* 在dolphinscheduler-dist/release-docs/LICENSE中添加相关的maven仓库地址。
* 在dolphinscheduler-dist/release-docs/NOTICE中追加相关的NOTICE文件，此文件请务必和原代码仓库地址中的NOTICE文件一致。
* 在dolphinscheduler-dist/release-docs/license/下添加相关源代码的协议，文件命名为license+文件名.txt。

#### check dependency license fail

```
--- /dev/fd/63	2020-12-03 03:08:57.191579482 +0000
+++ /dev/fd/62	2020-12-03 03:08:57.191579482 +0000
@@ -1,0 +2 @@
+HikariCP-java6-2.3.13.jar
@@ -16,0 +18 @@
+c3p0-0.9.5.2.jar
@@ -149,0 +152 @@
+mchange-commons-java-0.2.11.jar
Error: Process completed with exit code 1.
```

一般来讲，添加一个jar的工作往往不会如此轻易的结束，因为它往往依赖了其它各种各样的jar，这些jar我们同样需要添加相应的license。
这种情况下，我们会在check里面得到 check dependency license fail的错误信息，如上，我们缺少了HikariCP-java6-2.3.13、c3p0等的license声明，
按照添加jar的步骤补充即可，提示还是蛮友好的（哈哈）。

### 附件

<!-- markdown-link-check-disable -->
附件：新jar的邮-->

```
[VOTE][New Jar] jetcd-core(registry plugin support etcd3 ) 


（说明目的，以及需要添加的 jar 是什么）Hi, the registry SPI will provide the implementation of etcd3. Therefore, we need to introduce a new jar (jetcd-core, jetcd-launcher (test)), which complies with the Apache-2.0 License. I checked his related dependencies to make sure it complies with the license of the Apache project.

new jar : 

jetcd-core             version -x.x.x   license apache2.0

jetcd-launcher (test)  version -x.x.x   license apache2.0

dependent jar（它依赖了哪些jar，最好附带版本,以及相关采用的license协议）:
grpc-core     version -x.x.x  license XXX
grpc-netty    version -x.x.x  license XXX
grpc-protobuf version -x.x.x  license XXX
grpc-stub     version -x.x.x  license XXX
grpc-grpclb   version -x.x.x  license XXX
netty-all     version -x.x.x  license XXX
failsafe      version -x.x.x  license XXX

相关地址：主要有github地址、license文件地址、notice 文件地址、maven中央仓库地址

github address:https://github.com/etcd-io/jetcd


license:https://github.com/etcd-io/jetcd/blob/master/LICENSE


notice:https://github.com/etcd-io/jetcd/blob/master/NOTICE


Maven repository:


https://mvnrepository.com/artifact/io.etcd/jetcd-core


https://mvnrepository.com/artifact/io.etcd/jetcd-launcher
```

<!-- markdown-link-check-enable -->

### 参考文章：

* [COMMUNITY-LED DEVELOPMENT "THE APACHE WAY"](https://apache.org/dev/licensing-howto.html)
* [ASF 3RD PARTY LICENSE POLICY](https://apache.org/legal/resolved.html)

