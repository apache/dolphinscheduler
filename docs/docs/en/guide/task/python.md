# Python Node

- Using python nodes, you can directly execute python scripts. For python nodes, workers use `python **` to submit tasks.

> Drag from the toolbar ![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PYTHON.png) task node to the canvas, as shown in the following figure:

<p align="center">
   <img src="/img/python-en.png" width="80%" />
 </p>

- Script: Python program developed by the user.
- Environment Name: Specific Python interpreter path for running the script. If you need to use Python **virtualenv**, you should create multiply environments for each **virtualenv**.  
- Resources: Refers to the list of resource files that need to be called in the script.
- User-defined parameter: It is a user-defined local parameter of Python, and will replace the content with `${variable}` in the script.
- Note: If you import the python file under the resource directory tree, you need to add the `__init__.py` file.
