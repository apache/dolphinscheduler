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
A example workflow for task condition.

This example will create five task in single workflow, with four shell task and one condition task. Task
condition have one upstream which we declare explicit with syntax `parent >> condition`, and three downstream
automatically set dependence by condition task by passing parameter `condition`. The graph of this workflow
like:
pre_task_success_1 ->
                      \
pre_task_success_2 ->  --> conditions -> end
                      /
pre_task_fail      ->
.
"""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.condition import FAILURE, SUCCESS, And, Conditions
from pydolphinscheduler.tasks.shell import Shell

with ProcessDefinition(name="task_conditions_example", tenant="tenant_exists") as pd:
    condition_pre_task_1 = Shell(
        name="pre_task_success_1", command="echo pre_task_success_1"
    )
    condition_pre_task_2 = Shell(
        name="pre_task_success_2", command="echo pre_task_success_2"
    )
    condition_pre_task_3 = Shell(name="pre_task_fail", command="echo pre_task_fail")
    cond_operator = And(
        And(
            SUCCESS(condition_pre_task_1, condition_pre_task_2),
            FAILURE(condition_pre_task_3),
        ),
    )

    end = Shell(name="end", command="echo parent")

    condition = Conditions(name="conditions", condition=cond_operator)
    condition >> end
    pd.submit()
