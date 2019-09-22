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
