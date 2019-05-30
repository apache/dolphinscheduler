#!/bin/bash
# 当前路径
esc_basepath=$(cd `dirname $0`; pwd)

menu(){
        cat <<END
=================================================
        1.CentOS6安装
        2.CentOS7安装
        3.Ubuntu安装
        4.退出
=================================================
END
}


# 创建文件并配置nginx
eschedulerConf(){

    E_host='$host'
    E_remote_addr='$remote_addr'
    E_proxy_add_x_forwarded_for='$proxy_add_x_forwarded_for'
    E_http_upgrade='$http_upgrade'
    echo "
        server {
            listen       $1;# 访问端口
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;
            location / {
            root   ${esc_basepath}/dist; # 静态文件目录
            index  index.html index.html;
            }
            location /escheduler {
            proxy_pass $2; # 接口地址
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
    " >> /etc/nginx/conf.d/escheduler.conf

}

ubuntu(){
    #更新源
    apt-get update

    #安装nginx
    apt-get install -y nginx

    # 配置nginx
    eschedulerConf $1 $2

    # 启动nginx
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

    # 配置nginx
    eschedulerConf $1 $2

    # 解决 0.0.0.0:8888 问题
    yum -y install policycoreutils-python
    semanage port -a -t http_port_t -p tcp $esc_proxy

    # 开放前端访问端口
    firewall-cmd --zone=public --add-port=$esc_proxy/tcp --permanent

    # 启动nginx
    systemctl start nginx
    sleep 1
    if [ $? -ne 0 ];then
        systemctl start nginx
    fi
    nginx -s reload

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
    # 临时生效
    setenforce 0

}


centos6(){

    rpm -ivh http://nginx.org/packages/centos/6/noarch/RPMS/nginx-release-centos-6-0.el6.ngx.noarch.rpm

    # install nginx
    yum install nginx -y

    # 配置nginx
    eschedulerConf $1 $2

    # 启动nginx
    /etc/init.d/nginx start
    sleep 1
	if [ $? -ne 0 ];then
        /etc/init.d/nginx start
    fi
    nginx -s reload

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

    # 临时生效
    setenforce 0

}

function main(){
	echo "欢迎使用easy scheduler前端部署脚本,目前前端部署脚本仅支持CentOS,Ubuntu"
	echo "请在 escheduler-ui 目录下执行"

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


	# 配置前端访问端口
	read -p "请输入nginx代理端口，不输入，则默认8888 :" esc_proxy_port
	if [ -z "${esc_proxy_port}" ];then
    	esc_proxy_port="8888"
	fi

	read -p "请输入api server代理ip,必须输入，例如：192.168.xx.xx :" esc_api_server_ip
	if [ -z "${esc_api_server_ip}" ];then
		echo "api server代理ip不能为空."
		exit 1
	fi

	read -p "请输入api server代理端口,不输入，则默认12345 :" esc_api_server_port
	if [ -z "${esc_api_server_port}" ];then
		esc_api_server_port="12345"
	fi

	# api server后端地址
	esc_api_server="http://$esc_api_server_ip:$esc_api_server_port"

	# 本机ip地址
	esc_ipaddr=$(ip a | grep inet | grep -v inet6 | grep -v 127 | sed 's/^[ \t]*//g' | cut -d ' ' -f2 | head -n 1 | awk -F '/' '{print $1}')

	# 提示信息
	menu

	read -p "请输入安装编号(1|2|3|4)：" num

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
	echo "请浏览器访问：http://${esc_ipaddr}:${esc_proxy_port}"

}

main
