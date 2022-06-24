MLflow Node
===========

Overview
--------

[MLflow](https://mlflow.org/) is an excellent open source platform to manage the ML lifecycle, including experimentation, reproducibility, deployment, and a central model registry.

MLflow task plugin used to execute MLflow tasks，Currently contains MLflow Projects and MLflow Models. (Model Registry will soon be rewarded for support)

*   MLflow Projects: Package data science code in a format to reproduce runs on any platform.
*   MLflow Models: Deploy machine learning models in diverse serving environments.
*   Model Registry: Store, annotate, discover, and manage models in a central repository.

The MLflow plugin currently supports and will support the following:

*   \[x\] MLflow Projects
    *   \[x\] BasicAlgorithm: contains LogisticRegression, svm, lightgbm, xgboost
    *   \[x\] AutoML: AutoML tool，contains autosklean, flaml
    *   \[x\] Custom projects: Support for running your own MLflow projects
*   \[ \] MLflow Models
    *   \[x\] MLFLOW: Use `MLflow models serve` to deploy a model service
    *   \[x\] Docker: Run the container after packaging the docker image
    *   \[x\] Docker Compose: Use docker compose to run the container, it will replace the docker run above
    *   \[ \] Seldon core: Use Selcon core to deploy model to k8s cluster
    *   \[ \] k8s: Deploy containers directly to K8S
    *   \[ \] MLflow deployments: Built-in deployment modules, such as built-in deployment to SageMaker, etc
*   \[ \] Model Registry
    *   \[ \] Register Model: Allows artifacts (Including model and related parameters, indicators) to be registered directly into the model center

Create Task
-----------

*   Click `Project Management-> Project Name -> Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/mlflow.png" width="15"/> task node to canvas.

Task Example with Task Parameters
---------------------------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Predecessor task</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr><tr><td colspan="1" class="confluenceTd">MLflow Tracking Server URI</td><td colspan="1" class="confluenceTd"><p>MLflow Tracking Server URI, default<span>&nbsp;</span><a href="http://localhost:5000/" style="text-decoration: none;" class="external-link" rel="nofollow">http://localhost:5000</a>.</p></td></tr><tr><td colspan="1" class="confluenceTd">Experiment Name</td><td colspan="1" class="confluenceTd"><p>Create the experiment where the task is running, if the experiment does not exist. If the name is empty, it is set to<span>&nbsp;</span><code>Default</code>, the same as MLflow.</p></td></tr></tbody></table>

### MLflow Projects

#### BasicAlgorithm

![mlflow-conda-env](/img/tasks/demo/mlflow-basic-algorithm.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd"><strong>Register Model</strong></td><td class="confluenceTd">Register the model or not. If register is selected, the following parameters are expanded.</td></tr><tr><td class="confluenceTd"><strong>Model Name</strong></td><td class="confluenceTd">The registered model name is added to the original model version and registered as Production.</td></tr><tr><td class="confluenceTd"><strong>Data Path</strong></td><td class="confluenceTd"><p>The absolute path of the file or folder. Ends with .csv for file or contain train.csv and test.csv for folder.</p><p>（In the suggested way, users should build their own test sets for model evaluation）</p></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Parameters</strong></td><td colspan="1" class="confluenceTd"><p>Parameter when initializing the algorithm/AutoML model, which can be empty. For example parameters<span>&nbsp;</span><code>"time_budget=30;estimator_list=['lgbm']"</code><span>&nbsp;</span>for flaml 。The convention will be passed with '; ' shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through<span>&nbsp;</span><code>python eval()</code>.</p><ul><li style="list-style-type: none;"><ul><li><a href="https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression" style="text-decoration: none;" class="external-link" rel="nofollow">Logistic Regression</a></li><li><a href="https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC" style="text-decoration: none;" class="external-link" rel="nofollow">SVM</a></li><li><a style="text-decoration: none;" href="https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier" class="external-link" rel="nofollow">lightgbm</a></li><li><a href="https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier" style="text-decoration: none;" class="external-link" rel="nofollow">xgboost</a></li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Algorithm</strong></td><td colspan="1" class="confluenceTd"><p>The selected algorithm currently supports<span>&nbsp;</span><code>LR</code>,<span>&nbsp;</span><code>SVM</code>,<span>&nbsp;</span><code>LightGBM</code><span>&nbsp;</span>and<span>&nbsp;</span><code>XGboost</code><span>&nbsp;</span>based on<span>&nbsp;</span><a style="text-decoration: none;" href="https://scikit-learn.org/" class="external-link" rel="nofollow">scikit-learn</a><span>&nbsp;</span>form.</p></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Parameter Search Space</strong></td><td colspan="1" class="confluenceTd"><p>Parameter search space when running the corresponding algorithm, which can be empty. For example, the parameter<span>&nbsp;</span><code>max_depth=[5, 10];n_estimators=[100, 200]</code><span>&nbsp;</span>for lightgbm 。The convention will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through<span>&nbsp;</span><code>python eval()</code>.</p></td></tr></tbody></table>

#### AutoML Projects

![mlflow-automl](/img/tasks/demo/mlflow-automl.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd"><strong>Register Model</strong></td><td class="confluenceTd">Register the model or not. If register is selected, the following parameters are expanded.</td></tr><tr><td class="confluenceTd"><strong>Model Name</strong></td><td class="confluenceTd">The registered model name is added to the original model version and registered as Production.</td></tr><tr><td class="confluenceTd"><strong>Data Path</strong></td><td class="confluenceTd"><p>The absolute path of the file or folder. Ends with .csv for file or contain train.csv and test.csv for folder.</p><p>（In the suggested way, users should build their own test sets for model evaluation）</p></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Parameters</strong></td><td colspan="1" class="confluenceTd"><p>Parameter when initializing the algorithm/AutoML model, which can be empty. For example parameters<span>&nbsp;</span><code>n_estimators=200;learning_rate=0.2</code><span>&nbsp;</span>for flaml. The convention will be passed with '; 'shards each parameter, using the name before the equal sign as the parameter name, and using the name after the equal sign to get the corresponding parameter value through<span>&nbsp;</span><code>python eval()</code>. The detailed parameter list is as follows:</p><ul><li style="list-style-type: none;"><ul><li><a style="text-decoration: none;" href="https://microsoft.github.io/FLAML/docs/reference/automl#automl-objects" class="external-link" rel="nofollow">flaml</a></li><li><a style="text-decoration: none;" href="https://automl.github.io/auto-sklearn/master/api.html" class="external-link" rel="nofollow">autosklearn</a></li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd"><strong>AutoML tool</strong></td><td colspan="1" class="confluenceTd"><p>The AutoML tool used, currently supports<span>&nbsp;</span><a href="https://github.com/automl/auto-sklearn" style="text-decoration: none;" class="external-link" rel="nofollow">autosklearn</a><span>&nbsp;</span>and<span>&nbsp;</span><a style="text-decoration: none;" href="https://github.com/microsoft/FLAML" class="external-link" rel="nofollow">flaml</a>.</p></td></tr></tbody></table>

Custom Projects

![mlflow-custom-project.png](/img/tasks/demo/mlflow-custom-project.png)

  

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td colspan="1" class="confluenceTd"><strong>Parameters</strong></td><td colspan="1" class="confluenceTd"><p><code>--param-list</code><span>&nbsp;</span>in<span>&nbsp;</span><code>mlflow run</code>. For example<span>&nbsp;</span><code>-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9</code>.</p></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Repository</strong></td><td colspan="1" class="confluenceTd"><p>Repository url of MLflow Project，Support git address and directory on worker. If it's in a subdirectory，We add<span>&nbsp;</span><code>#</code><span>&nbsp;</span>to support this (same as<span>&nbsp;</span><code>mlflow run</code>) , for example<span>&nbsp;</span><code><a href="https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native" class="external-link" rel="nofollow">https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native</a></code>.</p></td></tr><tr><td colspan="1" class="confluenceTd"><strong>Project Version</strong></td><td colspan="1" class="confluenceTd">Version of the project，default master.</td></tr></tbody></table>

You can now use this feature to run all MLFlow projects on Github (For example [MLflow examples](https://github.com/mlflow/mlflow/tree/master/examples) ). You can also create your own machine learning library to reuse your work, and then use DolphinScheduler to use your library with one click.

### MLflow Models

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd"><strong>Model-URI</strong></td><td class="confluenceTd"><ul><li>Model-URI of MLflow , support<span>&nbsp;</span><code>models:/&lt;model_name&gt;/suffix</code><span>&nbsp;</span>format and<span>&nbsp;</span><code>runs:/</code><span>&nbsp;</span>format. See<span>&nbsp;</span><a href="https://mlflow.org/docs/latest/tracking.html#artifact-stores" style="text-decoration: none;" class="external-link" rel="nofollow">https://mlflow.org/docs/latest/tracking.html#artifact-stores</a>.</li></ul></td></tr><tr><td class="confluenceTd"><strong>Port</strong></td><td class="confluenceTd"><span style="letter-spacing: 0.0px;">The port to listen on.: Model-URI of MLflow , support</span><span style="letter-spacing: 0.0px;">&nbsp;</span><code style="letter-spacing: 0.0px;">models:/&lt;model_name&gt;/suffix</code><span style="letter-spacing: 0.0px;">&nbsp;</span><span style="letter-spacing: 0.0px;">format and</span><span style="letter-spacing: 0.0px;">&nbsp;</span><code style="letter-spacing: 0.0px;">runs:/</code><span style="letter-spacing: 0.0px;">&nbsp;</span><span style="letter-spacing: 0.0px;">format. See</span><span style="letter-spacing: 0.0px;">&nbsp;</span><a href="https://mlflow.org/docs/latest/tracking.html#artifact-stores" style="text-decoration: none;" class="external-link" rel="nofollow">https://mlflow.org/docs/latest/tracking.html#artifact-stores</a><span style="letter-spacing: 0.0px;">.</span></td></tr></tbody></table>

#### MLFLOW

![mlflow-models-mlflow](/img/tasks/demo/mlflow-models-mlflow.png)

#### Docker

![mlflow-models-docker](/img/tasks/demo/mlflow-models-docker.png)

#### DOCKER COMPOSE

![mlflow-models-docker-compose](/img/tasks/demo/mlflow-models-docker-compose.png)

  

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd"><strong>Max Cpu Limit</strong></td><td class="confluenceTd"><p>For example<span>&nbsp;</span><code>1.0</code><span>&nbsp;</span>or<span>&nbsp;</span><code>0.5</code>, the same as docker compose.</p></td></tr><tr><td class="confluenceTd"><strong>Max Memory Limit</strong></td><td class="confluenceTd"><p>For example<span>&nbsp;</span><code>1G</code><span>&nbsp;</span>or<span>&nbsp;</span><code>500M</code>, the same as docker compose.</p></td></tr></tbody></table>

Environment to prepare
----------------------

### Conda env

You need to enter the admin account to configure a conda environment variable（Please install [anaconda](https://docs.continuum.io/anaconda/install/) or [miniconda](https://docs.conda.io/en/latest/miniconda.html#installing) in advance).

![mlflow-conda-env](/img/tasks/demo/mlflow-conda-env.png)

Note: During the configuration task, select the conda environment created above. Otherwise, the program cannot find the Conda environment.

![mlflow-set-conda-env](/img/tasks/demo/mlflow-set-conda-env.png)

### Start the mlflow service

Make sure you have installed MLflow, using 'pip install mlflow'.

Create a folder where you want to save your experiments and models and start MLflow service.

```sh
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

After running, an MLflow service is started.

After this, you can visit the MLflow service (`[http://localhost:5000](http://localhost:5000)`) page to view the experiments and models.

![mlflow-server](/img/tasks/demo/mlflow-server.png)