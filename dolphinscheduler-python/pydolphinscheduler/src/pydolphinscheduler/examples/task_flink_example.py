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
"""A example workflow for task flink."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.flink import DeployMode, Flink, ProgramType

with ProcessDefinition(name="task_flink_example", tenant="tenant_exists") as pd:
    task = Flink(
        name="task_flink",
        main_class="org.apache.flink.streaming.examples.wordcount.WordCount",
        main_package="WordCount.jar",
        program_type=ProgramType.JAVA,
        deploy_mode=DeployMode.LOCAL,
    )
    pd.run()
# [end workflow_declare]
