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

"""Task Kubernetes."""
from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task


class Kubernetes(Task):
    """Task Kubernetes object, declare behavior for Kubernetes task to dolphinscheduler.

    :param name: task name
    :param image: the registry url for image.
    :param namespace: the namespace for running k8s task.
    :param min_cpu_cores: min CPU requirement for running k8s task.
    :param min_memory_space: min memory requirement for running k8s task.
    :param params_map: It is a local user-defined parameter for K8S task.
    """

    _task_custom_attr = {
        "image",
        "namespace",
        "min_cpu_cores",
        "min_memory_space",
    }

    def __init__(
        self,
        name: str,
        image: str,
        namespace: str,
        min_cpu_cores: float,
        min_memory_space: float,
        *args,
        **kwargs
    ):
        super().__init__(name, TaskType.KUBERNETES, *args, **kwargs)
        self.image = image
        self.namespace = namespace
        self.min_cpu_cores = min_cpu_cores
        self.min_memory_space = min_memory_space
