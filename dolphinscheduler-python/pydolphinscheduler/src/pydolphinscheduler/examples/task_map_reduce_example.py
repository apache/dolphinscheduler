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
"""A example workflow for task mr."""

from pydolphinscheduler.core.engine import ProgramType
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.map_reduce import MR

with ProcessDefinition(name="task_map_reduce_example", tenant="tenant_exists") as pd:
    task = MR(
        name="task_mr",
        main_class="wordcount",
        main_package="hadoop-mapreduce-examples-3.3.1.jar",
        program_type=ProgramType.JAVA,
        main_args="/dolphinscheduler/tenant_exists/resources/file.txt /output/ds",
    )
    pd.run()
# [end workflow_declare]
