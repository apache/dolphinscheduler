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


import sys
from resource_management import *
from resource_management.core.logger import Logger
from resource_management.libraries.functions import default

Logger.initialize_logger()
reload(sys)
sys.setdefaultencoding('utf-8')

# server configurations
config = Script.get_config()

# conf_dir = "/etc/"
dolphin_home = "/opt/soft/dolphinscheduler"
dolphin_conf_dir = dolphin_home + "/conf"
dolphin_log_dir = dolphin_home + "/logs"
dolphin_bin_dir = dolphin_home + "/bin"
dolphin_lib_jars = dolphin_home + "/lib/*"
dolphin_pidfile_dir = "/opt/soft/run/dolphinscheduler"

rmHosts = default("/clusterHostInfo/rm_host", [])

# dolphin-env
dolphin_env_map = {}
dolphin_env_map.update(config['configurations']['dolphin-env'])

# which user to install and admin dolphin scheduler
dolphin_user = dolphin_env_map['dolphin.user']
dolphin_group = dolphin_env_map['dolphin.group']

# .dolphinscheduler_env.sh
dolphin_env_path = dolphin_conf_dir + '/env/dolphinscheduler_env.sh'
dolphin_env_content = dolphin_env_map['dolphinscheduler-env-content']

# database config
dolphin_database_config = {}
dolphin_database_config['dolphin_database_type'] = dolphin_env_map['dolphin.database.type']
dolphin_database_config['dolphin_database_username'] = dolphin_env_map['dolphin.database.username']
dolphin_database_config['dolphin_database_password'] = dolphin_env_map['dolphin.database.password']
if 'mysql' == dolphin_database_config['dolphin_database_type']:
    dolphin_database_config['dolphin_database_driver'] = 'com.mysql.jdbc.Driver'
    dolphin_database_config['driverDelegateClass'] = 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate'
    dolphin_database_config['dolphin_database_url'] = 'jdbc:mysql://' + dolphin_env_map['dolphin.database.host'] \
                                                      + ':' + dolphin_env_map['dolphin.database.port'] \
                                                      + '/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8'
else:
    dolphin_database_config['dolphin_database_driver'] = 'org.postgresql.Driver'
    dolphin_database_config['driverDelegateClass'] = 'org.quartz.impl.jdbcjobstore.PostgreSQLDelegate'
    dolphin_database_config['dolphin_database_url'] = 'jdbc:postgresql://' + dolphin_env_map['dolphin.database.host'] \
                                                      + ':' + dolphin_env_map['dolphin.database.port'] \
                                                      + '/dolphinscheduler'





# application-alert.properties
dolphin_alert_map = {}
wechat_push_url = 'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=$token'
wechat_token_url = 'https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=$corpId&corpsecret=$secret'
wechat_team_send_msg = '{\"toparty\":\"$toParty\",\"agentid\":\"$agentId\",\"msgtype\":\"text\",\"text\":{\"content\":\"$msg\"},\"safe\":\"0\"}'
wechat_user_send_msg = '{\"touser\":\"$toUser\",\"agentid\":\"$agentId\",\"msgtype\":\"markdown\",\"markdown\":{\"content\":\"$msg\"}}'

dolphin_alert_config_map = config['configurations']['dolphin-alert']

if dolphin_alert_config_map['enterprise.wechat.enable']:
    dolphin_alert_map['enterprise.wechat.push.ur'] = wechat_push_url
    dolphin_alert_map['enterprise.wechat.token.url'] = wechat_token_url
    dolphin_alert_map['enterprise.wechat.team.send.msg'] = wechat_team_send_msg
    dolphin_alert_map['enterprise.wechat.user.send.msg'] = wechat_user_send_msg

dolphin_alert_map.update(dolphin_alert_config_map)



# application-api.properties
dolphin_app_api_map = {}
dolphin_app_api_map.update(config['configurations']['dolphin-application-api'])


# common.properties
dolphin_common_map = {}

if 'yarn-site' in config['configurations'] and \
        'yarn.resourcemanager.webapp.address' in config['configurations']['yarn-site']:
    yarn_resourcemanager_webapp_address = config['configurations']['yarn-site']['yarn.resourcemanager.webapp.address']
    yarn_application_status_address = 'http://' + yarn_resourcemanager_webapp_address + '/ws/v1/cluster/apps/%s'
    dolphin_common_map['yarn.application.status.address'] = yarn_application_status_address

rmHosts = default("/clusterHostInfo/rm_host", [])
if len(rmHosts) > 1:
    dolphin_common_map['yarn.resourcemanager.ha.rm.ids'] = ','.join(rmHosts)
else:
    dolphin_common_map['yarn.resourcemanager.ha.rm.ids'] = ''

dolphin_common_map_tmp = config['configurations']['dolphin-common']
data_basedir_path = dolphin_common_map_tmp['data.basedir.path']
dolphin_common_map['dolphinscheduler.env.path'] = dolphin_env_path
dolphin_common_map.update(config['configurations']['dolphin-common'])

# datasource.properties
dolphin_datasource_map = {}
dolphin_datasource_map['spring.datasource.type'] = 'com.alibaba.druid.pool.DruidDataSource'
dolphin_datasource_map['spring.datasource.driver-class-name'] = dolphin_database_config['dolphin_database_driver']
dolphin_datasource_map['spring.datasource.url'] = dolphin_database_config['dolphin_database_url']
dolphin_datasource_map['spring.datasource.username'] = dolphin_database_config['dolphin_database_username']
dolphin_datasource_map['spring.datasource.password'] = dolphin_database_config['dolphin_database_password']
dolphin_datasource_map.update(config['configurations']['dolphin-datasource'])

# master.properties
dolphin_master_map = config['configurations']['dolphin-master']

# quartz.properties
dolphin_quartz_map = {}
dolphin_quartz_map['org.quartz.jobStore.driverDelegateClass'] = dolphin_database_config['driverDelegateClass']
dolphin_quartz_map.update(config['configurations']['dolphin-quartz'])

# worker.properties
dolphin_worker_map = config['configurations']['dolphin-worker']

# zookeeper.properties
dolphin_zookeeper_map={}
zookeeperHosts = default("/clusterHostInfo/zookeeper_hosts", [])
if len(zookeeperHosts) > 0 and "clientPort" in config['configurations']['zoo.cfg']:
    clientPort = config['configurations']['zoo.cfg']['clientPort']
    zookeeperPort = ":" + clientPort + ","
    dolphin_zookeeper_map['zookeeper.quorum'] = zookeeperPort.join(zookeeperHosts) + ":" + clientPort
dolphin_zookeeper_map.update(config['configurations']['dolphin-zookeeper'])
if 'spring.servlet.multipart.max-file-size' in dolphin_app_api_map:
    file_size = dolphin_app_api_map['spring.servlet.multipart.max-file-size']
    dolphin_app_api_map['spring.servlet.multipart.max-file-size'] = file_size + "MB"
if 'spring.servlet.multipart.max-request-size' in dolphin_app_api_map:
    request_size = dolphin_app_api_map['spring.servlet.multipart.max-request-size']
    dolphin_app_api_map['spring.servlet.multipart.max-request-size'] = request_size + "MB"


