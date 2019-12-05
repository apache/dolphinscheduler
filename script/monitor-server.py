#!/usr/bin/env python
# -*- coding:utf-8 -*-
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

'''
1, yum install pip
yum -y install python-pip

2, pip install kazoo
pip install kazoo

or

3, conda install kazoo
conda install -c conda-forge kazoo

run script and parameter description：
nohup python -u monitor_server.py /data1_1T/dolphinscheduler 192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181 /dolphinscheduler/masters /dolphinscheduler/workers> monitor_server.log 2>&1 &
the parameters are as follows:
/data1_1T/dolphinscheduler : the value comes from the installPath in install.sh
192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181 : the value comes from zkQuorum in install.sh
the value comes from zkWorkers in install.sh
/dolphinscheduler/masters : the value comes from zkMasters in install.sh
/dolphinscheduler/workers : the value comes from zkWorkers in install.sh
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
        # hosts configuration zk address cluster
	    self.zk = KazooClient(hosts=zookeepers)
	    self.zk.start()

    # read configuration files and assemble them into a dictionary
    def read_file(self,path):
        with open(path, 'r') as f:
            dict = {}
            for line in f.readlines():
                arr = line.strip().split('=')
                if (len(arr) == 2):
                    dict[arr[0]] = arr[1]
            return dict

    # get the ip address according to hostname
    def get_ip_by_hostname(self,hostname):
        return socket.gethostbyname(hostname)

    # restart server
    def restart_server(self,inc):
        config_dict = self.read_file(install_path + '/conf/config/run_config.conf')

        master_list = config_dict.get('masters').split(',')
        print master_list
        master_list = list(map(lambda item : self.get_ip_by_hostname(item),master_list))

        worker_list = config_dict.get('workers').split(',')
	    print worker_list
        worker_list = list(map(lambda item: self.get_ip_by_hostname(item), worker_list))

        ssh_port = config_dict.get("sshPort")
        print ssh_port

        if (self.zk.exists(masters_zk_path)):
            zk_master_list = []
            zk_master_nodes = self.zk.get_children(masters_zk_path)
            for zk_master_node in zk_master_nodes:
                zk_master_list.append(zk_master_node.split('_')[0])
            restart_master_list = list(set(master_list) - set(zk_master_list))
            if (len(restart_master_list) != 0):
                for master in restart_master_list:
                    print("master " + self.get_ip_by_hostname(master) + " server has down")
                    os.system('ssh -p ' + ssh_port + ' ' + self.get_ip_by_hostname(master) + ' sh ' + install_path + '/bin/dolphinscheduler-daemon.sh start master-server')

        if (self.zk.exists(workers_zk_path)):
            zk_worker_list = []
            zk_worker_nodes = self.zk.get_children(workers_zk_path)
            for zk_worker_node in zk_worker_nodes:
                zk_worker_list.append(zk_worker_node.split('_')[0])
            restart_worker_list = list(set(worker_list) - set(zk_worker_list))
            if (len(restart_worker_list) != 0):
                for worker in restart_worker_list:
                    print("worker " + self.get_ip_by_hostname(worker) + " server has down")
                    os.system('ssh -p ' + ssh_port + ' ' + self.get_ip_by_hostname(worker) + ' sh ' + install_path + '/bin/dolphinscheduler-daemon.sh start worker-server')

        print(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        schedule.enter(inc, 0, self.restart_server, (inc,))
    # default parameter 60s
    def main(self,inc=60):
        # the enter four parameters are: interval event, priority (sequence for simultaneous execution of two events arriving at the same time), function triggered by the call，
        # the argument to the trigger function (tuple form)
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