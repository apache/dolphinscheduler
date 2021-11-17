# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

r"""
A tutorial example take you to experience pydolphinscheduler.

After tutorial.py file submit to Apache DolphinScheduler server a DAG would be create,
and workflow DAG graph as below:

                  --> task_child_one
                /                    \
task_parent -->                        -->  task_union
                \                   /
                  --> task_child_two

it will instantiate and run all the task it have.
"""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.shell import Shell

with ProcessDefinition(
    name="tutorial",
    schedule="0 0 0 * * ? *",
    start_time="2021-01-01",
    tenant="tenant_exists",
) as pd:
    task_parent = Shell(name="task_parent", command="echo hello pydolphinscheduler")
    task_child_one = Shell(name="task_child_one", command="echo 'child one'")
    task_child_two = Shell(name="task_child_two", command="echo 'child two'")
    task_union = Shell(name="task_union", command="echo union")

    task_group = [task_child_one, task_child_two]
    task_parent.set_downstream(task_group)

    task_union << task_group

    pd.run()
