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

# master execute thread number
master.exec.threads=${MASTER_EXEC_THREADS}

# master execute task number in parallel
master.exec.task.num=${MASTER_EXEC_TASK_NUM}

# master dispatch task number
master.dispatch.task.num=${MASTER_DISPATCH_TASK_NUM}

# master host selector to select a suitable worker, default value: LowerWeight. Optional values include Random, RoundRobin, LowerWeight
master.host.selector=${MASTER_HOST_SELECTOR}

# master heartbeat interval
master.heartbeat.interval=${MASTER_HEARTBEAT_INTERVAL}

# master commit task retry times
master.task.commit.retryTimes=${MASTER_TASK_COMMIT_RETRYTIMES}

# master commit task interval
master.task.commit.interval=${MASTER_TASK_COMMIT_INTERVAL}

# only less than cpu avg load, master server can work. default value -1: the number of cpu cores * 2
master.max.cpuload.avg=${MASTER_MAX_CPULOAD_AVG}

# only larger than reserved memory, master server can work. default value 0.3, the unit is G
master.reserved.memory=${MASTER_RESERVED_MEMORY}
