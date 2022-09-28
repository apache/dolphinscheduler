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

"""Task SageMaker."""

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task


class SageMaker(Task):
    """Task SageMaker object, declare behavior for SageMaker task to dolphinscheduler.

    :param name: A unique, meaningful string for the SageMaker task.
    :param sagemaker_request_json: Request parameters of StartPipelineExecution，
        see also `AWS API
        <https://docs.aws.amazon.com/sagemaker/latest/APIReference/API_StartPipelineExecution.html>`_

    """

    _task_custom_attr = {
        "sagemaker_request_json",
    }

    def __init__(self, name: str, sagemaker_request_json: str, *args, **kwargs):
        super().__init__(name, TaskType.SAGEMAKER, *args, **kwargs)
        self.sagemaker_request_json = sagemaker_request_json
