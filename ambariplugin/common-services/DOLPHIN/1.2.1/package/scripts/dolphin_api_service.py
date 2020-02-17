# -*- coding: utf-8 -*-

import time
from resource_management import *

from dolphin_env import dolphin_env


class DolphinApiService(Script):
    def install(self, env):
        import params
        env.set_params(params)
        self.install_packages(env)
        Execute(('chmod', '-R', '777', params.dolphin_home), user=params.dolphin_user, sudo=True)

    def configure(self, env):
        import params
        params.pika_slave = True
        env.set_params(params)

        dolphin_env()

    def start(self, env):
        import params
        env.set_params(params)
        self.configure(env)

        #init
        init_cmd=format("sh " + params.dolphin_home + "/script/create-dolphinscheduler.sh")
        Execute(init_cmd, user=params.dolphin_user)

        #upgrade
        upgrade_cmd=format("sh " + params.dolphin_home + "/script/upgrade-dolphinscheduler.sh")
        Execute(upgrade_cmd, user=params.dolphin_user)

        start_cmd = format("sh " + params.dolphin_bin_dir + "/dolphinscheduler-daemon.sh start api-server")
        Execute(start_cmd, user=params.dolphin_user)

    def stop(self, env):
        import params
        env.set_params(params)
        stop_cmd = format("sh " + params.dolphin_bin_dir + "/dolphinscheduler-daemon.sh stop api-server")
        Execute(stop_cmd, user=params.dolphin_user)
        time.sleep(5)

    def status(self, env):
        import status_params
        env.set_params(status_params)
        check_process_status(status_params.dolphin_run_dir + "api-server.pid")


if __name__ == "__main__":
    DolphinApiService().execute()
