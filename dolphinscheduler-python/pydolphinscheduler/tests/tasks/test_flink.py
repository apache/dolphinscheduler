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

"""Test Task Flink."""

from unittest.mock import patch

from pydolphinscheduler.tasks.flink import DeployMode, Flink, FlinkVersion, ProgramType


@patch(
    "pydolphinscheduler.core.engine.Engine.get_resource_info",
    return_value=({"id": 1, "name": "test"}),
)
def test_flink_get_define(mock_resource):
    """Test task flink function get_define."""
    code = 123
    version = 1
    name = "test_flink_get_define"
    main_class = "org.apache.flink.test_main_class"
    main_package = "test_main_package"
    program_type = ProgramType.JAVA
    deploy_mode = DeployMode.LOCAL

    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "FLINK",
        "taskParams": {
            "mainClass": main_class,
            "mainJar": {
                "id": 1,
            },
            "programType": program_type,
            "deployMode": deploy_mode,
            "flinkVersion": FlinkVersion.LOW_VERSION,
            "slot": 1,
            "parallelism": 1,
            "taskManager": 2,
            "jobManagerMemory": "1G",
            "taskManagerMemory": "2G",
            "appName": None,
            "mainArgs": None,
            "others": None,
            "localParams": [],
            "resourceList": [],
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
        task = Flink(name, main_class, main_package, program_type, deploy_mode)
        assert task.get_define() == expect
