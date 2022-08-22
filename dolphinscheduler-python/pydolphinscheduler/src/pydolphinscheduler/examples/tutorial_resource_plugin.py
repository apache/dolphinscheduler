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

Resource plug-ins can be defined in workflows and tasks

it will instantiate and run all the task it have.
"""

# [start tutorial_resource_plugin]
# [start package_import]
# Import ProcessDefinition object to define your workflow attributes
from pydolphinscheduler.core.process_definition import ProcessDefinition

# Import task Shell object cause we would create some shell tasks later
from pydolphinscheduler.resources_plugin.github import Github
from pydolphinscheduler.tasks.shell import Shell

# [end package_import]

# [start workflow_declare]
with ProcessDefinition(
    name="tutorial_resource_plugin",
    schedule="0 0 0 * * ? *",
    start_time="2021-01-01",
    tenant="tenant_exists",
    resource_plugin=Github(
        prefix="https://github.com/xdu-chenrj/codeforces/blob/main/1715",
        # access_token="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",
    ),
) as process_definition:
    # [end workflow_declare]
    # [start task_declare]
    # file = "resource.sh"
    # path = Path("/tmp").joinpath(file)
    # with open(str(path), "w") as f:
    #     f.write("echo tutorial resource plugin")
    task_parent = Shell(
        name="local-resource-example",
        command="union.sh",
    )
    print(task_parent.task_params)
    # [end task_declare]

    # [start submit_or_run]
    process_definition.run()
    # [end submit_or_run]
# [end tutorial_resource_plugin]
