#!/bin/bash

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
python /opt/dolphinscheduler/script/del-zk-node.py 127.0.0.1 /dolphinscheduler/masters
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start master-server

echo "start worker-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop worker-server
python /opt/dolphinscheduler/script/del-zk-node.py 127.0.0.1 /dolphinscheduler/workers
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start worker-server


echo "start logger-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop logger-server
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start logger-server


echo "start alert-server"
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh stop alert-server
/opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh start alert-server





echo "start nginx"
/etc/init.d/nginx stop
nginx &


while true
do
 	sleep 101
done
exec "$@"
