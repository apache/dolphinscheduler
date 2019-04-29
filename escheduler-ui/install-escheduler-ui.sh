#!/bin/bash

# 当前路径
esc_basepath=$(cd `dirname $0`; pwd)


echo "欢迎使用easy scheduler前端部署脚本,目前前端部署脚本仅支持Centos"
echo "请在 escheduler-ui 目录下执行"

# 配置前端访问端口
esc_proxy="8888"

# 配置代理后端接口
esc_proxy_port="http://192.168.xx.xx:12345"

# 本机ip
esc_ipaddr='127.0.0.1'

esc_ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')


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

# 区分版本
version=`cat /etc/redhat-release|sed -r 's/.* ([0-9]+)\..*/\1/'`


echo "========================================================================配置信息======================================================================="

echo "前端访问端口：${esc_proxy}"
echo "后端代理接口地址：${esc_proxy_port}"
echo "静态文件地址：${esc_basepath}/dist"
echo "当前路径：${esc_basepath}"
echo "本机ip：${esc_ipaddr}"

echo "========================================================================配置信息======================================================================="
echo ""


# 创建文件并配置nginx
eschedulerConf(){

    E_host='$host'
    E_remote_addr='$remote_addr'
    E_proxy_add_x_forwarded_for='$proxy_add_x_forwarded_for'
    E_http_upgrade='$http_upgrade'
    echo "
        server {
            listen       $esc_proxy;# 访问端口
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;
            location / {
            root   ${esc_basepath}/dist; # 静态文件目录
            index  index.html index.html;
            }
            location /escheduler {
            proxy_pass ${esc_proxy_port}; # 接口地址
            proxy_set_header Host $E_host;
            proxy_set_header X-Real-IP $E_remote_addr;
            proxy_set_header x_real_ipP $E_remote_addr;
            proxy_set_header remote_addr $E_remote_addr;
            proxy_set_header X-Forwarded-For $E_proxy_add_x_forwarded_for;
            proxy_http_version 1.1;
            proxy_connect_timeout 4s;
            proxy_read_timeout 30s;
            proxy_send_timeout 12s;
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


centos7(){
    # nginx是否安装
    sudo rpm -Uvh http://nginx.org/packages/centos/7/noarch/RPMS/nginx-release-centos-7-0.el7.ngx.noarch.rpm
    sudo yum install -y nginx
    echo "nginx 安装成功"

    # 配置nginx
    eschedulerConf

    # 解决 0.0.0.0:8888 问题
    yum -y install policycoreutils-python
    semanage port -a -t http_port_t -p tcp $esc_proxy

    # 开放前端访问端口
    firewall-cmd --zone=public --add-port=$esc_proxy/tcp --permanent

    # 重启防火墙
    firewall-cmd --reload

    # 启动nginx
    systemctl start nginx

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
    # 临时生效
    setenforce 0

}


centos6(){
    # yum
    E_basearch='$basearch'
    E_releasever='$releasever'
    echo "
    [nginx]
    name=nginx repo
    baseurl=http://nginx.org/packages/centos/$E_releasever/$E_basearch/
    gpgcheck=0
    enabled=1
    " >> /etc/yum.repos.d/nginx.repo

    # install nginx
    yum install nginx -y

    # 配置nginx
    eschedulerConf

    # 防火墙
    E_iptables=`lsof -i:$esc_proxy | wc -l`
    if [ "$E_iptables" -gt "0" ];then
    # 已开启端口防火墙重启
    service iptables restart
    else
    # 未开启防火墙添加端口再重启
    iptables -I INPUT 5 -i eth0 -p tcp --dport $esc_proxy -m state --state NEW,ESTABLISHED -j ACCEPT
    service iptables save
    service iptables restart
    fi

    # start
    /etc/init.d/nginx start

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

    # 临时生效
    setenforce 0

}

# centos 6
if [[ $version -eq 6 ]]; then
    centos6
fi

# centos 7
if [[ $version -eq 7 ]]; then	
    centos7
fi


echo "请浏览器访问：http://${esc_ipaddr}:${esc_proxy}"

