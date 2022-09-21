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

"""Test Task MLflow."""
from copy import deepcopy
from unittest.mock import patch

from pydolphinscheduler.tasks.mlflow import (
    MLflowDeployType,
    MLflowJobType,
    MLflowModels,
    MLFlowProjectsAutoML,
    MLFlowProjectsBasicAlgorithm,
    MLFlowProjectsCustom,
    MLflowTaskType,
)

CODE = 123
VERSION = 1
MLFLOW_TRACKING_URI = "http://127.0.0.1:5000"

EXPECT = {
    "code": CODE,
    "version": VERSION,
    "description": None,
    "delayTime": 0,
    "taskType": "MLFLOW",
    "taskParams": {
        "resourceList": [],
        "localParams": [],
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


def test_mlflow_models_get_define():
    """Test task mlflow models function get_define."""
    name = "mlflow_models"
    model_uri = "models:/xgboost_native/Production"
    port = 7001

    expect = deepcopy(EXPECT)
    expect["name"] = name
    task_params = expect["taskParams"]
    task_params["mlflowTrackingUri"] = MLFLOW_TRACKING_URI
    task_params["mlflowTaskType"] = MLflowTaskType.MLFLOW_MODELS
    task_params["deployType"] = MLflowDeployType.DOCKER
    task_params["deployModelKey"] = model_uri
    task_params["deployPort"] = port

    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(CODE, VERSION),
    ):
        task = MLflowModels(
            name=name,
            model_uri=model_uri,
            mlflow_tracking_uri=MLFLOW_TRACKING_URI,
            deploy_mode=MLflowDeployType.DOCKER,
            port=port,
        )
        assert task.get_define() == expect


def test_mlflow_project_custom_get_define():
    """Test task mlflow project custom function get_define."""
    name = ("train_xgboost_native",)
    repository = "https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native"
    mlflow_tracking_uri = MLFLOW_TRACKING_URI
    parameters = "-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9"
    experiment_name = "xgboost"

    expect = deepcopy(EXPECT)
    expect["name"] = name
    task_params = expect["taskParams"]

    task_params["mlflowTrackingUri"] = MLFLOW_TRACKING_URI
    task_params["mlflowTaskType"] = MLflowTaskType.MLFLOW_PROJECTS
    task_params["mlflowJobType"] = MLflowJobType.CUSTOM_PROJECT
    task_params["experimentName"] = experiment_name
    task_params["params"] = parameters
    task_params["mlflowProjectRepository"] = repository
    task_params["mlflowProjectVersion"] = "dev"

    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(CODE, VERSION),
    ):
        task = MLFlowProjectsCustom(
            name=name,
            repository=repository,
            mlflow_tracking_uri=mlflow_tracking_uri,
            parameters=parameters,
            experiment_name=experiment_name,
            version="dev",
        )
        assert task.get_define() == expect


def test_mlflow_project_automl_get_define():
    """Test task mlflow project automl function get_define."""
    name = ("train_automl",)
    mlflow_tracking_uri = MLFLOW_TRACKING_URI
    parameters = "time_budget=30;estimator_list=['lgbm']"
    experiment_name = "automl_iris"
    model_name = "iris_A"
    automl_tool = "flaml"
    data_path = "/data/examples/iris"

    expect = deepcopy(EXPECT)
    expect["name"] = name
    task_params = expect["taskParams"]

    task_params["mlflowTrackingUri"] = MLFLOW_TRACKING_URI
    task_params["mlflowTaskType"] = MLflowTaskType.MLFLOW_PROJECTS
    task_params["mlflowJobType"] = MLflowJobType.AUTOML
    task_params["experimentName"] = experiment_name
    task_params["modelName"] = model_name
    task_params["registerModel"] = bool(model_name)
    task_params["dataPath"] = data_path
    task_params["params"] = parameters
    task_params["automlTool"] = automl_tool

    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(CODE, VERSION),
    ):
        task = MLFlowProjectsAutoML(
            name=name,
            mlflow_tracking_uri=mlflow_tracking_uri,
            parameters=parameters,
            experiment_name=experiment_name,
            model_name=model_name,
            automl_tool=automl_tool,
            data_path=data_path,
        )
    assert task.get_define() == expect


def test_mlflow_project_basic_algorithm_get_define():
    """Test task mlflow project BasicAlgorithm function get_define."""
    name = "train_basic_algorithm"
    mlflow_tracking_uri = MLFLOW_TRACKING_URI
    parameters = "n_estimators=200;learning_rate=0.2"
    experiment_name = "basic_algorithm_iris"
    model_name = "iris_B"
    algorithm = "lightgbm"
    data_path = "/data/examples/iris"
    search_params = "max_depth=[5, 10];n_estimators=[100, 200]"

    expect = deepcopy(EXPECT)
    expect["name"] = name
    task_params = expect["taskParams"]

    task_params["mlflowTrackingUri"] = MLFLOW_TRACKING_URI
    task_params["mlflowTaskType"] = MLflowTaskType.MLFLOW_PROJECTS
    task_params["mlflowJobType"] = MLflowJobType.BASIC_ALGORITHM
    task_params["experimentName"] = experiment_name
    task_params["modelName"] = model_name
    task_params["registerModel"] = bool(model_name)
    task_params["dataPath"] = data_path
    task_params["params"] = parameters
    task_params["algorithm"] = algorithm
    task_params["searchParams"] = search_params

    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(CODE, VERSION),
    ):
        task = MLFlowProjectsBasicAlgorithm(
            name=name,
            mlflow_tracking_uri=mlflow_tracking_uri,
            parameters=parameters,
            experiment_name=experiment_name,
            model_name=model_name,
            algorithm=algorithm,
            data_path=data_path,
            search_params=search_params,
        )
    assert task.get_define() == expect
