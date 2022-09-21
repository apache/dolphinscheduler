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

"""Init pydolphinscheduler.tasks package."""

from pydolphinscheduler.tasks.condition import FAILURE, SUCCESS, And, Condition, Or
from pydolphinscheduler.tasks.datax import CustomDataX, DataX
from pydolphinscheduler.tasks.dependent import Dependent
from pydolphinscheduler.tasks.dvc import DVCDownload, DVCInit, DVCUpload
from pydolphinscheduler.tasks.flink import Flink
from pydolphinscheduler.tasks.http import Http
from pydolphinscheduler.tasks.map_reduce import MR
from pydolphinscheduler.tasks.mlflow import (
    MLflowModels,
    MLFlowProjectsAutoML,
    MLFlowProjectsBasicAlgorithm,
    MLFlowProjectsCustom,
)
from pydolphinscheduler.tasks.openmldb import OpenMLDB
from pydolphinscheduler.tasks.procedure import Procedure
from pydolphinscheduler.tasks.python import Python
from pydolphinscheduler.tasks.pytorch import Pytorch
from pydolphinscheduler.tasks.sagemaker import SageMaker
from pydolphinscheduler.tasks.shell import Shell
from pydolphinscheduler.tasks.spark import Spark
from pydolphinscheduler.tasks.sql import Sql
from pydolphinscheduler.tasks.sub_process import SubProcess
from pydolphinscheduler.tasks.switch import Branch, Default, Switch, SwitchCondition

__all__ = [
    "Condition",
    "DataX",
    "CustomDataX",
    "Dependent",
    "DVCInit",
    "DVCUpload",
    "DVCDownload",
    "Flink",
    "Http",
    "MR",
    "OpenMLDB",
    "MLFlowProjectsBasicAlgorithm",
    "MLFlowProjectsCustom",
    "MLFlowProjectsAutoML",
    "MLflowModels",
    "Procedure",
    "Python",
    "Pytorch",
    "Shell",
    "Spark",
    "Sql",
    "SubProcess",
    "Switch",
    "SageMaker",
]
