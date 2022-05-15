# MLflow节点

## 综述

[MLflow](https://mlflow.org) 是一个MLops领域一个优秀的开源项目， 用于管理机器学习的生命周期，包括实验、可再现性、部署和中心模型注册。

MLflow 任务用于执行 MLflow Project 任务，其中包含了阈值的基础算法能力与AutoML能力（将在不久将来支持用户自定义的mlflow project任务执行）。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="/img/tasks/icons/mlflow.png" width="15"/> 任务节点到画板中。

## 任务参数

- DS通用参数
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
    - **自定义参数** ：是 mlflow 局部的用户自定义参数，会替换脚本中以 ${变量} 的内容
    - **前置任务** ：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

- MLflow任务特定参数
    - **mlflow server tracking uri** ：MLflow server 的连接, 默认 http://127.0.0.1:5000。
    - **实验名称** ：任务运行时所在的实验，若无则创建。
    - **注册模型** ：是否注册模型，若选择注册，则会展开以下参数。
        - **注册的模型名称** : 注册的模型名称，会在原来的基础上加上一个模型版本，并注册为Production。
    - **任务类型** : 运行的任务类型，目前包括基础算法与AutoML, 后续将会支持用户自定义的ML Project。
        - 基础算法下的特有参数
            - **算法** ：选择的算法，目前基于 [scikit-learn](https://scikit-learn.org/) 形式支持 `lr`, `svm`, `lightgbm`, `xgboost`.
            - **参数搜索空间** : 运行对应算法的参数搜索空间, 可为空。如针对lightgbm 的 `max_depth=[5, 10];n_estimators=[100, 200]`
              则会进行对应搜索。约定传入后会以`;`切分各个参数，等号前的名字作为参数名，等号后的名字将以python eval执行得到对应的参数值
        - AutoML下的参数下的特有参数
            - **AutoML工具** : 使用的AutoML工具，目前支持 [autosklearn](https://github.com/automl/auto-sklearn)
              , [flaml](https://github.com/microsoft/FLAML)
        - BasicAlgorithm 和 AutoML共有参数
            - **数据路径** : 文件/文件夹的绝对路径, 若文件需以.csv结尾（自动切分训练集与测试集）, 文件夹需包含train.csv和test.csv（建议方式，用户应自行构建测试集用于模型评估）。
            - **参数** : 初始化模型/AutoML训练器时的参数，可为空, 如针对 flaml 设置`"time_budget=30;estimator_list=['lgbm']"`。约定传入后会以`;`
              切分各个参数，等号前的名字作为参数名，等号后的名字将以python eval执行得到对应的参数值。详细的参数列表如下:
                - BasicAlgorithm
                    - [lr](https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.LogisticRegression.html#sklearn.linear_model.LogisticRegression)
                    - [SVM](https://scikit-learn.org/stable/modules/generated/sklearn.svm.SVC.html?highlight=svc#sklearn.svm.SVC)
                    - [lightgbm](https://lightgbm.readthedocs.io/en/latest/pythonapi/lightgbm.LGBMClassifier.html#lightgbm.LGBMClassifier)
                    - [xgboost](https://xgboost.readthedocs.io/en/stable/python/python_api.html#xgboost.XGBClassifier)
                - AutoML
                    - [flaml](https://microsoft.github.io/FLAML/docs/reference/automl#automl-objects)
                    - [autosklearn](https://automl.github.io/auto-sklearn/master/api.html)

## 任务样例

### 前置准备

#### conda 环境配置

你需要进入admin账户配置一个conda环境变量（请提前[安装anaconda](https://docs.continuum.io/anaconda/install/)
或者[安装miniconda](https://docs.conda.io/en/latest/miniconda.html#installing) )

![mlflow-conda-env](/img/tasks/demo/mlflow-conda-env.png)

后续注意配置任务时，环境选择上面创建的conda环境，否则程序会找不到conda环境

![mlflow-set-conda-env](/img/tasks/demo/mlflow-set-conda-env.png)

#### mlflow service 启动

确保你已经安装mlflow，可以使用`pip install mlflow`进行安装

在你想保存实验和模型的地方建立一个文件夹，然后启动 mlflow service

```
mkdir mlflow
cd mlflow
mlflow server -h 0.0.0.0 -p 5000 --serve-artifacts --backend-store-uri sqlite:///mlflow.db
```

运行后会启动一个mlflow服务

### 执行 基础算法 任务

以下实例展示了如何创建 mlflow 基础算法任务

![mlflow-basic-algorithm](/img/tasks/demo/mlflow-basic-algorithm.png)

执行完后可以通过访问 mlflow service ([127.0.0.1:5000](127.0.0.1:5000)) 页面查看实验与模型

![mlflow-server](/img/tasks/demo/mlflow-server.png)

### 执行 AutoML 任务

![mlflow-automl](/img/tasks/demo/mlflow-automl.png)
