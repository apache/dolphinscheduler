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

# worker execute thread number
worker.exec.threads=${WORKER_EXEC_THREADS}

# worker heartbeat interval
worker.heartbeat.interval=${WORKER_HEARTBEAT_INTERVAL}

# only less than cpu avg load, worker server can work. default value -1: the number of cpu cores * 2
worker.max.cpuload.avg=${WORKER_MAX_CPULOAD_AVG}

# only larger than reserved memory, worker server can work. default value 0.3, the unit is G
worker.reserved.memory=${WORKER_RESERVED_MEMORY}

# default worker groups separated by comma, like 'worker.groups=default,test'
worker.groups=${WORKER_GROUPS}
