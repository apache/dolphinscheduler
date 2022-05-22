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
                \                    /
                  --> task_child_two

it will instantiate and run all the task it have.
"""

# [start tutorial]
# [start package_import]
# Import ProcessDefinition object to define your workflow attributes
from pydolphinscheduler.core.process_definition import ProcessDefinition

# Import task Shell object cause we would create some shell tasks later
from pydolphinscheduler.tasks.func_wrap import task

# [end package_import]


# [start task_declare]
@task
def task_parent():
    """First task in this workflow."""
    print("echo hello pydolphinscheduler")


@task
def task_child_one():
    """Child task will be run parallel after task ``task_parent`` finished."""
    print("echo 'child one'")


@task
def task_child_two():
    """Child task will be run parallel after task ``task_parent`` finished."""
    print("echo 'child two'")


@task
def task_union():
    """Last task in this workflow."""
    print("echo union")


# [end task_declare]


# [start workflow_declare]
with ProcessDefinition(
    name="tutorial_decorator",
    schedule="0 0 0 * * ? *",
    start_time="2021-01-01",
    tenant="tenant_exists",
) as pd:
    # [end workflow_declare]

    # [start task_relation_declare]
    task_group = [task_child_one(), task_child_two()]
    task_parent().set_downstream(task_group)

    task_union() << task_group
    # [end task_relation_declare]

    # [start submit_or_run]
    pd.run()
    # [end submit_or_run]
# [end tutorial]
