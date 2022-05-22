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

"""
This example show you how to create workflows in batch mode.

After this example run, we will create 10 workflows named `workflow:<workflow_num>`, and with 3 tasks
named `task:<task_num>-workflow:<workflow_num>` in each workflow. Task shape as below

task:1-workflow:1 -> task:2-workflow:1 -> task:3-workflow:1

Each workflow is linear since we set `IS_CHAIN=True`, you could change task to parallel by set it to `False`.
"""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.shell import Shell

NUM_WORKFLOWS = 10
NUM_TASKS = 5
# Make sure your tenant exists in your operator system
TENANT = "exists_tenant"
# Whether task should dependent on pre one or not
# False will create workflow with independent task, while True task will dependent on pre-task and dependence
# link like `pre_task -> current_task -> next_task`, default True
IS_CHAIN = True

for wf in range(0, NUM_WORKFLOWS):
    workflow_name = f"workflow:{wf}"

    with ProcessDefinition(name=workflow_name, tenant=TENANT) as pd:
        for t in range(0, NUM_TASKS):
            task_name = f"task:{t}-{workflow_name}"
            command = f"echo This is task {task_name}"
            task = Shell(name=task_name, command=command)

            if IS_CHAIN and t > 0:
                pre_task_name = f"task:{t-1}-{workflow_name}"
                pd.get_one_task_by_name(pre_task_name) >> task

        # We just submit workflow and task definition without set schedule time or run it manually
        pd.submit()
