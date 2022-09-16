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
"""A example workflow for task openmldb."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.openmldb import OpenMLDB

sql = """USE demo_db;
set @@job_timeout=200000;
LOAD DATA INFILE 'file:///tmp/train_sample.csv'
INTO TABLE talkingdata OPTIONS(mode='overwrite');
"""

with ProcessDefinition(
    name="task_openmldb_example",
    tenant="tenant_exists",
) as pd:
    task_openmldb = OpenMLDB(
        name="task_openmldb",
        zookeeper="127.0.0.1:2181",
        zookeeper_path="/openmldb",
        execute_mode="offline",
        sql=sql,
    )

    pd.run()
# [end workflow_declare]
