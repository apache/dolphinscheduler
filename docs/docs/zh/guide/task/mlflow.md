# MLflow节点

## 综述

[MLflow](https://mlflow.org) 是一个MLops领域一个优秀的开源项目， 用于管理机器学习的生命周期，包括实验、可再现性、部署和中心模型注册。

MLflow 组件用于执行 MLflow 任务，目前包含Mlflow Projects, 和MLflow Models。（Model Registry将在不就的将来支持）

- Mlflow Projects: 将代码打包，并可以运行到任务的平台上。
- MLflow Models: 在不同的服务环境中部署机器学习模型。
- Model Registry: 在一个中央存储库中存储、注释、发现和管理模型 (你也可以在你的mlflow project 里面自行注册模型)。

目前 Mlflow 组件支持的和即将支持的内容如下中：

- [x] MLflow Projects
  - [x] BasicAlgorithm: 基础算法，包含LogisticRegression, svm, lightgbm, xgboost。
  - [x] AutoML: AutoML工具，包含autosklean, flaml。
  - [x] Custom projects: 支持运行自己的MLflow Projects项目
- [ ] MLflow Models
  - [x] MLFLOW: 直接使用 `MLflow models serve` 部署模型
  - [x] Docker: 打包 DOCKER 镜像后部署模型
  - [ ] Docker Compose: 使用Docker Compose 部署模型，将会取代上面的Docker部署
  - [ ] Seldon core: 构建完镜像后，使用Seldon Core 部署到k8s集群上, 可以使用Seldon Core的生成模型管理能力
  - [ ] k8s: 构建完镜像后， 部署到k8s集群上
  - [ ] mlflow deployments: 内置的允许MLflow 部署模块, 如内置的部署到Sagemaker等
- [ ] Model Registry
  - [ ] Register Model: 注册相关工件(模型以及相关的参数，指标)到模型中心

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="/img/tasks/icons/mlflow.png" width="15"/> 任务节点到画板中。


## 任务样例

首先介绍一些DS通用参数

- **节点名称** ：设置任务的名称。一个工作流定义中的节点名称是唯一的。
- **运行标志** ：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- **描述** ：描述该节点的功能。
- **任务优先级** ：worker 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- **Worker 分组** ：任务分配给 worker 组的机器执行，选择 Default，会随机选择一台 worker 机执行。
- **环境名称** ：配置运行脚本的环境。
- **失败重试次数** ：任务失败重新提交的次数。
- **失败重试间隔** ：任务失败重新提交任务的时间间隔，以分钟为单位。
- **延迟执行时间** ：任务延迟执行的时间，以分钟为单位。
- **超时告警** ：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败。
- **前置任务** ：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

### MLflow Projects

#### BasicAlgorithm

![mlflow-conda-env](/img/tasks/demo/mlflow-basic-algorithm.png)

**任务参数**

- **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://localhost:5000。
- **实验名称** ：任务运行时所在的实验，若实验不存在，则创建。若实验名称为空，则设置为`Default`, 与 MLflow 一样。
- **注册模型** ：是否注册模型，若选择注册，则会展开以下参数。
    - **注册的模型名称** : 注册的模型名称，会在原来的基础上加上一个模型版本，并注册为Production。
- **数据路径** : 文件/文件夹的绝对路径, 若文件需以.csv结尾（自动切分训练集与测试集）, 文件夹需包含train.csv和test.csv（建议方式，用户应自行构建测试集用于模型评估）。
详细的参数列表如下:
  - [LogisticRegression](https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression)
  - [SVM](https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC)
  - [lightgbm](https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier)
  - [xgboost](https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier)
- **算法** ：选择的算法，目前基于 [scikit-learn](https://scikit-learn.org/) 形式支持 `lr`, `svm`, `lightgbm`, `xgboost`.
- **参数搜索空间** : 运行对应算法的参数搜索空间, 可为空。如针对lightgbm 的 `max_depth=[5, 10];n_estimators=[100, 200]` 则会进行对应搜索。约定传入后会以;切分各个参数，等号前的名字作为参数名，等号后的名字将以python eval执行得到对应的参数值

#### AutoML

![mlflow-automl](/img/tasks/demo/mlflow-automl.png)

**任务参数**

- **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://localhost:5000。
- **实验名称** ：任务运行时所在的实验，若实验不存在，则创建。若实验名称为空，则设置为`Default`, 与 MLflow 一样。
- **注册模型** ：是否注册模型，若选择注册，则会展开以下参数。
    - **注册的模型名称** : 注册的模型名称，会在原来的基础上加上一个模型版本，并注册为Production。
- **数据路径** : 文件/文件夹的绝对路径, 若文件需以.csv结尾（自动切分训练集与测试集）, 文件夹需包含train.csv和test.csv（建议方式，用户应自行构建测试集用于模型评估）。
- **参数** : 初始化AutoML训练器时的参数，可为空, 如针对 flaml 设置`time_budget=30;estimator_list=['lgbm']`。约定传入后会以; 切分各个参数，等号前的名字作为参数名，等号后的名字将以python eval执行得到对应的参数值。详细的参数列表如下:
  - [flaml](https://microsoft.github.io/FLAML/docs/reference/automl#automl-objects)
  - [autosklearn](https://automl.github.io/auto-sklearn/master/api.html)
- **AutoML工具** : 使用的AutoML工具，目前支持 [autosklearn](https://github.com/automl/auto-sklearn)
  , [flaml](https://github.com/microsoft/FLAML)

#### Custom projects

![mlflow-custom-project-template.png](/img/tasks/demo/mlflow-custom-project-template.png)

**任务参数**

- **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://localhost:5000。
- **实验名称** ：任务运行时所在的实验，若实验不存在，则创建。若实验名称为空，则设置为`Default`, 与 MLflow 一样。
- **参数** : `mlflow run`中的 --param-list 如 `-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9`
- **运行仓库** : MLflow Project的仓库地址，可以为github地址，或者worker上的目录, 如Mlflow project位于子目录，可以添加 `#` 隔开, 如 `https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native`
- **项目版本** : 对应项目中git版本管理中的版本，默认 master

现在你可以使用这个功能来运行github上所有的MLflow Projects (如 [MLflow examples](https://github.com/mlflow/mlflow/tree/master/examples) )了。你也可以创建自己的机器学习库，用来复用你的研究成果，以后你就可以使用DolphinScheduler来一键操作使用你的算法库。

实际运行界面如下

![mlflow-custom-project.png](/img/tasks/demo/mlflow-custom-project.png)


### MLflow Models

#### MLFLOW

![mlflow-models-mlflow](/img/tasks/demo/mlflow-models-mlflow.png)

**任务参数**

- **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://localhost:5000。
- **部署模型的uri** ：mlflow 服务里面模型对应的uri, 支持 `models:/<model_name>/suffix` 格式 和 `runs:/` 格式。
- **部署端口** ：部署服务时的端口。

#### Docker

![mlflow-models-docker](/img/tasks/demo/mlflow-models-docker.png)

- **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://localhost:5000。
- **部署模型的uri** ：mlflow 服务里面模型对应的uri, 支持 `models:/<model_name>/suffix` 格式 和 `runs:/` 格式。
- **部署端口** ：部署服务时的端口。

## 环境准备

### conda 环境配置

你需要进入admin账户配置一个conda环境变量（请提前[安装anaconda](https://docs.continuum.io/anaconda/install/)
或者[安装miniconda](https://docs.conda.io/en/latest/miniconda.html#installing) )

![mlflow-conda-env](/img/tasks/demo/mlflow-conda-env.png)

后续注意配置任务时，环境选择上面创建的conda环境，否则程序会找不到conda环境

![mlflow-set-conda-env](/img/tasks/demo/mlflow-set-conda-env.png)

### mlflow service 启动

确保你已经安装mlflow，可以使用`pip install mlflow`进行安装

在你想保存实验和模型的地方建立一个文件夹，然后启动 mlflow service

```sh
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

运行后会启动一个mlflow服务

可以通过访问 mlflow service (`http://localhost:5000`) 页面查看实验与模型

![mlflow-server](/img/tasks/demo/mlflow-server.png)

