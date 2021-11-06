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

"""Test Task class function."""

from unittest.mock import patch

from pydolphinscheduler.core.task import TaskParams, TaskRelation, Task


def test_task_params_to_dict():
    """Test TaskParams object function to_dict."""
    raw_script = "test_task_params_to_dict"
    expect = {
        "resourceList": [],
        "localParams": [],
        "rawScript": raw_script,
        "dependence": {},
        "conditionResult": TaskParams.DEFAULT_CONDITION_RESULT,
        "waitStartTimeout": {},
    }
    task_param = TaskParams(raw_script=raw_script)
    assert task_param.to_dict() == expect


def test_task_relation_to_dict():
    """Test TaskRelation object function to_dict."""
    pre_task_code = 123
    post_task_code = 456
    expect = {
        "name": "",
        "preTaskCode": pre_task_code,
        "postTaskCode": post_task_code,
        "preTaskVersion": 1,
        "postTaskVersion": 1,
        "conditionType": 0,
        "conditionParams": {},
    }
    task_param = TaskRelation(
        pre_task_code=pre_task_code, post_task_code=post_task_code
    )
    assert task_param.to_dict() == expect


def test_task_to_dict():
    """Test Task object function to_dict."""
    code = 123
    version = 1
    name = "test_task_to_dict"
    task_type = "test_task_to_dict_type"
    raw_script = "test_task_params_to_dict"
    expect = {
        "code": code,
        "name": name,
        "version": version,
        "description": None,
        "delayTime": 0,
        "taskType": task_type,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "rawScript": raw_script,
            "dependence": {},
            "conditionResult": {"successNode": [""], "failedNode": [""]},
            "waitStartTimeout": {},
        },
        "flag": "YES",
        "taskPriority": "MEDIUM",
        "workerGroup": "default",
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
        task = Task(name=name, task_type=task_type, task_params=TaskParams(raw_script))
        assert task.to_dict() == expect
