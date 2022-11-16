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

"""Test Task shell."""

from unittest.mock import patch

from pydolphinscheduler.tasks.kubernetes import Kubernetes


def test_kubernetes_get_define():
    """Test task kubernetes function get_define."""
    code = 123
    version = 1
    name = "test_kubernetes_get_define"
    image = "ds-dev"
    namespace = str({"name": "default", "cluster": "lab"})
    minCpuCores = 2.0
    minMemorySpace = 10.0

    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "K8S",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "image": image,
            "namespace": namespace,
            "minCpuCores": minCpuCores,
            "minMemorySpace": minMemorySpace,
            "dependence": {},
            "conditionResult": {"successNode": [""], "failedNode": [""]},
            "waitStartTimeout": {},
        },
        "flag": "YES",
        "taskPriority": "MEDIUM",
        "workerGroup": "default",
        "environmentCode": None,
        "failRetryTimes": 0,
        "failRetryInterval": 1,
        "timeoutFlag": "CLOSE",
        "timeoutNotifyStrategy": None,
        "timeout": 0,
    }
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        k8s = Kubernetes(name, image, namespace, minCpuCores, minMemorySpace)
        assert k8s.get_define() == expect
