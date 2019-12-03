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

import time
import sys
from kazoo.client import KazooClient

class ZkClient:
    def __init__(self):
        self.zk = KazooClient(hosts=sys.argv[1])
        self.zk.start()
    def del_node(self):
        self.zk.delete(sys.argv[2], recursive=True)
        print('deleted success')
    def __del__(self):
        self.zk.stop()
if __name__ == '__main__':
    zkclient = ZkClient()
    zkclient.del_node()
    time.sleep(2)
