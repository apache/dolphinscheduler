#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

workDir=`dirname $0`
workDir=`cd ${workDir};pwd`

source ${workDir}/conf/config/install_config.conf

# 1.replace file
echo "1.replace file"

txt=""
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    txt="''"
fi

datasourceDriverClassname="com.mysql.jdbc.Driver"
if [ $dbtype == "postgresql" ];then
  datasourceDriverClassname="org.postgresql.Driver"
fi

# Change configuration in conf/config/dolphinscheduler_env.sh
sed -i ${txt} "s@^export JAVA_HOME=.*@export JAVA_HOME=${javaHome}@g" conf/env/dolphinscheduler_env.sh

# Change configuration in conf/datasource.properties
sed -i ${txt} "s@^spring.datasource.driver-class-name=.*@spring.datasource.driver-class-name=${datasourceDriverClassname}@g" conf/datasource.properties
sed -i ${txt} "s@^spring.datasource.url=.*@spring.datasource.url=jdbc:${dbtype}://${dbhost}/${dbname}?characterEncoding=UTF-8\&allowMultiQueries=true@g" conf/datasource.properties
sed -i ${txt} "s@^spring.datasource.username=.*@spring.datasource.username=${username}@g" conf/datasource.properties
sed -i ${txt} "s@^spring.datasource.password=.*@spring.datasource.password=${password}@g" conf/datasource.properties

# Change configuration in conf/common.properties
sed -i ${txt} "s@^data.basedir.path=.*@data.basedir.path=${dataBasedirPath}@g" conf/common.properties
sed -i ${txt} "s@^resource.storage.type=.*@resource.storage.type=${resourceStorageType}@g" conf/common.properties
sed -i ${txt} "s@^resource.upload.path=.*@resource.upload.path=${resourceUploadPath}@g" conf/common.properties
sed -i ${txt} "s@^hadoop.security.authentication.startup.state=.*@hadoop.security.authentication.startup.state=${kerberosStartUp}@g" conf/common.properties
sed -i ${txt} "s@^java.security.krb5.conf.path=.*@java.security.krb5.conf.path=${krb5ConfPath}@g" conf/common.properties
sed -i ${txt} "s@^login.user.keytab.username=.*@login.user.keytab.username=${keytabUserName}@g" conf/common.properties
sed -i ${txt} "s@^login.user.keytab.path=.*@login.user.keytab.path=${keytabPath}@g" conf/common.properties
sed -i ${txt} "s@^kerberos.expire.time=.*@kerberos.expire.time=${kerberosExpireTime}@g" conf/common.properties
sed -i ${txt} "s@^hdfs.root.user=.*@hdfs.root.user=${hdfsRootUser}@g" conf/common.properties
sed -i ${txt} "s@^fs.defaultFS=.*@fs.defaultFS=${defaultFS}@g" conf/common.properties
sed -i ${txt} "s@^fs.s3a.endpoint=.*@fs.s3a.endpoint=${s3Endpoint}@g" conf/common.properties
sed -i ${txt} "s@^fs.s3a.access.key=.*@fs.s3a.access.key=${s3AccessKey}@g" conf/common.properties
sed -i ${txt} "s@^fs.s3a.secret.key=.*@fs.s3a.secret.key=${s3SecretKey}@g" conf/common.properties
sed -i ${txt} "s@^resource.manager.httpaddress.port=.*@resource.manager.httpaddress.port=${resourceManagerHttpAddressPort}@g" conf/common.properties
sed -i ${txt} "s@^yarn.resourcemanager.ha.rm.ids=.*@yarn.resourcemanager.ha.rm.ids=${yarnHaIps}@g" conf/common.properties
sed -i ${txt} "s@^yarn.application.status.address=.*@yarn.application.status.address=http://${singleYarnIp}:%s/ws/v1/cluster/apps/%s@g" conf/common.properties
sed -i ${txt} "s@^yarn.job.history.status.address=.*@yarn.job.history.status.address=http://${singleYarnIp}:19888/ws/v1/history/mapreduce/jobs/%s@g" conf/common.properties
sed -i ${txt} "s@^sudo.enable=.*@sudo.enable=${sudoEnable}@g" conf/common.properties

# The following configurations may be commented, so ddd #* to ensure sed work correct
# Change configuration in conf/worker.properties
sed -i ${txt} "s@^#*worker.tenant.auto.create=.*@worker.tenant.auto.create=${workerTenantAutoCreate}@g" conf/worker.properties
sed -i ${txt} "s@^#*alert.listen.host=.*@alert.listen.host=${alertServer}@g" conf/worker.properties
sed -i ${txt} "s@^#*task.plugin.dir=.*@task.plugin.dir=${installPath}/${taskPluginDir}@g" conf/worker.properties

# Change configuration in conf/alert.properties
sed -i ${txt} "s@^#*alert.plugin.dir=.*@alert.plugin.dir=${installPath}/${alertPluginDir}@g" conf/alert.properties

# Change configuration in conf/application-api.properties
sed -i ${txt} "s@^#*server.port=.*@server.port=${apiServerPort}@g" conf/application-api.properties

# Change configuration in conf/registry.properties
sed -i ${txt} "s@^#*registry.plugin.dir=.*@registry.plugin.dir=${installPath}/${registryPluginDir}@g" conf/registry.properties
sed -i ${txt} "s@^#*registry.plugin.name=.*@registry.plugin.name=${registryPluginName}@g" conf/registry.properties
sed -i ${txt} "s@^#*registry.servers=.*@registry.servers=${registryServers}@g" conf/registry.properties

# 2.create directory
echo "2.create directory"

if [ ! -d $installPath ];then
  sudo mkdir -p $installPath
  sudo chown -R $deployUser:$deployUser $installPath
fi

# 3.scp resources
echo "3.scp resources"
sh ${workDir}/script/scp-hosts.sh
if [ $? -eq 0 ]
then
	echo 'scp copy completed'
else
	echo 'scp copy failed to exit'
	exit 1
fi


# 4.stop server
echo "4.stop server"
sh ${workDir}/script/stop-all.sh


# 5.delete zk node
echo "5.delete zk node"

sh ${workDir}/script/remove-zk-node.sh $zkRoot


# 6.startup
echo "6.startup"
sh ${workDir}/script/start-all.sh
