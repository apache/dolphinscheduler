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

# server port
server.port=12345

# session config
server.servlet.session.timeout=7200

# servlet config
server.servlet.context-path=/dolphinscheduler/

# time zone
spring.jackson.time-zone=GMT+8

# file size limit for upload
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB

# enable response compression
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml

# max http post size
server.jetty.max-http-form-post-size=5000000

# messages encoding
spring.messages.encoding=UTF-8

# i18n classpath folder, file prefix messages. if have many files, use "," seperator
spring.messages.basename=i18n/messages

# Authentication types (supported types: PASSWORD)
security.authentication.type=PASSWORD

# Traffic control, if you turn on this config, the maximum number of request/s will be limited.
# global max request number per second
# default tenant-level max request number
#traffic.control.global.switch=true
#traffic.control.max.global.qps.rate=500
#traffic.control.tenant.switch=true
#traffic.control.default.tenant.qps.rate=10
#traffic.control.customize.tenant.qps.rate={'tenant1':11,'tenant2':20}

#============================================================================
# LDAP Config
# mock ldap server from https://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/
#============================================================================
# admin userId
#security.authentication.ldap.user.admin=read-only-admin
# ldap server config
#ldap.urls=ldap://ldap.forumsys.com:389/
#ldap.base.dn=dc=example,dc=com
#ldap.username=cn=read-only-admin,dc=example,dc=com
#ldap.password=password
#ldap.user.identity.attribute=uid
#ldap.user.email.attribute=mail
