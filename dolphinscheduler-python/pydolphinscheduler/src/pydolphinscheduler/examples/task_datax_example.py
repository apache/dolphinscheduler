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
"""
A example workflow for task datax.

This example will create a workflow named `task_datax`.
`task_datax` is true workflow define and run task task_datax.
You can create data sources `first_mysql` and `first_mysql` through UI.
It creates a task to synchronize datax from the source database to the target database.
"""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.datax import CustomDataX, DataX

# datax json template
JSON_TEMPLATE = {
    "job": {
        "content": [
            {
                "reader": {
                    "name": "mysqlreader",
                    "parameter": {
                        "username": "usr",
                        "password": "pwd",
                        "column": ["id", "name", "code", "description"],
                        "splitPk": "id",
                        "connection": [
                            {
                                "table": ["source_table"],
                                "jdbcUrl": ["jdbc:mysql://127.0.0.1:3306/source_db"],
                            }
                        ],
                    },
                },
                "writer": {
                    "name": "mysqlwriter",
                    "parameter": {
                        "writeMode": "insert",
                        "username": "usr",
                        "password": "pwd",
                        "column": ["id", "name"],
                        "connection": [
                            {
                                "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/target_db",
                                "table": ["target_table"],
                            }
                        ],
                    },
                },
            }
        ],
        "setting": {
            "errorLimit": {"percentage": 0, "record": 0},
            "speed": {"channel": 1, "record": 1000},
        },
    }
}

with ProcessDefinition(
    name="task_datax_example",
    tenant="tenant_exists",
) as pd:
    # This task synchronizes the data in `t_ds_project`
    # of `first_mysql` database to `target_project` of `second_mysql` database.
    # You have to make sure data source named `first_mysql` and `second_mysql` exists
    # in your environment.
    task1 = DataX(
        name="task_datax",
        datasource_name="first_mysql",
        datatarget_name="second_mysql",
        sql="select id, name, code, description from source_table",
        target_table="target_table",
    )

    # You can custom json_template of datax to sync data. This task create a new
    # datax job same as task1, transfer record from `first_mysql` to `second_mysql`
    task2 = CustomDataX(name="task_custom_datax", json=str(JSON_TEMPLATE))
    pd.run()
# [end workflow_declare]
