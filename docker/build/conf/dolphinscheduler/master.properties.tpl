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

# master listen port
#master.listen.port=5678

# master execute thread number to limit process instances in parallel
master.exec.threads=${MASTER_EXEC_THREADS}

# master execute task number in parallel per process instance
master.exec.task.num=${MASTER_EXEC_TASK_NUM}

# master dispatch task number per batch
master.dispatch.task.num=${MASTER_DISPATCH_TASK_NUM}

# master host selector to select a suitable worker, default value: LowerWeight. Optional values include Random, RoundRobin, LowerWeight
master.host.selector=${MASTER_HOST_SELECTOR}

# master heartbeat interval, the unit is second
master.heartbeat.interval=${MASTER_HEARTBEAT_INTERVAL}

# master commit task retry times
master.task.commit.retryTimes=${MASTER_TASK_COMMIT_RETRYTIMES}

# master commit task interval, the unit is millisecond
master.task.commit.interval=${MASTER_TASK_COMMIT_INTERVAL}

# master max cpuload avg, only higher than the system cpu load average, master server can schedule. default value -1: the number of cpu cores * 2
master.max.cpuload.avg=${MASTER_MAX_CPULOAD_AVG}

# master reserved memory, only lower than system available memory, master server can schedule. default value 0.3, the unit is G
master.reserved.memory=${MASTER_RESERVED_MEMORY}
