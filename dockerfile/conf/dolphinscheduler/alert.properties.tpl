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
#alert type is EMAIL/SMS
alert.type=EMAIL

# alter msg template, default is html template
#alert.template=html
# mail server configuration
mail.protocol=SMTP
mail.server.host=${MAIL_SERVER_HOST}
mail.server.port=${MAIL_SERVER_PORT}
mail.sender=${MAIL_SENDER}
mail.user=${MAIL_USER}
mail.passwd=${MAIL_PASSWD}
# TLS
mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS_ENABLE}
# SSL
mail.smtp.ssl.enable=${MAIL_SMTP_SSL_ENABLE}
mail.smtp.ssl.trust=${MAIL_SMTP_SSL_TRUST}

#xls file path,need create if not exist
xls.file.path=${XLS_FILE_PATH}

# Enterprise WeChat configuration
enterprise.wechat.enable=${ENTERPRISE_WECHAT_ENABLE}
enterprise.wechat.corp.id=${ENTERPRISE_WECHAT_CORP_ID}
enterprise.wechat.secret=${ENTERPRISE_WECHAT_SECRET}
enterprise.wechat.agent.id=${ENTERPRISE_WECHAT_AGENT_ID}
enterprise.wechat.users=${ENTERPRISE_WECHAT_USERS}
enterprise.wechat.token.url=https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=$corpId&corpsecret=$secret
enterprise.wechat.push.url=https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=$token
enterprise.wechat.team.send.msg={\"toparty\":\"$toParty\",\"agentid\":\"$agentId\",\"msgtype\":\"text\",\"text\":{\"content\":\"$msg\"},\"safe\":\"0\"}
enterprise.wechat.user.send.msg={\"touser\":\"$toUser\",\"agentid\":\"$agentId\",\"msgtype\":\"markdown\",\"markdown\":{\"content\":\"$msg\"}}



