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

# file size limit for upload
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB

# post content
server.jetty.max-http-post-size=5000000

# i18n
spring.messages.encoding=UTF-8

#i18n classpath folder , file prefix messages， if have many files, use "," seperator
spring.messages.basename=i18n/messages

# Authentication types (supported types: PASSWORD)
security.authentication.type=PASSWORD




