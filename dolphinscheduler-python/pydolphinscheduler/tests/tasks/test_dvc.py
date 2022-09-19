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

"""Test Task Dvc."""
from unittest.mock import patch

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.tasks.dvc import DVCDownload, DVCInit, DvcTaskType, DVCUpload

repository = "git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example.git"


def test_dvc_init_get_define():
    """Test task dvc init function get_define."""
    name = "test_dvc_init"
    dvc_store_url = "~/dvc_data"

    code = 123
    version = 1
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": TaskType.DVC,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "dvcTaskType": DvcTaskType.INIT,
            "dvcRepository": repository,
            "dvcStoreUrl": dvc_store_url,
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
        dvc_init = DVCInit(name, repository, dvc_store_url)
        assert dvc_init.get_define() == expect


def test_dvc_upload_get_define():
    """Test task dvc upload function get_define."""
    name = "test_dvc_upload"
    data_path_in_dvc_repository = "iris"
    data_path_in_worker = "~/source/iris"
    version = "v1"
    message = "upload iris data v1"

    code = 123
    version = 1
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": TaskType.DVC,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "dvcTaskType": DvcTaskType.UPLOAD,
            "dvcRepository": repository,
            "dvcDataLocation": data_path_in_dvc_repository,
            "dvcLoadSaveDataPath": data_path_in_worker,
            "dvcVersion": version,
            "dvcMessage": message,
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
        dvc_upload = DVCUpload(
            name,
            repository=repository,
            data_path_in_dvc_repository=data_path_in_dvc_repository,
            data_path_in_worker=data_path_in_worker,
            version=version,
            message=message,
        )
        assert dvc_upload.get_define() == expect


def test_dvc_download_get_define():
    """Test task dvc download function get_define."""
    name = "test_dvc_upload"
    data_path_in_dvc_repository = "iris"
    data_path_in_worker = "~/target/iris"
    version = "v1"

    code = 123
    version = 1
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": TaskType.DVC,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "dvcTaskType": DvcTaskType.DOWNLOAD,
            "dvcRepository": repository,
            "dvcDataLocation": data_path_in_dvc_repository,
            "dvcLoadSaveDataPath": data_path_in_worker,
            "dvcVersion": version,
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
        dvc_download = DVCDownload(
            name,
            repository=repository,
            data_path_in_dvc_repository=data_path_in_dvc_repository,
            data_path_in_worker=data_path_in_worker,
            version=version,
        )
        assert dvc_download.get_define() == expect
