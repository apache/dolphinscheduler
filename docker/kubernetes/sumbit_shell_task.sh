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

curl -X POST -u admin:dolphinscheduler123 http://localhost:12345/dolphinscheduler/tenant/create -d 'tenantCode=ds&tenantName=ds&queueId=1&description='

curl -X POST -u admin:dolphinscheduler123 http://127.0.0.1:12345/dolphinscheduler/users/update -d 'userName=admin&userPassword=&tenantId=1&email=xxx%40qq.com&queue=default&phone=&id=1'

curl -X POST -u admin:dolphinscheduler123 http://127.0.0.1:12345/dolphinscheduler/projects/create -d 'projectName=test&description='

curl -X POST -u admin:dolphinscheduler123 http://127.0.0.1:12345/dolphinscheduler/projects/test/process/save -d 'processDefinitionJson=%7B%22globalParams%22%3A%5B%5D%2C%22tasks%22%3A%5B%7B%22type%22%3A%22SHELL%22%2C%22id%22%3A%22tasks-1383%22%2C%22name%22%3A%22shell+task%22%2C%22params%22%3A%7B%22resourceList%22%3A%5B%5D%2C%22localParams%22%3A%5B%5D%2C%22rawScript%22%3A%22echo+dolphinscheduler+shell+task+test%22%7D%2C%22description%22%3A%22shell+task%22%2C%22timeout%22%3A%7B%22strategy%22%3A%22%22%2C%22interval%22%3Anull%2C%22enable%22%3Afalse%7D%2C%22runFlag%22%3A%22NORMAL%22%2C%22conditionResult%22%3A%7B%22successNode%22%3A%5B%22%22%5D%2C%22failedNode%22%3A%5B%22%22%5D%7D%2C%22dependence%22%3A%7B%7D%2C%22maxRetryTimes%22%3A%220%22%2C%22retryInterval%22%3A%221%22%2C%22taskInstancePriority%22%3A%22MEDIUM%22%2C%22workerGroup%22%3A%22default%22%2C%22preTasks%22%3A%5B%5D%7D%5D%2C%22tenantId%22%3A-1%2C%22timeout%22%3A0%7D&name=dag+test&description=&locations=%7B%22tasks-1383%22%3A%7B%22name%22%3A%22shell+task%22%2C%22targetarr%22%3A%22%22%2C%22nodenumber%22%3A%220%22%2C%22x%22%3A135%2C%22y%22%3A44%7D%7D&connects=%5B%5D'