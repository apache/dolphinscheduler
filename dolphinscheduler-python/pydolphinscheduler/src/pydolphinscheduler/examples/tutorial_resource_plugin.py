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
A tutorial example take you to experience pydolphinscheduler resource plugin.

After tutorial_resource_plugin.py file submit to Apache DolphinScheduler server a DAG would be create,
and workflow DAG graph as below:

                  --> task_child_one
                /                    \
task_parent -->                        -->  task_union
                \                    /
                  --> task_child_two

Resource plug-ins can be defined in workflows and tasks

task_parent uses local resource plugin.
task_child_one uses local resource plugin.
task_child_two uses local resource plugin.
task_union does not use resource plug-ins

it will instantiate and run all the task it have.
"""

from pydolphinscheduler.constants import ResourcePluginType

# [start tutorial_resource_plugin]
# [start package_import]
# Import ProcessDefinition object to define your workflow attributes
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.resources_plugin import ResourcePlugin

# Import task Shell object cause we would create some shell tasks later
from pydolphinscheduler.tasks.shell import Shell

# [end package_import]

# [start workflow_declare]
with ProcessDefinition(
    name="tutorial_resource_plugin",
    schedule="0 0 0 * * ? *",
    start_time="2021-01-01",
    tenant="tenant_exists",
    resource_plugin=ResourcePlugin(
        type=ResourcePluginType.LOCAL,
        prefix="/opt/",
    ),
) as process_definition:
    # [end workflow_declare]
    # [start task_declare]
    task_parent = Shell(
        name="task_parent",
        command="parent.zsh",
        resource_plugin=ResourcePlugin(
            type=ResourcePluginType.LOCAL,
            prefix="/opt/",
        ),
    )

    task_child_one = Shell(
        name="task_child_one",
        command="child_one.sh",
        resource_plugin=ResourcePlugin(
            type=ResourcePluginType.LOCAL,
            prefix="/opt/",
        ),
    )

    task_child_two = Shell(
        name="task_child_two",
        command="child_two.sh",
        resource_plugin=ResourcePlugin(
            type=ResourcePluginType.LOCAL,
            prefix="/opt/",
        ),
    )
    task_union = Shell(
        name="task_union",
        command="echo union",
    )
    # [end task_declare]

    # [start task_relation_declare]
    task_group = [task_child_one, task_child_two]
    task_parent.set_downstream(task_group)

    task_union << task_group
    # [end task_relation_declare]

    # [start submit_or_run]
    process_definition.run()
    # [end submit_or_run]
# [end tutorial_resource_plugin]
