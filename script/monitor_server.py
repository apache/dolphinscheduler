#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author:qiaozhanwei

'''
yum 安装pip
yum -y install python-pip

pip install kazoo 安装
conda install -c conda-forge kazoo 安装

运行脚本及参数说明：
nohup python -u monitor_server.py /data1_1T/escheduler 192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181 /escheduler/masters /escheduler/workers> monitor_server.log 2>&1 &
参数说明如下:
/data1_1T/escheduler的值来自install.sh中的installPath
192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181的值来自install.sh中的zkQuorum
/escheduler/masters的值来自install.sh中的zkMasters
/escheduler/workers的值来自install.sh中的zkWorkers
'''
import sys
import socket
import os
import sched
import time
from datetime import datetime
from kazoo.client import KazooClient

schedule = sched.scheduler(time.time, time.sleep)

class ZkClient:
    def __init__(self):
        # hosts配置zk地址集群
	    self.zk = KazooClient(hosts=zookeepers)
	    self.zk.start()

    # 读取配置文件，组装成字典
    def read_file(self,path):
        with open(path, 'r') as f:
            dict = {}
            for line in f.readlines():
                arr = line.strip().split('=')
                if (len(arr) == 2):
                    dict[arr[0]] = arr[1]
            return dict

    # 根据hostname获取ip地址
    def get_ip_by_hostname(self,hostname):
        return socket.gethostbyname(hostname)

    # 重启服务
    def restart_server(self,inc):
        config_dict = self.read_file(install_path + '/conf/config/run_config.conf')

        master_list = config_dict.get('masters').split(',')
        print master_list
        master_list = list(map(lambda item : self.get_ip_by_hostname(item),master_list))

        worker_list = config_dict.get('workers').split(',')
	print worker_list
        worker_list = list(map(lambda item: self.get_ip_by_hostname(item), worker_list))

        if (self.zk.exists(masters_zk_path)):
            zk_master_list = []
            zk_master_nodes = self.zk.get_children(masters_zk_path)
            for zk_master_node in zk_master_nodes:
                zk_master_list.append(zk_master_node.split('_')[0])
            restart_master_list = list(set(master_list) - set(zk_master_list))
            if (len(restart_master_list) != 0):
                for master in restart_master_list:
                    print("master " + self.get_ip_by_hostname(master) + " 服务已经掉了")
                    os.system('ssh ' + self.get_ip_by_hostname(master) + ' sh ' + install_path + '/bin/escheduler-daemon.sh start master-server')

        if (self.zk.exists(workers_zk_path)):
            zk_worker_list = []
            zk_worker_nodes = self.zk.get_children(workers_zk_path)
            for zk_worker_node in zk_worker_nodes:
                zk_worker_list.append(zk_worker_node.split('_')[0])
            restart_worker_list = list(set(worker_list) - set(zk_worker_list))
            if (len(restart_worker_list) != 0):
                for worker in restart_worker_list:
                    print("worker " + self.get_ip_by_hostname(worker) + " 服务已经掉了")
                    os.system('ssh  ' + self.get_ip_by_hostname(worker) + ' sh ' + install_path + '/bin/escheduler-daemon.sh start worker-server')

        print(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        schedule.enter(inc, 0, self.restart_server, (inc,))
    # 默认参数60s
    def main(self,inc=60):
        # enter四个参数分别为：间隔事件、优先级（用于同时间到达的两个事件同时执行时定序）、被调用触发的函数，
        # 给该触发函数的参数（tuple形式）
        schedule.enter(0, 0, self.restart_server, (inc,))
        schedule.run()
if __name__ == '__main__':
    if (len(sys.argv) < 4):
        print('please input install_path,zookeepers,masters_zk_path and worker_zk_path')
    install_path = sys.argv[1]
    zookeepers = sys.argv[2]
    masters_zk_path = sys.argv[3]
    workers_zk_path = sys.argv[4]
    zkClient = ZkClient()
    zkClient.main(300)
