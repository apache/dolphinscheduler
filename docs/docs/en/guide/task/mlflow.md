# MLflow Node

## Overview

[MLflow](https://mlflow.org) is an excellent open source platform to manage the ML lifecycle, including experimentation,
reproducibility, deployment, and a central model registry.

MLflow task plugin used to execute MLflow tasks, Currently contains MLflow Projects and MLflow Models. (Model Registry will soon be rewarded for support)

- MLflow Projects: Package data science code in a format to reproduce runs on any platform.
- MLflow Models: Deploy machine learning models in diverse serving environments.
- Model Registry: Store, annotate, discover, and manage models in a central repository.

The MLflow plugin currently supports and will support the following:

- MLflow Projects
  - BasicAlgorithm: contains LogisticRegression, svm, lightgbm, xgboost
  - AutoML: AutoML tool, contains autosklean, flaml
  - Custom projects: Support for running your own MLflow projects
- MLflow Models
  - MLFLOW: Use `MLflow models serve` to deploy a model service
  - Docker: Run the container after packaging the docker image

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/mlflow.png" width="15"/> task node to canvas.

## Task Parameters and Example

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|       **Parameter**        |                                                                    **Description**                                                                     |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| MLflow Tracking Server URI | MLflow Tracking Server URI, default http://localhost:5000.                                                                                             |
| Experiment Name            | Create the experiment where the task is running, if the experiment does not exist. If the name is empty, it is set to ` Default `, the same as MLflow. |

### MLflow Projects

#### BasicAlgorithm

![mlflow-conda-env](../../../../img/tasks/demo/mlflow-basic-algorithm.png)

**Task Parameters**

|     **Parameter**      |                                                                                                                                                                                                                                                                                                                                                                                                                                                             **Description**                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Register Model         | Register the model or not. If register is selected, the following parameters are expanded.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Model Name             | The registered model name is added to the original model version and registered as Production.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Data Path              | The absolute path of the file or folder. Ends with .csv for file or contain train.csv and test.csv for folder（In the suggested way, users should build their own test sets for model evaluation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Parameters             | Parameter when initializing the algorithm/AutoML model, which can be empty. For example, parameters `"time_budget=30;estimator_list=['lgbm']"` for flaml. The convention will be passed with '; ' shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through `python eval()`. <ul><li>[Logistic Regression](https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression)</li><li>[SVM](https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC)</li><li>[lightgbm](https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier)</li><li>[xgboost](https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier)</li></ul> |
| Algorithm              | The selected algorithm currently supports `LR`, `SVM`, `LightGBM` and `XGboost` based on [scikit-learn](https://scikit-learn.org/) form.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Parameter Search Space | Parameter search space when running the corresponding algorithm, which can be empty. For example, the parameter `max_depth=[5, 10];n_estimators=[100, 200]` for lightgbm. The convention will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through `python eval()`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |

#### AutoML

![mlflow-automl](../../../../img/tasks/demo/mlflow-automl.png)

**Task Parameter**

| **Parameter**  |                                                                                                                                                                                                                                                                                                 **Description**                                                                                                                                                                                                                                                                                                  |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Register Model | Register the model or not. If register is selected, the following parameters are expanded.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| model name     | The registered model name is added to the original model version and registered as Production.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Data Path      | The absolute path of the file or folder. Ends with .csv for file or contain train.csv and test.csv for folder(In the suggested way, users should build their own test sets for model evaluation).                                                                                                                                                                                                                                                                                                                                                                                                                |
| Parameters     | Parameter when initializing the algorithm/AutoML model, which can be empty. For example, parameters `n_estimators=200;learning_rate=0.2` for flaml. The convention will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through `python eval()`. The detailed parameter list is as follows: <ul><li>[flaml](https://microsoft.github.io/FLAML/docs/Use-Cases/Task-Oriented-AutoML)</li><li>[autosklearn](https://automl.github.io/auto-sklearn/master/api.html)</li></ul> |
| AutoML tool    | The AutoML tool used, currently supports [autosklearn](https://github.com/automl/auto-sklearn) and [flaml](https://github.com/microsoft/FLAML).                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

#### Custom projects

![mlflow-custom-project.png](../../../../img/tasks/demo/mlflow-custom-project.png)

**Task Parameter**

|  **Parameter**  |                                                                                                                **Description**                                                                                                                |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| parameters      | `--param-list` in `mlflow run`. For example `-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9`.                                                                                                                                  |
| Repository      | Repository url of MLflow Project, Support git address and directory on worker. If it's in a subdirectory, We add `#` to support this (same as `mlflow run`) , for example `https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native`. |
| Project Version | Version of the project, default master.                                                                                                                                                                                                       |

You can now use this feature to run all MLFlow projects on GitHub (For example [MLflow examples](https://github.com/mlflow/mlflow/tree/master/examples) ). You can also create your own machine learning library to reuse your work, and then use DolphinScheduler to use your library with one click.

### MLflow Models

**General Parameters**

| **Parameter** |                                                                     **Description**                                                                      |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Model-URI     | Model-URI of MLflow , support `models:/<model_name>/suffix` format and `runs:/` format. See https://mlflow.org/docs/latest/tracking.html#artifact-stores |
| Port          | The port to listen on.                                                                                                                                   |

#### MLflow

![mlflow-models-mlflow](../../../../img/tasks/demo/mlflow-models-mlflow.png)

#### Docker

![mlflow-models-docker](../../../../img/tasks/demo/mlflow-models-docker.png)

## Environment to Prepare

### Conda Environment

Please install [anaconda](https://docs.continuum.io/anaconda/install/) or [miniconda](https://docs.conda.io/en/latest/miniconda.html#installing) in advance.

**Method A:**

Config anaconda environment in `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

Add the following content to the file:

```bash
# config anaconda environment
export PATH=/opt/anaconda3/bin:$PATH
```

**Method B:**

You need to enter the admin account to configure a conda environment variable.

![mlflow-conda-env](../../../../img/tasks/demo/mlflow-conda-env.png)

Note During the configuration task, select the conda environment created above. Otherwise, the program cannot find the
Conda environment.

![mlflow-set-conda-env](../../../../img/tasks/demo/mlflow-set-conda-env.png)

### Start the MLflow Service

Make sure you have installed MLflow, using 'pip install mlflow'.

Create a folder where you want to save your experiments and models and start MLflow service.

```sh
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

After running, an MLflow service is started.

After this, you can visit the MLflow service (`http://localhost:5000`) page to view the experiments and models.

![mlflow-server](../../../../img/tasks/demo/mlflow-server.png)

### Preset Algorithm Repository Configuration

If you can't access github, you can modify the following fields in the `common.properties` configuration file to replace the github address with an accessible address.

```yaml
# mlflow task plugin preset repository
ml.mlflow.preset_repository=https://github.com/apache/dolphinscheduler-mlflow
# mlflow task plugin preset repository version
ml.mlflow.preset_repository_version="main"
```

