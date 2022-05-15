# MLflow Node

## Overview

[MLflow](https://mlflow.org) is an excellent open source platform to manage the ML lifecycle, including experimentation,
reproducibility, deployment, and a central model registry.

Mlflow task is used to perform mlflow project tasks, which include basic algorithmic and autoML capabilities (
User-defined MLFlow project task execution will be supported in the near future)

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the
  DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/mlflow.png" width="15"/> task node to canvas.

## Task Parameter

- DolphinScheduler common parameters
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
    - **Custom parameter**: It is a local user-defined parameter for mlflow, and will replace the content
      with `${variable}` in the script.
    - **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as
      upstream of the current task.

- MLflow task specific parameters
    - **mlflow server tracking uri** ：MLflow server uri, default http://127.0.0.1:5000.
    - **experiment name** ：The experiment in which the task is running, if none, is created.
    - **register model** ：Register the model or not. If register is selected, the following parameters are expanded.
        - **model name** : The registered model name is added to the original model version and registered as
          Production.
    - **job type** : The type of task to run, currently including the underlying algorithm and AutoML. (User-defined
      MLFlow project task execution will be supported in the near future)
        - BasicAlgorithm specific parameters
            - **algorithm** ：The selected algorithm currently supports `LR`, `SVM`, `LightGBM` and `XGboost` based
              on [scikit-learn](https://scikit-learn.org/) form.
            - **Parameter search space** : Parameter search space when running the corresponding algorithm, which can be
              empty. For example, the parameter `max_depth=[5, 10];n_estimators=[100, 200]` for lightgbm 。The convention
              will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name,
              and using the name after the equal sign to get the corresponding parameter value through `python eval()`.
        - AutoML specific parameters
            - **AutoML tool** : The AutoML tool used, currently
              supports [autosklearn](https://github.com/automl/auto-sklearn)
              and [flaml](https://github.com/microsoft/FLAML)
        - Parameters common to BasicAlgorithm and AutoML
        - **data path** : The absolute path of the file or folder. Ends with .csv for file or contain train.csv and
          test.csv for folder（In the suggested way, users should build their own test sets for model evaluation）。
        - **parameters** : Parameter when initializing the algorithm/AutoML model, which can be empty. For example
          parameters `"time_budget=30;estimator_list=['lgbm']"` for flaml 。The convention will be passed with '; 'shards
          each parameter, using the name before the equal sign as the parameter name, and using the name after the equal
          sign to get the corresponding parameter value through `python eval()`.
            - BasicAlgorithm
                - [lr](https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression)
                - [SVM](https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC)
                - [lightgbm](https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier)
                - [xgboost](https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier)
            - AutoML
                - [flaml](https://microsoft.github.io/FLAML/docs/reference/automl#automl-objects)
                - [autosklearn](https://automl.github.io/auto-sklearn/master/api.html)

## Task Example

### Preparation

#### Conda env

You need to enter the admin account to configure a conda environment variable（Please
install [anaconda](https://docs.continuum.io/anaconda/install/)
or [miniconda](https://docs.conda.io/en/latest/miniconda.html#installing ) in advance )

![mlflow-conda-env](/img/tasks/demo/mlflow-conda-env.png)

Note During the configuration task, select the conda environment created above. Otherwise, the program cannot find the
Conda environment.

![mlflow-set-conda-env](/img/tasks/demo/mlflow-set-conda-env.png)

#### Start the mlflow service

Make sure you have installed MLflow, using 'PIP Install MLFlow'.

Create a folder where you want to save your experiments and models and start mlFlow service.

```
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

After running, an MLflow service is started

### Run BasicAlgorithm task

The following example shows how to create an MLflow BasicAlgorithm task.

![mlflow-basic-algorithm](/img/tasks/demo/mlflow-basic-algorithm.png)

After this, you can visit the MLFlow service ([http://localhost:5000](http://localhost:5000)) page to view the experiments and models.

![mlflow-server](/img/tasks/demo/mlflow-server.png)

### Run AutoML task

![mlflow-automl](/img/tasks/demo/mlflow-automl.png)
