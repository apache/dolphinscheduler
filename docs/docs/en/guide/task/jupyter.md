# Jupyter

## Overview

Use `Jupyter Task` to create a jupyter-type task and execute jupyter notes. When the worker executes `Jupyter Task`,
it will use `papermill` to evaluate jupyter notes. Click [here](https://papermill.readthedocs.io/en/latest/) for details about `papermill`.

## Conda Configuration
 
- Config `conda.path` in `common.properties` to the path of your `conda.sh`, which should be the same `conda` you use to manage the python environment of your `papermill` and `jupyter`.
Click [here](https://docs.conda.io/en/latest/) for more information about `conda`.
- `conda.path` is set to `/opt/anaconda3/etc/profile.d/conda.sh` by default. If you have no idea where your `conda` is, simply run `conda info | grep -i 'base environment'`.

## Create Task

- Click Project Management-Project Name-Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag <img src="/img/tasks/icons/jupyter.png" width="15"/> from the toolbar to the canvas.

## Task Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Number of failed retry attempts: The failure task resubmitting times. It supports drop-down and hand-filling.
- Failed retry interval: The time interval for resubmitting the task after a failed task. It supports drop-down and hand-filling.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will send and the task execution will fail.
- Conda Env Name: Name of conda environment.
- Input Note Path: Path of input jupyter note template.
- Out Note Path: Path of output note.
- Jupyter Parameters: Parameters in json format used for jupyter note parameterization.
- Kernel: Jupyter notebook kernel.
- Engine: Engine to evaluate jupyter notes.
- Jupyter Execution Timeout: Timeout set for each jupyter notebook cell.
- Jupyter Start Timeout: Timeout set for jupyter notebook kernel.
- Others: Other command options for papermill.

## Task Example

### Jupyter Task Example

This example illustrates how to create a jupyter task node.

![demo-jupyter-simple](/img/tasks/demo/jupyter.png)
