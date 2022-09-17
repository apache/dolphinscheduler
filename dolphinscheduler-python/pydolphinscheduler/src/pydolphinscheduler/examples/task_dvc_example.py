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
"""A example workflow for task dvc."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks import DVCDownload, DVCInit, DVCUpload

repository = "git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example.git"

with ProcessDefinition(
    name="task_dvc_example",
    tenant="tenant_exists",
) as pd:
    init_task = DVCInit(name="init_dvc", repository=repository, store_url="~/dvc_data")
    upload_task = DVCUpload(
        name="upload_data",
        repository=repository,
        data_path_in_dvc_repository="iris",
        data_path_in_worker="~/source/iris",
        version="v1",
        message="upload iris data v1",
    )

    download_task = DVCDownload(
        name="download_data",
        repository=repository,
        data_path_in_dvc_repository="iris",
        data_path_in_worker="~/target/iris",
        version="v1",
    )

    init_task >> upload_task >> download_task

    pd.run()

# [end workflow_declare]
