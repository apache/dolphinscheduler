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
"""A example workflow for task kubernetes."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks import Kubernetes

with ProcessDefinition(
    name="task_kubernetes_example",
    tenant="tenant_exists",
) as pd:
    task_k8s = Kubernetes(
        name="task_k8s",
        image="ds-dev",
        namespace=str({"name": "default", "cluster": "lab"}),
        min_cpu_cores=2.0,
        min_memory_space=10.0,
    )
    pd.submit()
# [end workflow_declare]
