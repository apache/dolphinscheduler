# Kubeflow Node

## Overview

[Kubeflow](https://www.kubeflow.org) task type is used to create tasks on Kubeflow.

The backend mainly uses the `kubectl` command to create kubeflow tasks, and continues to monitor the resource status on Kubeflow until the task is completed.

Now it mainly supports creating kubeflow tasks using yaml files. If you need to publish `kubeflow pipeline` tasks, you can use the [python task type](./python.md).

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/kubeflow.png" width="15"/> from the toolbar to the canvas.

## Task Example

The task plugin picture is as follows

![kubeflow](../../../../img/tasks/demo/kubeflow.png)

### First, introduce some general parameters of DolphinScheduler

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

### Here are some specific parameters for the Kubeflow plugin

- **Namespace**：The namespace parameter of the cluster
- **yamlContent**：CRD YAML file content

## Environment Configuration

**Configure Kubernetes environment**

Reference [Cluster Management and Namespace Management](../security.md).

Only the required fields need to be filled in, and the others do not need to be filled in. The resource management depends on the YAML file definition in the specific Job.

**kubectl**

Install [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/), and make sure `kubectl` can submit tasks to kubeflow normally.

