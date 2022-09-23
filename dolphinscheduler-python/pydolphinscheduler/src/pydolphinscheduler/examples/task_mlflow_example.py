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
"""A example workflow for task mlflow."""

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.mlflow import (
    MLflowDeployType,
    MLflowModels,
    MLFlowProjectsAutoML,
    MLFlowProjectsBasicAlgorithm,
    MLFlowProjectsCustom,
)

mlflow_tracking_uri = "http://127.0.0.1:5000"

with ProcessDefinition(
    name="task_mlflow_example",
    tenant="tenant_exists",
) as pd:

    # run custom mlflow project to train model
    train_custom = MLFlowProjectsCustom(
        name="train_xgboost_native",
        repository="https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native",
        mlflow_tracking_uri=mlflow_tracking_uri,
        parameters="-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9",
        experiment_name="xgboost",
    )

    # run automl to train model
    train_automl = MLFlowProjectsAutoML(
        name="train_automl",
        mlflow_tracking_uri=mlflow_tracking_uri,
        parameters="time_budget=30;estimator_list=['lgbm']",
        experiment_name="automl_iris",
        model_name="iris_A",
        automl_tool="flaml",
        data_path="/data/examples/iris",
    )

    # Using DOCKER to deploy model from train_automl
    deploy_docker = MLflowModels(
        name="deploy_docker",
        model_uri="models:/iris_A/Production",
        mlflow_tracking_uri=mlflow_tracking_uri,
        deploy_mode=MLflowDeployType.DOCKER,
        port=7002,
    )

    train_automl >> deploy_docker

    # run lightgbm to train model
    train_basic_algorithm = MLFlowProjectsBasicAlgorithm(
        name="train_basic_algorithm",
        mlflow_tracking_uri=mlflow_tracking_uri,
        parameters="n_estimators=200;learning_rate=0.2",
        experiment_name="basic_algorithm_iris",
        model_name="iris_B",
        algorithm="lightgbm",
        data_path="/data/examples/iris",
        search_params="max_depth=[5, 10];n_estimators=[100, 200]",
    )

    # Using MLFLOW to deploy model from training lightgbm project
    deploy_mlflow = MLflowModels(
        name="deploy_mlflow",
        model_uri="models:/iris_B/Production",
        mlflow_tracking_uri=mlflow_tracking_uri,
        deploy_mode=MLflowDeployType.MLFLOW,
        port=7001,
    )

    train_basic_algorithm >> deploy_mlflow

    pd.submit()

# [end workflow_declare]
