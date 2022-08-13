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
"""A example workflow for task sagemaker."""
import json

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.sagemaker import SageMaker

sagemaker_request_data = {
    "ParallelismConfiguration": {"MaxParallelExecutionSteps": 1},
    "PipelineExecutionDescription": "test Pipeline",
    "PipelineExecutionDisplayName": "AbalonePipeline",
    "PipelineName": "AbalonePipeline",
    "PipelineParameters": [
        {"Name": "ProcessingInstanceType", "Value": "ml.m4.xlarge"},
        {"Name": "ProcessingInstanceCount", "Value": "2"},
    ],
}

with ProcessDefinition(
    name="task_sagemaker_example",
    tenant="tenant_exists",
) as pd:
    task_sagemaker = SageMaker(
        name="task_sagemaker",
        sagemaker_request_json=json.dumps(sagemaker_request_data, indent=2),
    )

    pd.run()
# [end workflow_declare]
