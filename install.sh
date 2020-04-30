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
sed -i ${txt} "s#spring.datasource.driver-class-name.*#spring.datasource.driver-class-name=${datasourceDriverClassname}#g" conf/datasource.properties
sed -i ${txt} "s#spring.datasource.url.*#spring.datasource.url=jdbc:${dbtype}://${dbhost}/${dbname}?characterEncoding=UTF-8\&allowMultiQueries=true#g" conf/datasource.properties
sed -i ${txt} "s#spring.datasource.username.*#spring.datasource.username=${username}#g" conf/datasource.properties
sed -i ${txt} "s#spring.datasource.password.*#spring.datasource.password=${password}#g" conf/datasource.properties

sed -i ${txt} "s#fs.defaultFS.*#fs.defaultFS=${defaultFS}#g" conf/common.properties
sed -i ${txt} "s#fs.s3a.endpoint.*#fs.s3a.endpoint=${s3Endpoint}#g" conf/common.properties
sed -i ${txt} "s#fs.s3a.access.key.*#fs.s3a.access.key=${s3AccessKey}#g" conf/common.properties
sed -i ${txt} "s#fs.s3a.secret.key.*#fs.s3a.secret.key=${s3SecretKey}#g" conf/common.properties
sed -i ${txt} "s#yarn.resourcemanager.ha.rm.ids.*#yarn.resourcemanager.ha.rm.ids=${yarnHaIps}#g" conf/common.properties
sed -i ${txt} "s#yarn.application.status.address.*#yarn.application.status.address=http://${singleYarnIp}:8088/ws/v1/cluster/apps/%s#g" conf/common.properties
sed -i ${txt} "s#hdfs.root.user.*#hdfs.root.user=${hdfsRootUser}#g" conf/common.properties
sed -i ${txt} "s#resource.upload.path.*#resource.upload.path=${resourceUploadPath}#g" conf/common.properties
sed -i ${txt} "s#resource.storage.type.*#resource.storage.type=${resourceStorageType}#g" conf/common.properties
sed -i ${txt} "s#hadoop.security.authentication.startup.state.*#hadoop.security.authentication.startup.state=${kerberosStartUp}#g" conf/common.properties
sed -i ${txt} "s#java.security.krb5.conf.path.*#java.security.krb5.conf.path=${krb5ConfPath}#g" conf/common.properties
sed -i ${txt} "s#login.user.keytab.username.*#login.user.keytab.username=${keytabUserName}#g" conf/common.properties
sed -i ${txt} "s#login.user.keytab.path.*#login.user.keytab.path=${keytabPath}#g" conf/common.properties
sed -i ${txt} "s#zookeeper.quorum.*#zookeeper.quorum=${zkQuorum}#g" conf/zookeeper.properties
sed -i ${txt} "s#server.port.*#server.port=${apiServerPort}#g" conf/application-api.properties
sed -i ${txt} "s#mail.server.host.*#mail.server.host=${mailServerHost}#g" conf/alert.properties
sed -i ${txt} "s#mail.server.port.*#mail.server.port=${mailServerPort}#g" conf/alert.properties
sed -i ${txt} "s#mail.sender.*#mail.sender=${mailSender}#g" conf/alert.properties
sed -i ${txt} "s#mail.user.*#mail.user=${mailUser}#g" conf/alert.properties
sed -i ${txt} "s#mail.passwd.*#mail.passwd=${mailPassword}#g" conf/alert.properties
sed -i ${txt} "s#mail.smtp.starttls.enable.*#mail.smtp.starttls.enable=${starttlsEnable}#g" conf/alert.properties
sed -i ${txt} "s#mail.smtp.ssl.trust.*#mail.smtp.ssl.trust=${sslTrust}#g" conf/alert.properties
sed -i ${txt} "s#mail.smtp.ssl.enable.*#mail.smtp.ssl.enable=${sslEnable}#g" conf/alert.properties

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