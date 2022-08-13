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

# [start workflow_declare]
r"""
A example workflow for task dependent.

This example will create two workflows named `task_dependent` and `task_dependent_external`.
`task_dependent` is true workflow define and run task dependent, while `task_dependent_external`
define outside workflow and task from dependent.

After this script submit, we would get workflow as below:

task_dependent_external:

task_1
task_2
task_3

task_dependent:

task_dependent(this task dependent on task_dependent_external.task_1 and task_dependent_external.task_2).
"""
from pydolphinscheduler import configuration
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.dependent import And, Dependent, DependentItem, Or
from pydolphinscheduler.tasks.shell import Shell

with ProcessDefinition(
    name="task_dependent_external",
    tenant="tenant_exists",
) as pd:
    task_1 = Shell(name="task_1", command="echo task 1")
    task_2 = Shell(name="task_2", command="echo task 2")
    task_3 = Shell(name="task_3", command="echo task 3")
    pd.submit()

with ProcessDefinition(
    name="task_dependent_example",
    tenant="tenant_exists",
) as pd:
    task = Dependent(
        name="task_dependent",
        dependence=And(
            Or(
                DependentItem(
                    project_name=configuration.WORKFLOW_PROJECT,
                    process_definition_name="task_dependent_external",
                    dependent_task_name="task_1",
                ),
                DependentItem(
                    project_name=configuration.WORKFLOW_PROJECT,
                    process_definition_name="task_dependent_external",
                    dependent_task_name="task_2",
                ),
            )
        ),
    )
    pd.submit()
# [end workflow_declare]
