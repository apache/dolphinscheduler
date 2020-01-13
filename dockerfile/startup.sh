#!/bin/bash
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

set -e
	echo "start postgresql service"
    /etc/init.d/postgresql restart
    echo "create user and init db"
    sudo -u postgres psql <<'ENDSSH'
create user root with password 'root@123';
create database dolphinscheduler owner root;
grant all privileges on database dolphinscheduler to root;
\q
ENDSSH
    echo "import sql data"
    /opt/dolphinscheduler/script/create-dolphinscheduler.sh

/opt/zookeeper/bin/zkServer.sh restart

sleep 90

echo "start api-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop api-server
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start api-server



echo "start master-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop master-server
sh /opt/dolphinscheduler/script/remove-zk-node.sh  /dolphinscheduler/masters
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start master-server

echo "start worker-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop worker-server
sh /opt/dolphinscheduler/script/remove-zk-node.sh  /dolphinscheduler/workers
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start worker-server


echo "start logger-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop logger-server
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start logger-server


echo "start alert-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop alert-server
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start alert-server


while true
do
 	sleep 101
done
exec "$@"
