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
"""A example workflow for task pytorch."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.pytorch import Pytorch

with ProcessDefinition(
    name="task_pytorch_example",
    tenant="tenant_exists",
) as pd:

    # run project with existing environment
    task_existing_env = Pytorch(
        name="task_existing_env",
        script="main.py",
        script_params="--dry-run --no-cuda",
        project_path="https://github.com/pytorch/examples#mnist",
        python_command="/home/anaconda3/envs/pytorch/bin/python3",
    )

    # run project with creating conda environment
    task_conda_env = Pytorch(
        name="task_conda_env",
        script="main.py",
        script_params="--dry-run --no-cuda",
        project_path="https://github.com/pytorch/examples#mnist",
        is_create_environment=True,
        python_env_tool="conda",
        requirements="requirements.txt",
        conda_python_version="3.7",
    )

    # run project with creating virtualenv environment
    task_virtualenv_env = Pytorch(
        name="task_virtualenv_env",
        script="main.py",
        script_params="--dry-run --no-cuda",
        project_path="https://github.com/pytorch/examples#mnist",
        is_create_environment=True,
        python_env_tool="virtualenv",
        requirements="requirements.txt",
    )

    pd.submit()
# [end workflow_declare]
