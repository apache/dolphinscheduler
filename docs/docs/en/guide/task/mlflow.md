# MLflow Node

## Overview

[MLflow](https://mlflow.org) is an excellent open source platform to manage the ML lifecycle, including experimentation,
reproducibility, deployment, and a central model registry.

MLflow task plugin used to execute MLflow tasks，Currently contains Mlflow Projects and MLflow Models.（Model Registry will soon be rewarded for support）

- Mlflow Projects: Package data science code in a format to reproduce runs on any platform.
- MLflow Models: Deploy machine learning models in diverse serving environments.
- Model Registry: Store, annotate, discover, and manage models in a central repository.

The Mlflow plugin currently supports and will support the following:

- [x] MLflow Projects
    - [x] BasicAlgorithm: contains LogisticRegression, svm, lightgbm, xgboost
    - [x] AutoML: AutoML tool，contains autosklean, flaml
    - [x] Custom projects: Support for running your own MLflow projects
- [ ] MLflow Models
    - [x] MLFLOW: Use `MLflow models serve` to deploy a model service
    - [x] Docker: Run the container after packaging the docker image
    - [ ] Docker Compose: Use docker compose to run the container, Will replace the docker run above
    - [ ] Seldon core: Use Selcon core to deploy model to k8s cluster
    - [ ] k8s: Deploy containers directly to K8S 
    - [ ] mlflow deployments: Built-in deployment modules, such as built-in deployment to SageMaker, etc
- [ ] Model Registry
    - [ ] Register Model: Allows artifacts (Including model and related parameters, indicators) to be registered directly into the model center



## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the
  DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/mlflow.png" width="15"/> task node to canvas.

## Task Example

First, introduce some general parameters of DolphinScheduler

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select
  the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high
  to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected,
  randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm
  email will send and the task execution will fail.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as
  upstream of the current task.

### MLflow Projects

#### BasicAlgorithm

![mlflow-conda-env](/img/tasks/demo/mlflow-basic-algorithm.png)

**Task Parameter**

- **mlflow server tracking uri** ：MLflow server uri, default http://localhost:5000.
- **experiment name** ：Create the experiment where the task is running, if the experiment does not exist. If the name is empty, it is set to ` Default `, the same as MLflow.
- **register model** ：Register the model or not. If register is selected, the following parameters are expanded.
    - **model name** : The registered model name is added to the original model version and registered as
      Production.
- **data path** : The absolute path of the file or folder. Ends with .csv for file or contain train.csv and
  test.csv for folder（In the suggested way, users should build their own test sets for model evaluation）。
- **parameters** : Parameter when initializing the algorithm/AutoML model, which can be empty. For example
  parameters `"time_budget=30;estimator_list=['lgbm']"` for flaml 。The convention will be passed with '; 'shards
  each parameter, using the name before the equal sign as the parameter name, and using the name after the equal
  sign to get the corresponding parameter value through `python eval()`.
    - [Logistic Regression](https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression)
    - [SVM](https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC)
    - [lightgbm](https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier)
    - [xgboost](https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier)
- **algorithm** ：The selected algorithm currently supports `LR`, `SVM`, `LightGBM` and `XGboost` based
  on [scikit-learn](https://scikit-learn.org/) form.
- **Parameter search space** : Parameter search space when running the corresponding algorithm, which can be
  empty. For example, the parameter `max_depth=[5, 10];n_estimators=[100, 200]` for lightgbm 。The convention
  will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name,
  and using the name after the equal sign to get the corresponding parameter value through `python eval()`.

#### AutoML

![mlflow-automl](/img/tasks/demo/mlflow-automl.png)

**Task Parameter**

- **mlflow server tracking uri** ：MLflow server uri, default http://localhost:5000.
- **experiment name** ：Create the experiment where the task is running, if the experiment does not exist. If the name is empty, it is set to ` Default `, the same as MLflow.
- **register model** ：Register the model or not. If register is selected, the following parameters are expanded.
    - **model name** : The registered model name is added to the original model version and registered as
      Production.
- **data path** : The absolute path of the file or folder. Ends with .csv for file or contain train.csv and
  test.csv for folder（In the suggested way, users should build their own test sets for model evaluation）。
- **parameters** : Parameter when initializing the algorithm/AutoML model, which can be empty. For example
  parameters `n_estimators=200;learning_rate=0.2` for flaml 。The convention will be passed with '; 'shards
  each parameter, using the name before the equal sign as the parameter name, and using the name after the equal
  sign to get the corresponding parameter value through `python eval()`. The detailed parameter list is as follows:
  - [flaml](https://microsoft.github.io/FLAML/docs/reference/automl#automl-objects)
  - [autosklearn](https://automl.github.io/auto-sklearn/master/api.html)
- **AutoML tool** : The AutoML tool used, currently
  supports [autosklearn](https://github.com/automl/auto-sklearn)
  and [flaml](https://github.com/microsoft/FLAML)


#### Custom projects

![mlflow-custom-project-template.png](/img/tasks/demo/mlflow-custom-project-template.png)

**Task Parameter**

- **mlflow server tracking uri** ：MLflow server uri, default http://localhost:5000.
- **experiment name** ：Create the experiment where the task is running, if the experiment does not exist. If the name is empty, it is set to ` Default `, the same as MLflow.
- **parameters** : `--param-list` in `mlflow run`. For example `-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9`
- **Repository** : Repository url of MLflow Project，Support git address and directory on worker. If it's in a subdirectory，We add `#` to support this (same as `mlflow run`) , for example `https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native`
- **Project Version** : Version of the project，default master

You can now use this feature to run all mlFlow projects on Github (For example [MLflow examples](https://github.com/mlflow/mlflow/tree/master/examples) )了。You can also create your own machine learning library to reuse your work, and then use DolphinScheduler to use your library with one click.

The actual interface is as follows

![mlflow-custom-project.png](/img/tasks/demo/mlflow-custom-project.png)

### MLflow Models

#### MLFLOW

![mlflow-models-mlflow](/img/tasks/demo/mlflow-models-mlflow.png)

**Task Parameter**

- **mlflow server tracking uri** ：MLflow server uri, default http://localhost:5000.
- **model-uri** ：Model-uri of mlflow , support `models:/<model_name>/suffix` format and `runs:/` format. See https://mlflow.org/docs/latest/tracking.html#artifact-stores
- **Port** ：The port to listen on

#### Docker

![mlflow-models-docker](/img/tasks/demo/mlflow-models-docker.png)

**Task Parameter**

- **mlflow server tracking uri** ：MLflow server uri, default http://localhost:5000.
- **model-uri** ：Model-uri of mlflow , support `models:/<model_name>/suffix` format and `runs:/` format. See https://mlflow.org/docs/latest/tracking.html#artifact-stores
- **Port** ：The port to listen on

## Environment to prepare

### Conda env

You need to enter the admin account to configure a conda environment variable（Please
install [anaconda](https://docs.continuum.io/anaconda/install/)
or [miniconda](https://docs.conda.io/en/latest/miniconda.html#installing ) in advance )

![mlflow-conda-env](/img/tasks/demo/mlflow-conda-env.png)

Note During the configuration task, select the conda environment created above. Otherwise, the program cannot find the
Conda environment.

![mlflow-set-conda-env](/img/tasks/demo/mlflow-set-conda-env.png)

### Start the mlflow service

Make sure you have installed MLflow, using 'PIP Install MLFlow'.

Create a folder where you want to save your experiments and models and start mlFlow service.

```sh
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

After running, an MLflow service is started

After this, you can visit the MLFlow service (`http://localhost:5000`) page to view the experiments and models.

![mlflow-server](/img/tasks/demo/mlflow-server.png)
