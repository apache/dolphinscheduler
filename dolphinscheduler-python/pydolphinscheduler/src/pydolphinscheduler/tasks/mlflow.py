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

"""Task mlflow."""
from copy import deepcopy
from typing import Dict, Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task


class MLflowTaskType(str):
    """MLflow task type."""

    MLFLOW_PROJECTS = "MLflow Projects"
    MLFLOW_MODELS = "MLflow Models"


class MLflowJobType(str):
    """MLflow job type."""

    AUTOML = "AutoML"
    BASIC_ALGORITHM = "BasicAlgorithm"
    CUSTOM_PROJECT = "CustomProject"


class MLflowDeployType(str):
    """MLflow deploy type."""

    MLFLOW = "MLFLOW"
    DOCKER = "DOCKER"


DEFAULT_MLFLOW_TRACKING_URI = "http://127.0.0.1:5000"
DEFAULT_VERSION = "master"


class BaseMLflow(Task):
    """Base MLflow task."""

    mlflow_task_type = None

    _task_custom_attr = {
        "mlflow_tracking_uri",
        "mlflow_task_type",
    }

    _child_task_mlflow_attr = set()

    def __init__(self, name: str, mlflow_tracking_uri: str, *args, **kwargs):
        super().__init__(name, TaskType.MLFLOW, *args, **kwargs)
        self.mlflow_tracking_uri = mlflow_tracking_uri

    @property
    def task_params(self) -> Dict:
        """Return task params."""
        self._task_custom_attr = deepcopy(self._task_custom_attr)
        self._task_custom_attr.update(self._child_task_mlflow_attr)
        return super().task_params


class MLflowModels(BaseMLflow):
    """Task MLflow models object, declare behavior for MLflow models task to dolphinscheduler.

    Deploy machine learning models in diverse serving environments.

    :param name: task name
    :param model_uri: Model-URI of MLflow , support models:/<model_name>/suffix format and runs:/ format.
        See https://mlflow.org/docs/latest/tracking.html#artifact-stores
    :param mlflow_tracking_uri: MLflow tracking server uri, default is http://127.0.0.1:5000
    :param deploy_mode: MLflow deploy mode, support MLFLOW, DOCKER, default is DOCKER
    :param port: deploy port, default is 7000
    """

    mlflow_task_type = MLflowTaskType.MLFLOW_MODELS

    _child_task_mlflow_attr = {
        "deploy_type",
        "deploy_model_key",
        "deploy_port",
    }

    def __init__(
        self,
        name: str,
        model_uri: str,
        mlflow_tracking_uri: Optional[str] = DEFAULT_MLFLOW_TRACKING_URI,
        deploy_mode: Optional[str] = MLflowDeployType.DOCKER,
        port: Optional[int] = 7000,
        *args,
        **kwargs
    ):
        """Init mlflow models task."""
        super().__init__(name, mlflow_tracking_uri, *args, **kwargs)
        self.deploy_type = deploy_mode.upper()
        self.deploy_model_key = model_uri
        self.deploy_port = port


class MLFlowProjectsCustom(BaseMLflow):
    """Task MLflow projects object, declare behavior for MLflow Custom projects task to dolphinscheduler.

    :param name: task name
    :param repository: Repository url of MLflow Project, Support git address and directory on worker.
        If it's in a subdirectory, We add # to support this (same as mlflow run) ,
        for example https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native.
    :param mlflow_tracking_uri: MLflow tracking server uri, default is http://127.0.0.1:5000
    :param experiment_name: MLflow experiment name, default is empty
    :param parameters: MLflow project parameters, default is empty
    :param version: MLflow project version, default is master

    """

    mlflow_task_type = MLflowTaskType.MLFLOW_PROJECTS
    mlflow_job_type = MLflowJobType.CUSTOM_PROJECT

    _child_task_mlflow_attr = {
        "mlflow_job_type",
        "experiment_name",
        "params",
        "mlflow_project_repository",
        "mlflow_project_version",
    }

    def __init__(
        self,
        name: str,
        repository: str,
        mlflow_tracking_uri: Optional[str] = DEFAULT_MLFLOW_TRACKING_URI,
        experiment_name: Optional[str] = "",
        parameters: Optional[str] = "",
        version: Optional[str] = "master",
        *args,
        **kwargs
    ):
        """Init mlflow projects task."""
        super().__init__(name, mlflow_tracking_uri, *args, **kwargs)
        self.mlflow_project_repository = repository
        self.experiment_name = experiment_name
        self.params = parameters
        self.mlflow_project_version = version


class MLFlowProjectsAutoML(BaseMLflow):
    """Task MLflow projects object, declare behavior for AutoML task to dolphinscheduler.

    :param name: task name
    :param data_path: data path of MLflow Project, Support git address and directory on worker.
    :param automl_tool: The AutoML tool used, currently supports autosklearn and flaml.
    :param mlflow_tracking_uri: MLflow tracking server uri, default is http://127.0.0.1:5000
    :param experiment_name: MLflow experiment name, default is empty
    :param model_name: MLflow model name, default is empty
    :param parameters: MLflow project parameters, default is empty

    """

    mlflow_task_type = MLflowTaskType.MLFLOW_PROJECTS
    mlflow_job_type = MLflowJobType.AUTOML

    _child_task_mlflow_attr = {
        "mlflow_job_type",
        "experiment_name",
        "model_name",
        "register_model",
        "data_path",
        "params",
        "automl_tool",
    }

    def __init__(
        self,
        name: str,
        data_path: str,
        automl_tool: Optional[str] = "flaml",
        mlflow_tracking_uri: Optional[str] = DEFAULT_MLFLOW_TRACKING_URI,
        experiment_name: Optional[str] = "",
        model_name: Optional[str] = "",
        parameters: Optional[str] = "",
        *args,
        **kwargs
    ):
        """Init mlflow projects task."""
        super().__init__(name, mlflow_tracking_uri, *args, **kwargs)
        self.data_path = data_path
        self.experiment_name = experiment_name
        self.model_name = model_name
        self.params = parameters
        self.automl_tool = automl_tool.lower()
        self.register_model = bool(model_name)


class MLFlowProjectsBasicAlgorithm(BaseMLflow):
    """Task MLflow projects object, declare behavior for BasicAlgorithm task to dolphinscheduler.

    :param name: task name
    :param data_path: data path of MLflow Project, Support git address and directory on worker.
    :param algorithm: The selected algorithm currently supports LR, SVM, LightGBM and XGboost
            based on scikit-learn form.
    :param mlflow_tracking_uri: MLflow tracking server uri, default is http://127.0.0.1:5000
    :param experiment_name: MLflow experiment name, default is empty
    :param model_name: MLflow model name, default is empty
    :param parameters: MLflow project parameters, default is empty
    :param search_params: Whether to search the parameters, default is empty

    """

    mlflow_job_type = MLflowJobType.BASIC_ALGORITHM
    mlflow_task_type = MLflowTaskType.MLFLOW_PROJECTS

    _child_task_mlflow_attr = {
        "mlflow_job_type",
        "experiment_name",
        "model_name",
        "register_model",
        "data_path",
        "params",
        "algorithm",
        "search_params",
    }

    def __init__(
        self,
        name: str,
        data_path: str,
        algorithm: Optional[str] = "lightgbm",
        mlflow_tracking_uri: Optional[str] = DEFAULT_MLFLOW_TRACKING_URI,
        experiment_name: Optional[str] = "",
        model_name: Optional[str] = "",
        parameters: Optional[str] = "",
        search_params: Optional[str] = "",
        *args,
        **kwargs
    ):
        """Init mlflow projects task."""
        super().__init__(name, mlflow_tracking_uri, *args, **kwargs)
        self.data_path = data_path
        self.experiment_name = experiment_name
        self.model_name = model_name
        self.params = parameters
        self.algorithm = algorithm.lower()
        self.search_params = search_params
        self.register_model = bool(model_name)
