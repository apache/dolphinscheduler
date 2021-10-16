"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""

from resource_management import *


def dolphin_env():
    import params

    Directory(params.dolphin_pidfile_dir,
              mode=0777,
              owner=params.dolphin_user,
              group=params.dolphin_group,
              create_parents=True
              )
    Directory(params.dolphin_log_dir,
              mode=0777,
              owner=params.dolphin_user,
              group=params.dolphin_group,
              create_parents=True
              )
    Directory(params.dolphin_conf_dir,
              mode=0777,
              owner=params.dolphin_user,
              group=params.dolphin_group,
              create_parents=True
              )

    Directory(params.dolphin_common_map['data.basedir.path'],
              mode=0777,
              owner=params.dolphin_user,
              group=params.dolphin_group,
              create_parents=True
              )


    File(format(params.dolphin_env_path),
         mode=0777,
         content=InlineTemplate(params.dolphin_env_content),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )


    File(format(params.dolphin_bin_dir + "/dolphinscheduler-daemon.sh"),
               mode=0755,
               content=Template("dolphin-daemon.sh.j2"),
               owner=params.dolphin_user,
               group=params.dolphin_group
               )

    File(format(params.dolphin_conf_dir + "/master.properties"),
         mode=0755,
         content=Template("master.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/worker.properties"),
         mode=0755,
         content=Template("worker.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )


    File(format(params.dolphin_conf_dir + "/alert.properties"),
         mode=0755,
         content=Template("alert.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/datasource.properties"),
         mode=0755,
         content=Template("datasource.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/application-api.properties"),
         mode=0755,
         content=Template("application-api.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/common.properties"),
         mode=0755,
         content=Template("common.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/quartz.properties"),
         mode=0755,
         content=Template("quartz.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )

    File(format(params.dolphin_conf_dir + "/zookeeper.properties"),
         mode=0755,
         content=Template("zookeeper.properties.j2"),
         owner=params.dolphin_user,
         group=params.dolphin_group
         )
