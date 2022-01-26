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
A example workflow for task switch.

This example will create four task in single workflow, with three shell task and one switch task. Task switch
have one upstream which we declare explicit with syntax `parent >> switch`, and two downstream automatically
set dependence by switch task by passing parameter `condition`. The graph of this workflow like:
                      --> switch_child_1
                    /
parent -> switch ->
                    \
                      --> switch_child_2
.
"""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.shell import Shell
from pydolphinscheduler.tasks.switch import Branch, Default, Switch, SwitchCondition

with ProcessDefinition(
    name="task_switch_example", tenant="tenant_exists", param={"var": "1"}
) as pd:
    parent = Shell(name="parent", command="echo parent")
    switch_child_1 = Shell(name="switch_child_1", command="echo switch_child_1")
    switch_child_2 = Shell(name="switch_child_2", command="echo switch_child_2")
    switch_condition = SwitchCondition(
        Branch(condition="${var} > 1", task=switch_child_1),
        Default(task=switch_child_2),
    )

    switch = Switch(name="switch", condition=switch_condition)
    parent >> switch
    pd.submit()
# [end workflow_declare]
