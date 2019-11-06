#! /bin/bash

set -e
if [ `netstat -anop|grep mysql|wc -l` -gt 0 ];then
                echo "MySQL is Running."
else
	MYSQL_ROOT_PWD="root@123"
    ESZ_DB="dolphinscheduler"
	echo "start mysql service"
	chown -R mysql:mysql /var/lib/mysql /var/run/mysqld
	find /var/lib/mysql -type f -exec touch {} \; && service mysql restart $ sleep 10
	if [ ! -f /nohup.out ];then
		echo "setting mysql password"
		mysql --user=root --password=root -e "UPDATE mysql.user set authentication_string=password('$MYSQL_ROOT_PWD') where user='root'; FLUSH PRIVILEGES;"

		echo "setting mysql permission"
		mysql --user=root --password=$MYSQL_ROOT_PWD -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '$MYSQL_ROOT_PWD' WITH GRANT OPTION; FLUSH PRIVILEGES;"
		echo "create dolphinscheduler database"
		mysql --user=root --password=$MYSQL_ROOT_PWD -e "CREATE DATABASE IF NOT EXISTS \`$ESZ_DB\` CHARACTER SET utf8 COLLATE utf8_general_ci; FLUSH PRIVILEGES;"
		echo "import mysql data"
		nohup /opt/dolphinscheduler/script/create-dolphinscheduler.sh &
		sleep 90
	fi

	if [ `mysql --user=root --password=$MYSQL_ROOT_PWD -s -r -e  "SELECT count(TABLE_NAME) FROM information_schema.TABLES WHERE TABLE_SCHEMA='dolphinscheduler';" | grep -v count` -eq 38 ];then
		echo "\`$ESZ_DB\` the number of tables is correct"
	else
		echo "\`$ESZ_DB\` the number of tables is incorrect"
		mysql --user=root --password=$MYSQL_ROOT_PWD  -e "DROP DATABASE \`$ESZ_DB\`;"
		echo "create dolphinscheduler database"
                mysql --user=root --password=$MYSQL_ROOT_PWD -e "CREATE DATABASE IF NOT EXISTS \`$ESZ_DB\` CHARACTER SET utf8 COLLATE utf8_general_ci; FLUSH PRIVILEGES;"
                echo "import mysql data"
                nohup /opt/dolphinscheduler/script/create-dolphinscheduler.sh &
                sleep 90
	fi
fi

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
