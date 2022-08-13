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

"""Test Task SageMaker."""
import json
from unittest.mock import patch

import pytest

from pydolphinscheduler.tasks.sagemaker import SageMaker

sagemaker_request_json = json.dumps(
    {
        "ParallelismConfiguration": {"MaxParallelExecutionSteps": 1},
        "PipelineExecutionDescription": "test Pipeline",
        "PipelineExecutionDisplayName": "AbalonePipeline",
        "PipelineName": "AbalonePipeline",
        "PipelineParameters": [
            {"Name": "ProcessingInstanceType", "Value": "ml.m4.xlarge"},
            {"Name": "ProcessingInstanceCount", "Value": "2"},
        ],
    },
    indent=2,
)


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {"sagemaker_request_json": sagemaker_request_json},
            {
                "sagemakerRequestJson": sagemaker_request_json,
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        )
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_property_task_params(mock_code_version, attr, expect):
    """Test task sagemaker task property."""
    task = SageMaker("test-sagemaker-task-params", **attr)
    assert expect == task.task_params


def test_sagemaker_get_define():
    """Test task sagemaker function get_define."""
    code = 123
    version = 1
    name = "test_sagemaker_get_define"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "SAGEMAKER",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "sagemakerRequestJson": sagemaker_request_json,
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
        sagemaker = SageMaker(name, sagemaker_request_json)
        assert sagemaker.get_define() == expect
