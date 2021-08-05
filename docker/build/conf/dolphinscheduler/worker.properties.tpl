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

# worker listener port
#worker.listen.port=1234

# worker execute thread number to limit task instances in parallel
worker.exec.threads=${WORKER_EXEC_THREADS}

# worker heartbeat interval, the unit is second
worker.heartbeat.interval=${WORKER_HEARTBEAT_INTERVAL}

# worker host weight to dispatch tasks, default value 100
worker.host.weight=${WORKER_HOST_WEIGHT}

# worker tenant auto create
worker.tenant.auto.create=true

# worker max cpuload avg, only higher than the system cpu load average, worker server can be dispatched tasks. default value -1: the number of cpu cores * 2
worker.max.cpuload.avg=${WORKER_MAX_CPULOAD_AVG}

# worker reserved memory, only lower than system available memory, worker server can be dispatched tasks. default value 0.3, the unit is G
worker.reserved.memory=${WORKER_RESERVED_MEMORY}

# default worker groups separated by comma, like 'worker.groups=default,test'
worker.groups=${WORKER_GROUPS}

# alert server listen host
alert.listen.host=${ALERT_LISTEN_HOST}
