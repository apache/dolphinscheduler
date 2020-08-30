# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/bash
# current path
esc_basepath=$(cd `dirname $0`; pwd)

menu(){
        cat <<END
=================================================
        1.CentOS6 Installation
        2.CentOS7 Installation
        3.Ubuntu Installation
        4.Exit
=================================================
END
}


# create a file and configure nginx
dolphinschedulerConf(){

    E_host='$host'
    E_remote_addr='$remote_addr'
    E_proxy_add_x_forwarded_for='$proxy_add_x_forwarded_for'
    E_http_upgrade='$http_upgrade'
    echo "
        server {
            listen       $1;# access port
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;
            location / {
            root   ${esc_basepath}/dist; # static file directory
            index  index.html index.html;
            }
            location /dolphinscheduler {
            proxy_pass $2; # interface address
            proxy_set_header Host $E_host;
            proxy_set_header X-Real-IP $E_remote_addr;
            proxy_set_header x_real_ipP $E_remote_addr;
            proxy_set_header remote_addr $E_remote_addr;
            proxy_set_header X-Forwarded-For $E_proxy_add_x_forwarded_for;
            proxy_http_version 1.1;
            proxy_connect_timeout 300s;
            proxy_read_timeout 300s;
            proxy_send_timeout 300s;
            proxy_set_header Upgrade $E_http_upgrade;
            proxy_set_header Connection "upgrade";
            }
            #error_page  404              /404.html;
            # redirect server error pages to the static page /50x.html
            #
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
            root   /usr/share/nginx/html;
            }
        }
    " >> /etc/nginx/conf.d/dolphinscheduler.conf

}

ubuntu(){
    # update source
    apt-get update

    # install nginx
    apt-get install -y nginx

    # config nginx
    dolphinschedulerConf $1 $2

    # startup nginx
    /etc/init.d/nginx start
    sleep 1
    if [ $? -ne 0 ];then
        /etc/init.d/nginx start
    fi
    nginx -s reload
}

centos7(){

    rpm -Uvh http://nginx.org/packages/centos/7/noarch/RPMS/nginx-release-centos-7-0.el7.ngx.noarch.rpm
    yum install -y nginx

    # config nginx
    dolphinschedulerConf $1 $2

    # solve 0.0.0.0:8888 problem
    yum -y install policycoreutils-python
    semanage port -a -t http_port_t -p tcp $esc_proxy

    # open front access port
    firewall-cmd --zone=public --add-port=$esc_proxy/tcp --permanent

    # startup nginx
    systemctl start nginx
    sleep 1
    if [ $? -ne 0 ];then
        systemctl start nginx
    fi
    nginx -s reload

    # set SELinux parameters
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
    # temporary effect
    setenforce 0

}


centos6(){

    rpm -ivh http://nginx.org/packages/centos/6/noarch/RPMS/nginx-release-centos-6-0.el6.ngx.noarch.rpm

    # install nginx
    yum install nginx -y

    # config nginx
    dolphinschedulerConf $1 $2

    # startup nginx
    /etc/init.d/nginx start
    sleep 1
	if [ $? -ne 0 ];then
        /etc/init.d/nginx start
    fi
    nginx -s reload

    # set SELinux parameters
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

    # temporary effect
    setenforce 0

}

function main(){
	echo "Welcome to thedolphinscheduler front-end deployment script, which is currently only supported by front-end deployment scripts : CentOS and Ubuntu"
	echo "Please execute in the dolphinscheduler-ui directory"

	#To be compatible with MacOS and Linux
	if [[ "$OSTYPE" == "darwin"* ]]; then
    		# Mac OSX
    		echo "Easy Scheduler ui install not support Mac OSX operating system"
    		exit 1
	elif [[ "$OSTYPE" == "linux-gnu" ]]; then
   		# linux
		echo "linux"
	elif [[ "$OSTYPE" == "cygwin" ]]; then
    		# POSIX compatibility layer and Linux environment emulation for Windows
    		echo "Easy Scheduler ui not support Windows operating system"
    		exit 1
	elif [[ "$OSTYPE" == "msys" ]]; then
    		# Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    		echo "Easy Scheduler ui not support Windows operating system"
    		exit 1
	elif [[ "$OSTYPE" == "win32" ]]; then
    		echo "Easy Scheduler ui not support Windows operating system"
    		exit 1
	elif [[ "$OSTYPE" == "freebsd"* ]]; then
    		# ...
    		echo "freebsd"
	else
    		# Unknown.
    		echo "Operating system unknown, please tell us(submit issue) for better service"
    		exit 1
	fi


	# config front-end access ports
	read -p "Please enter the nginx proxy port, do not enter, the default is 8888 :" esc_proxy_port
	if [ -z "${esc_proxy_port}" ];then
    	esc_proxy_port="8888"
	fi

	read -p "Please enter the api server proxy ip, you must enter, for example: 192.168.xx.xx :" esc_api_server_ip
	if [ -z "${esc_api_server_ip}" ];then
		echo "api server proxy ip can not be empty."
		exit 1
	fi

	read -p "Please enter the api server proxy port, do not enter, the default is 12345:" esc_api_server_port
	if [ -z "${esc_api_server_port}" ];then
		esc_api_server_port="12345"
	fi

	# api server backend address
	esc_api_server="http://$esc_api_server_ip:$esc_api_server_port"

	# local ip address
	esc_ipaddr=$(ip a | grep inet | grep -v inet6 | grep -v 127 | sed 's/^[ \t]*//g' | cut -d ' ' -f2 | head -n 1 | awk -F '/' '{print $1}')

	# Prompt message
	menu

	read -p "Please enter the installation number(1|2|3|4)：" num

   	case $num in
        	1)
			centos6 ${esc_proxy_port} ${esc_api_server}
                	;;
       		2)
                	centos7 ${esc_proxy_port} ${esc_api_server}
                	;;
        	3)
			ubuntu ${esc_proxy_port} ${esc_api_server}
                	;;
		4)
			echo $"Usage :sh $0"
                	exit 1
			;;
        	*)
                	echo $"Usage :sh $0"
                	exit 1
	esac
	echo "Please visit the browser：http://${esc_ipaddr}:${esc_proxy_port}"

}

main
