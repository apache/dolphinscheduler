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

"""Test Task OpenMLDB."""
from unittest.mock import patch

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.tasks.openmldb import OpenMLDB


def test_openmldb_get_define():
    """Test task openmldb function get_define."""
    zookeeper = "127.0.0.1:2181"
    zookeeper_path = "/openmldb"
    execute_mode = "offline"

    sql = """USE demo_db;
    set @@job_timeout=200000;
    LOAD DATA INFILE 'file:///tmp/train_sample.csv'
    INTO TABLE talkingdata OPTIONS(mode='overwrite');
    """

    code = 123
    version = 1
    name = "test_openmldb_get_define"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": TaskType.OPENMLDB,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "zk": zookeeper,
            "zkPath": zookeeper_path,
            "executeMode": execute_mode,
            "sql": sql,
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
        openmldb = OpenMLDB(name, zookeeper, zookeeper_path, execute_mode, sql)
        assert openmldb.get_define() == expect
